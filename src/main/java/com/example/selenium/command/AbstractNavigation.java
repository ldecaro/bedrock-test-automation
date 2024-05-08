package com.example.selenium.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;

import com.example.selenium.bedrock.BedrockClient;
import com.example.selenium.html.HtmlElement;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

public abstract class AbstractNavigation implements Command {
    
    private static final Logger logger = LogManager.getLogger(AbstractNavigation.class);
    private HtmlCompressor compressor = new HtmlCompressor();
    protected BedrockClient service = null;
    protected WebDriver browser = null;
    protected final CommandParams params;

    public AbstractNavigation(CommandParams params) {
        
        try{
        	service = new BedrockClient();
        }catch(Exception e){
        	e.printStackTrace();
            throw e;
        }
        this.params = params;
    }

    protected void setDriver(WebDriver browser){
        this.browser = browser;
    }

    @Override
    public Command execute() throws Exception {


        String url = params.getUrl();
        // Integer delay = params.getDelay();
        Integer interactions = params.getInteractions();
        Integer loadWaitTime = params.getLoadWaitTime();
        List<String> pastActions = new ArrayList<>();
        String testCase = params.getTestCase();    

        if( browser == null ){
            // Open the web browser and navigate to the app's URL
            ChromeOptions options = new ChromeOptions();
            options.setHeadless(Boolean.TRUE);
            options.addArguments("--remote-allow-origins=*");  
            browser = new ChromeDriver(options);
        }
        browser.get(url);

        String html = null;
        final List<HtmlElement> elements = new ArrayList<>();

        // Start testing
        for (int i = 0; i < interactions; i++) {

            Integer step = i+1;  
            
            // WebDriverWait wait = new WebDriverWait(browser, Duration.ofMillis(loadWaitTime));
            // wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));   
            FluentWait<WebDriver> wait = new FluentWait<>(browser);
            wait.withTimeout(Duration.ofMillis(loadWaitTime));
            wait.pollingEvery(Duration.ofMillis(250));
            // wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            wait.until(browser-> ((JavascriptExecutor)browser).executeScript("return document.readyState").toString().equals("complete"));
            
            // IWait<IWebDriver> wait = new OpenQA.Selenium.Support.UI.WebDriverWait(driver, TimeSpan.FromSeconds(30.00));
            // wait.Until(driver1 => ((IJavaScriptExecutor)driver).ExecuteScript("return document.readyState").Equals("complete"));

            // try {
            //     Thread.sleep(delay);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            //     Thread.currentThread().interrupt();
            // }
    
            elements.addAll(getHtmlElements(browser));
            setIds(browser, elements);

            html = cleanHtml(browser.getPageSource());
            // logger.info("HTML: "+html);
            logger.info("HTML length: "+html.length());
            logger.info("HTML COMPRESSED: "+compressor.compress(html).length());
            String prompt = String.format( getPrompt(), html, testCase, pastActions, interactions-i, elements);

            //logger.info("Source:\n "+html);
             logger.info("Prompt Length:"+prompt.length());
            screenshot();
            String response = service.invoke(prompt);

            logger.info(response);

            JSONObject text = getResponseJSON(response);

            if(text.has("status")){
                logger.info(String.format("Test finished. Status: %s. Explanation: %s", text.getString("status"), text.getString("explanation")));   
                //take a screenshot
                screenshot();
                break;
            }

            JSONArray actions = text.getJSONArray("actions");
            String explanation = text.getString("explanation");

            //add step information inside the JSONObject text
            text.put("step", step);
            logger.info(String.format("Step #%s. Explanation: %s", step, explanation));
            
            HtmlElement click = null;
            for(int ii=0; ii<actions.length(); ii++){
                JSONObject action = actions.getJSONObject(ii);
                if( "input".equals(action.getString("action")) ){

                    Optional<HtmlElement> element = elements.stream().filter(elem-> elem.getId().equals(action.getString("id"))).findFirst();
                    if(element.isPresent()){
                        ((JavascriptExecutor)browser).executeScript("arguments[0].focus();", element.get().getElement());
                        element.get().getElement().sendKeys(Keys.chord(Keys.CONTROL, "a"), action.getString("value"));
                        logger.info("Inputted value "+action.getString("value")+" on "+element.get().getId());                        
                    }
                }else if( "click".equals(action.getString("action")) ){
                    
                    Optional<HtmlElement> webElement = elements.stream().filter(e-> action.getString("id").equals(e.getId())).findFirst();
                    if(webElement.isPresent()){
                        click = webElement.get();
                    }
                }
            }

            if( click == null){
                logger.info("No click action found");
                pastActions.add(text.toString());
                elements.clear();
                new Actions(browser).sendKeys(Keys.TAB, Keys.ENTER).perform();
                continue;
            }

            logger.info("Clicking on "+click.getId());
            new Actions(browser).moveToElement(click.getElement()).click().perform();
            
            pastActions.add(text.toString());
            elements.clear();
        }
        return this;
    }

