package com.example.selenium.command;

import java.io.File;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;

public class SolveCaptcha  extends AbstractNavigation {

    private static final Logger logger = LogManager.getLogger(SolveCaptcha.class);

    public SolveCaptcha(CommandParams params){
        super(params);
    }

    @Override
    public Command execute() throws Exception {
       
        String url = params.getUrl();
        Integer loadWaitTime = params.getLoadWaitTime();        
        String testCase = params.getTestCase();

        // Open the web browser and navigate to the app's URL
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(Boolean.TRUE);
        options.addArguments("--remote-allow-origins=*");      

        browser = new ChromeDriver(options);
        browser.get(url);
        //wait for it to finish loading.
        FluentWait<WebDriver> wait = new FluentWait<>(browser);
        wait.withTimeout(Duration.ofMillis(loadWaitTime));
        wait.pollingEvery(Duration.ofMillis(250));
        wait.until(browser-> ((JavascriptExecutor)browser).executeScript("return document.readyState").toString().equals("complete"));

        File screenshot = screenshot();
        String captchaResult = service.invokeWithImage(testCase, screenshot);

        new Actions(browser).sendKeys(Keys.TAB, captchaResult, Keys.ENTER).perform();
        logger.info("Captcha solved");
        return this;
    }
  
}
