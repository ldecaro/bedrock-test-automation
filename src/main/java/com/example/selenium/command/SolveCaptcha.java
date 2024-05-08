package com.example.selenium.command;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SolveCaptcha  extends AbstractNavigation {

    private WebDriver browser = null;

    public SolveCaptcha(){

    }


    @Override
    public Command execute(CommandParams params) throws Exception {
       
        String url = params.getUrl();
        Integer delay = params.getDelay();
        Integer interactions = params.getInteractions();
        Integer loadWaitTime = params.getLoadWaitTime();
        List<String> pastActions = new ArrayList<>();
        String testCase = params.getTestCase();

        // Open the web browser and navigate to the app's URL
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(Boolean.TRUE);
        options.addArguments("--remote-allow-origins=*");      

        browser = new ChromeDriver(options);
        browser.get(url);
        return this;
    }

    @Override
    public Command executeNext(Command c) throws Exception {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented");
    }

    @Override
    public void tearDown() throws Exception {
        browser.close();
        browser.quit();        
    }
    
    
}