    protected static String cleanHtml(String htmlString) {

        final Document doc = Jsoup.parse(htmlString);

        // Remove all script and style elements
        Elements scripts = doc.select("script, style");
        for (Element script : scripts) {
            script.remove();
        }
        //remove script tags inside the body
        Document body = Jsoup.parseBodyFragment(doc.select("body").html());
        body.select("script").remove();

        //remove script and head from inside the iframe
        Elements iframes = doc.select("iframe");

        // Iterate through the <iframe> elements
        for (Element iframe : iframes) {

            if(!iframe.attributes().hasKey("srcdoc")) continue;
            // Parse the content of the <iframe> as a new Document
            Document iframeDoc = Jsoup.parse(iframe.attributes().get("srcdoc"));
        
            // Select all <script> elements within the <head> section of the <iframe>
            scripts = iframeDoc.select("script");
        
            // Remove the selected <script> elements
            scripts.remove();
        
            // Update the <iframe> content with the modified HTML
            iframe.html(iframeDoc.outerHtml());
        }        

        // Remove the div with id 'coverage'
        Element coverageDiv = doc.selectFirst("#coverage");
        if (coverageDiv != null) {
            coverageDiv.remove();
        }

        //remove the attributes data- attributes from a, ul, div, span, input
        Elements elementsWithDataAttrs = doc.select("[^data-]");
        elementsWithDataAttrs.forEach(elem->{
           
           List<Attribute> attributes = elem.attributes().asList();
           attributes.stream().filter(attr->attr.getKey().indexOf("data-") != -1).forEach(attr->elem.removeAttr(attr.getKey()));
        });

        //if href starts with / we will on try to remove the extra info and update href with information inside the first / and the second /
        Elements links = doc.select("a");
        links.stream().forEach(link->{

            String href = link.attr("href");
            if(href.startsWith("/")){
                String[] parts = href.split("/");
                if(parts.length > 2){
                    link.attr("href", "/"+parts[1]+"/");
                }
            }else if(href.startsWith("https")){
                String[] parts = href.split("/");
                if(parts.length > 3){
                    link.attr("href", "https://"+parts[2]);
                }
                if( parts.length == 3){
                    if(parts[2].indexOf("?")!=-1){
                        link.attr("href", "https://"+parts[2].substring(0, parts[2].indexOf("?")));
                    }else{
                        link.attr("href", "https://"+parts[2]);
                    }
                }
            }
        });

        // Remove the alt attribute from images
        Elements images = doc.select("img");
        images.stream().forEach(image->image.removeAttr("alt"));
        images.stream().forEach(image->image.removeAttr("srcset"));        
        
        return doc.html().replaceAll("\\s+", " ");

        // links.stream().forEach(link-> link.a)
        
        // List<String> dataAttr = elem.dataset().keySet().stream().filter(s->s.indexOf("data-")!= -1).toList();
        // dataAttr.forEach(s->elem.removeAttr(s));


        //TODO test updating the HREF value using JSOUP
        // Remove href and image alts
        // String html = doc.html().replaceAll("\\s+", " ");
        // String hrefPattern = "\\s+href\\s*=\\s*\".*?\"";
        // String altPattern = "\\s+alt\\s*=\\s*\".*?\"";
        // String updatedHtmlCode = html.replaceAll(hrefPattern, "");
        // return updatedHtmlCode.replaceAll(altPattern, "");


        // Convert HTML object back to a string without additional newlines
        // return 
    }

    protected void setIds(WebDriver browser, List<HtmlElement> elements){

        elements.stream().filter(elem -> elem.isIdGenerated()).forEach(elem -> {
                
            ((JavascriptExecutor)browser).executeScript(String.format("arguments[0].id='%s';", elem.getId()), elem.getElement());
         } );  
    }

    protected List<HtmlElement> getHtmlElements(WebDriver browser){

        List<HtmlElement> buttons   =   browser.findElements(By.xpath("//button"))
            .stream()
            .filter(e->e.isDisplayed())
            .filter(e->e.isEnabled())
            .map(e-> HtmlElement.builder()
                .id(e.getAttribute("id"))
                .type("clickable")
                .element(e)
                .build())
            .toList();

        List<HtmlElement> inputs = browser.findElements(By.tagName("input"))
            .stream()
            .filter(e->e.isDisplayed())
            .filter(e->e.isEnabled())
            .map(e-> HtmlElement.builder()
                .id(e.getAttribute("id"))
                .type("input")
                .element(e)
                .build())
            .toList();

        List<HtmlElement> anchors = browser.findElements(By.tagName("a"))
            .stream()
            .filter(e->e.isDisplayed())
            .filter(e->e.isEnabled())
            .map(e-> HtmlElement.builder()
                .id(e.getAttribute("id"))
                .type("clickable")
                .element(e)
                .build())
            .toList();       
            
        List<HtmlElement> textarea = browser.findElements(By.tagName("textarea"))
            .stream()
            .filter(e->e.isDisplayed())
            .filter(e->e.isEnabled())
            .map(e-> HtmlElement.builder()
                .id(e.getAttribute("id"))
                .type("input")
                .element(e)
                .build())
            .toList();           

        List<HtmlElement> clickable = browser.findElements(By.xpath("//*[not(self::button or self::a or self::input)][@onclick or contains(@onclick, 'click')]"))
            .stream()
            .filter(e->e.isDisplayed())
            .filter(e->e.isEnabled())
            .map(e-> HtmlElement.builder()
                .id(e.getAttribute("id"))
                .type("clickable")
                .element(e)
                .build())
            .toList(); 
        
        final List<HtmlElement> elements = new ArrayList<>();
        elements.addAll(buttons);
        elements.addAll(inputs);
        elements.addAll(anchors);
        elements.addAll(textarea);
        elements.addAll(clickable);

        return elements;
    }
    
