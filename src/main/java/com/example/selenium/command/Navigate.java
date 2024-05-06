package com.example.selenium.command;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.selenium.bedrock.BedrockClient;
import com.example.selenium.html.HtmlElement;

public class Navigate implements Command {
    

    private static Logger logger = LogManager.getLogger(Navigate.class);
    private BedrockClient service = null;

    public Navigate(){
        try{
        	service = new BedrockClient();
        }catch(Exception e){
        	e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void execute(CommandParams params) throws Exception {


        String url = params.getUrl();
        Integer delay = params.getDelay();
        Integer interactions = params.getInteractions();
        Integer loadWaitTime = params.getLoadWaitTime();
        List<String> pastActions = new ArrayList<>();


        //TODO filter the HTML page removing the script piece of it and test again. For now, I'm going with the script.
                // Open the web browser and navigate to the app's URL
        ChromeOptions options = new ChromeOptions();
        // options.setHeadless(Boolean.TRUE);
        options.addArguments("--remote-allow-origins=*");      
        //TODO implement a tear down that quit() the browser
        final  WebDriver browser = new ChromeDriver(options);
        browser.get(url);

        String html = null;
        final List<HtmlElement> elements = new ArrayList<>();

        // Start testing
        for (int i = 0; i < interactions; i++) {

            Integer step = i+1;  
            
            WebDriverWait wait = new WebDriverWait(browser, Duration.ofSeconds(loadWaitTime));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));     
            
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            elements.addAll(getHtmlElements(browser));
            setIds(browser, elements);

            html = cleanHtml(browser.getPageSource());
            //logger.info("Source:\n "+html);

            String testCase = """
                You are testing the authentication mechanism of this web application. Your login is bexiga@gmail.com and your password is 2024welcome1. The test case finsishes successfully once you are able to input authetication details and presented with a page containing issues for the bazinga blitz. Show me how many issues are there
                    """;

                    // My recommendation is to execute the test case in 4 steps: click on sign in button, input e-mail, input password and authenticate.

/*             String initialPrompt = """ 
                
                    Human: You are a professional tester testing web applications looking for edge cases that will make the test fail. You execute your tests using Selenium, a tool for functional test automation. In order to let you operate Selenium I'll provide you with some inputs and specific instructions listed below:

                    1- One input is the HTML source code of the web page. You will find it inside <code></code> tags
                    2- Another input is the description of the test case you are executing. You will find it inside <testcase></testcase> tags
                    3- Another input is the list of past actions that you have done so far. The first element is the first action of the test and last element is the previous action.
        4- Another input is the number of available interactions. You will find it inside <available-interactions></available-interactions> tags.
        //5- Your output has to be in the following format: <output><script></script><explanation></explanation></output>.
        5- Your output has to be in the following format: <output><script></script><explanation></explanation></output>. If, for any reason, you cannot complete your task, your output should instead contain an explanation for why you could not complete the task and include which data you are missing.
        5- Your output contain a tag <script></script>. The value inside tags <script></script> should be a javascript code. This code will be sent to Selenium using a JavascriptExecutor to execute your action. You will use the HTML source code of the page and provide me with a javascript code containing your action to test the page according to the test case description. I'm adding a few examples of possible scripts inside <script></script> tags.
            <script>document.getElementById('<ELEMENT_ID>').value = 'red';document.getElementById('button-submit').click();</script>
            <script>document.getElementsByClassName('test')[0].value = 'my text';document.getElementsByClassName('test')[2].value = 'my text 3';document.querySelector('#mysubmitButton').click();</script>
                
                    6- The goal is to test as many different features as possible to find potential bugs and make sure to include edge cases. When you think you tested enough cases, please provide an output similar to the following: <output><script>alert(‘end of test’);</script><explanation>In here you add the explanation why you are stopping the test case</explanation></output>

                    <code>%s</code>
                    <testcase>%s</testcase>
                    <actions>%s</actions>
                    <available-interactions>%s</available-interactions>
                    
                    Assistant:                        
                        """; */

                        // The JSON Object must contain a list of actions and and explanation in the same format as shown in the examples I provide. You can choose one click action and several input actions but you can only return the next step and not the full test case. 
                        // Your reply is ONLY the JSON object, DO NOT use no markdown using ``` or ```json. 

            String prompt = String.format(
                """
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
                    <testcase>%s. Your answer is in JSON format. You execute at least 10 steps before failing. Your actions use elements from the input</testcase>
                    <actions>%s</actions>
                    <available-interactions>%s</available-interactions>
                    <interact>%s</interact>
                    
                    Assistant:                        
                        """, html, testCase, pastActions, interactions-i, elements
            );

            //TODO format the html page and remove the script tag let's see what it comes up with
            //logger.info(prompt);

            String response = service.invoke(prompt);

            //logger.info("**************************");
            logger.info(response);
            //logger.info("**************************");

            // Parse the response to get the selected element ID and explanation
            JSONObject jsonResponse = new JSONObject(response);
            //get content object that is an array of json objects
            JSONArray content = jsonResponse.getJSONArray("content");
            //get the first element of the array
            JSONObject firstElement = content.getJSONObject(0);

           // JSONObject text = new JSONObject(firstElement.getString("text"));
            JSONObject text = null;
            String rawResponse = firstElement.getString("text");

            rawResponse = rawResponse.replaceAll("\n", "");
            //logger.info(rawResponse);
            // Regular expression pattern to match a JSON object

            text = new JSONObject( rawResponse.substring(rawResponse.indexOf("{"), rawResponse.lastIndexOf("}")+1));

            if(text.has("status")){
                logger.info(String.format("Test finished. Status: %s. Explanationn: %s", text.getString("status"), text.getString("explanation")));
                break;
            }
/*             Pattern jsonPattern = Pattern.compile("(\\{.*?\\})");
            Matcher matcher = jsonPattern.matcher(rawResponse);

            if (matcher.find()) {
                String jsonObject = matcher.group(0);
                System.out.println("JSON Object: " + jsonObject);
                text = new JSONObject(jsonObject);

                // Extract the remaining message
                String message = rawResponse.substring(matcher.end()).trim();
                System.out.println("Message: " + message);
            } else {
                System.out.println("No JSON object found in the input string.");
                continue;
            }
 */

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
    }

    // private String formatPastActions(Collection <ActionRecord> records){
    //     return String.join("\n", records.stream().map(r -> r.toString()).toList());
    // }

    private static String cleanHtml(String htmlString) {
        Document doc = Jsoup.parse(htmlString);

        // Remove all script and style elements
        Elements scripts = doc.select("script, style");
        for (Element script : scripts) {
            script.remove();
        }

        // Remove the div with id 'coverage'
        Element coverageDiv = doc.selectFirst("#coverage");
        if (coverageDiv != null) {
            coverageDiv.remove();
        }

        // Convert HTML object back to a string without additional newlines
        return doc.html().replaceAll("\\s+", " ");
    }

    private void setIds(WebDriver browser, List<HtmlElement> elements){

        elements.stream().filter(elem -> elem.isIdGenerated()).forEach(elem -> {
                
            ((JavascriptExecutor)browser).executeScript(String.format("arguments[0].id='%s';", elem.getId()), elem.getElement());
         } );  
    }

    private List<HtmlElement> getHtmlElements(WebDriver browser){

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
}