    JSONObject getResponseJSON(String response) throws Exception {

        // Parse the response to get the selected element ID and explanation
        JSONObject jsonResponse = new JSONObject(response);
        //get content object that is an array of json objects
        JSONArray content = jsonResponse.getJSONArray("content");
        //get the first element of the array
        JSONObject firstElement = content.getJSONObject(0);

        String rawResponse = firstElement.getString("text");

        rawResponse = rawResponse.replaceAll("\n", "");
        //extract JSON Object from the response
        return new JSONObject( rawResponse.substring(rawResponse.indexOf("{"), rawResponse.lastIndexOf("}")+1));
    }
    
    protected File screenshot() throws IOException{
        File screenshot = ((TakesScreenshot)browser).getScreenshotAs(OutputType.FILE);
        String screenshotName = String.format("screenshot-%d.png", System.currentTimeMillis());
        File screenshotFile = new File(screenshotName);
        Files.copy(screenshot.toPath(), screenshotFile.toPath());
        logger.info("Screenshot saved to "+screenshotFile.toString());

        return screenshotFile;
    }

    protected String getPrompt(){
       return """
            Human: You are a professional tester testing web applications looking for edge cases that will make the test fail. You provide an output to the next step you need to complete the text case. You can provide values to several inputs at once but one click action only. Your actions must use actionable elements from the input. Provide the information to the next step according to the following instructions:

            1- One input is the HTML source code of the web page. You will find it inside <code></code> tags.
            2- Another input is the description of the test case you are executing. You will find it inside <testcase></testcase> tags
            3- Another input is the list of past actions that you have done so far. The first element is the first action of the test and last element is the previous action. You will find it inside <action></action> tags
            4- Another input is the number of available interactions. You will find it inside <available-interactions></available-interactions> tags.
            5- Another input is the list of elements available for you to interact with. They are of type input or clickable. You will find it inside <interact></interact> tags
            6- Your answer must always be JSON Object containing only the next step. The object should contain a key "explanation" and a key "actions". Key "actions" is an array of JSON objects with keys "action", "id" and "value". Sometimes you need to click an element to visualize the input form. These are the examples:
            <examples>
            {"explanation":"Click on the button to submit the form","actions":[{"action":"click","id":"button1","value":"Submit"}, {"action":"input","id":"name-field","value":"John Doe"}, {"action":"input","id":"dropdown-menu","value":"Option 2"}, {"action":"input","id":"email-field","value":"johndoe@example.com"} ]}
            {"explanation":"Click on the button to submit the form","actions":[{"action":"click","id":"link-1","value":"Learn More"}]}
            {"explanation":"Click on the button to submit the form","actions": [{"action":"input","id":"name-field","value":"John Doe"}, {"action":"input","id":"dropdown-menu","value":"Option 2"}, {"action":"input","id":"email-field","value":"johndoe@example.com"}, {"action":"click","id":"link-sign-in","value":"SignIn"} ]}
            </examples>
            7- When test case is completed your answer must be a JSON object with two keys, status and explanation. Here are a few examples:
            <examples>
            {"status":"success","explanation":"<EXPLANATION>"}
            {"status":"failure","explanation":"<EXPLANATION>"}
            </examples>
            8- For test to finish successfully, your explanation must contain evidence within the source HTML code that conditions to finish the test were met. Do not finish test successfully becore finding evidence within the HTML code.

            <code>%s</code>
            <testcase>%s. Your answer is in JSON format. Your answer contain at most only one click action. You execute at least 10 steps before failing. Your actions use elements from the input</testcase>
            <actions>%s</actions>
            <available-interactions>%s</available-interactions>
            <interact>%s</interact>
            
            Assistant:                        
                """;   
    }

    @Override
    public Command andThen(Command c) throws Exception {
        
        ((AbstractNavigation)c).setDriver(browser);
        return c.execute();
    }

    @Override
    public void tearDown() throws Exception {
        //release resources
        browser.close();
        browser.quit();
    }
    
}
