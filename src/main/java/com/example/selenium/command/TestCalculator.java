package com.example.selenium.command;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.example.selenium.App;
import com.example.selenium.bedrock.BedrockClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TestCalculator implements Command {

    private static final Logger logger = LogManager.getLogger(App.class);
    private WebDriver browser;
    private final CommandParams params;

    public TestCalculator(CommandParams params){
        this.params = params;
    }
    
    @Override
    public Command execute() throws Exception {

        String url = params.getUrl();
        Integer delay = params.getDelay();
        Integer interactions = params.getInteractions();
        Integer loadWaitTime = params.getLoadWaitTime();
        String testType = "bedrock";
        String outputDir = "./output/";
        List<ActionRecord> pastActions = new ArrayList<>();

        URI uri = new URI(url);
        if( "file".equals(uri.toURL().getProtocol()) ){
            url = Paths.get(url).toAbsolutePath().toUri().toString();
        }

        logger.info(String.format("Configuration: url=%s, delay=%d, interactions=%d, loadWaitTime=%d, testType=%s, outputDir=%s", url, delay, interactions, loadWaitTime, testType, outputDir));
        logger.info("Starting the test on URL: " + url);

        Gson gson = new Gson();

        // Open the web browser and navigate to the app's URL
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(Boolean.TRUE);
        options.addArguments("--remote-allow-origins=*");        

        browser.get(url);

        // Wait for the elements to load
        WebDriverWait wait = new WebDriverWait(browser, Duration.ofSeconds(loadWaitTime));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        logger.info("Web page loaded successfully.");

        // Get the filtered HTML source code of the page
        String filteredHtml = filterHtml(browser.getPageSource());

        BedrockClient service = new BedrockClient();

        // Start testing
        for (int i = 0; i < interactions; i++) {
            // Refresh the list of buttons before each interaction
            List<WebElement> buttons = browser.findElements(By.xpath("//button"));
            WebElement displayElement = browser.findElement(By.id("display"));

            WebElement element = null;
            if (testType.equals("monkey")) {
                // Choose a random button
                element = buttons.get(new Random().nextInt(buttons.size()));
            } else if (testType.equals("bedrock")) {
                // Create list of clickable elements using IDs
                List<String> clickableElementsData = new ArrayList<>();
                for (WebElement button : buttons) {
                    clickableElementsData.add(button.getAttribute("id"));
                }

                // Create the prompt for the Claude model with task description
                // String prompt = String.format(
                //         "Given a web application, you are tasked with testing its functionality. Here is the filtered HTML source code of the web application: '%s'. Here are the available interactable GUI elements: %s. Here are the ordered past actions that you have done for this test (first element was the first action of the test and the last element was the previous action): %s. Please output the id of the element to click on next and provide a brief explanation or reasoning for your choice. Remember, the goal is to test as many different features as possible to find potential bugs and make sure to include edge cases. Your response should be only a print of a JSON Object in the following format: {\"id\": \"<element-id>\", \"explanation\": \"<explanation>\"}.",
                //         filteredHtml, clickableElementsData, formatPastActions(pastActions));

                String prompt = String.format(
                    """
                        Human: You are a professional tester testing web applications looking for edge cases that will make the test fail. Please follow the following instructions:

                        1- A filtered HTML source for the web application is sent with these instructions inside <code></code> tags
                        2- A list of interactable elements is sent inside <interact></interact> tags
                        3- A list of past actions that you have done for this test is sent inside <actions></actions> tags. The first element is the first action of the test and last element is the previous action
                        4- Please output the id of the element to click on next and provide a brief explanation or reasoning for your choice
                        5- The goal is to test as many different features as possible to find potential bugs and make sure to include edge cases. Also make sure to try to get to a 100%% coverage with the total interactions you have available. Your total interactions available is informed inside <available-interactions></available-interactions> tags
                        6- Your responnse should be sent as a JSON object that follows the format:{\"id\": \"<element-id>\", \"explanation\": \"<explanation>\"}. Your reply is ONLY the JSON object, DO NOT use no markdown using ``` or ```json
                        
                        <code>%s</code>
                        <interact>%s</interact>
                        <actions>%s</actions>
                        <available-interactions>%s</available-interactions>
                        
                        Assistant:
                    """, filteredHtml, clickableElementsData, formatPastActions(pastActions), interactions-i);
                
                logger.info(prompt);

                String response = service.invoke(prompt);
                
                // logger.info("Model Response: \n\n"+response);
                // Parse the response to get the selected element ID and explanation
                JSONObject jsonResponse = new JSONObject(response);
                //get content object that is an array of json objects
                JSONArray content = jsonResponse.getJSONArray("content");
                //get the first element of the array
                JSONObject firstElement = content.getJSONObject(0);
                //get the id property of the first element
                JSONObject cleanResponse = new JSONObject(firstElement.getString("text"));

                String actionId = cleanResponse.getString("id");
                String explanation = cleanResponse.getString("explanation");
                logger.info(explanation);

                // Find the button with the selected ID
                for (WebElement button : buttons) {
                    if (button.getAttribute("id").equals(actionId)) {
                        element = button;
                        break;
                    }
                }
                if (element == null) {
                    throw new RuntimeException("No button found with id: " + actionId);
                }
            } else {
                throw new IllegalArgumentException("Invalid test type: " + testType);
            }

            element.click();

            // Check for alert and accept it if present
            try {
                Alert alert = browser.switchTo().alert();
                logger.info("Alert found with message: " + alert.getText() + ". Accepting it.");
                alert.accept();
            } catch (Exception e) {
                // No alert, so pass
            }

            // Get coverage percentage
            String coveragePercentage = null;
            try {
                WebElement coverageElement = browser.findElement(By.id("percentage"));
                String coverageText = coverageElement.getText();
                Pattern pattern = Pattern.compile("(\\d+.\\d+)%");
                Matcher matcher = pattern.matcher(coverageText);
                if (matcher.find()) {
                    coveragePercentage = matcher.group(1);
                }
            } catch (Exception e) {
                logger.info("Could not find coverage element or extract percentage: " + e.getMessage());
            }

            // Record the observation after the action
            String currentObservation = displayElement.getAttribute("value");
            String currentAction = element.getAttribute("id");

            logger.info(String.format("Action %d: %s tester clicking button with id: '%s' | Current observation: %s | Coverage: %s%%",
                    i + 1, testType.substring(0, 1).toUpperCase() + testType.substring(1), currentAction, currentObservation, coveragePercentage));

            // Record action
            pastActions.add(new ActionRecord(i + 1, currentAction, currentObservation, coveragePercentage));

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        // Extract and log the code coverage data after all interactions
        String coverageJsonString = (String) ((JavascriptExecutor)browser).executeScript("return JSON.stringify(coverage)");
        JsonObject coverageData = gson.fromJson(coverageJsonString, JsonObject.class);
        int coveredBlocks = 0;
        int totalBlocks = 0;
        for (String functionName : coverageData.keySet()) {
            JsonObject function = coverageData.getAsJsonObject(functionName);
            for (String block : function.keySet()) {
                totalBlocks++;
                if (function.get(block).getAsInt() > 0) {
                    coveredBlocks++;
                }
            }
        }
        double coveragePercentage = totalBlocks > 0 ? (double) coveredBlocks / totalBlocks * 100 : 0;

        logger.info("Final coverage data: " + coverageData.toString());
        logger.info(String.format("Detailed coverage calculation explanation: Out of the total number of %d blocks across all functions, %d were covered (i.e., executed at least once during the test). This leads to a final coverage percentage of %.2f%%. This percentage represents the ratio of the number of covered blocks to the total number of blocks, giving equal weight to each block.", totalBlocks, coveredBlocks, coveragePercentage));

        // Close the driver
        logger.info("Test run completed.");
        browser.quit();

        // Time-stamp to uniquely identify this run
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // Create a unique subfolder for each test run
        outputDir = Paths.get(outputDir, timestamp).toString();

        // Create results directory if it doesn't exist
        Files.createDirectories(Paths.get(outputDir));

        // Save click arguments
        Path configFile = Paths.get(outputDir, "config.json");
        JsonObject config = new JsonObject();
        config.addProperty("url", url);
        config.addProperty("delay", delay);
        config.addProperty("interactions", interactions);
        config.addProperty("loadWaitTime", loadWaitTime);
        config.addProperty("testType", testType);
        config.addProperty("outputDir", outputDir);
        Files.write(configFile, gson.toJson(config).getBytes());

        // Save past actions to an output file
        Path pastActionsFile = Paths.get(outputDir, "past_actions.json");
        Files.write(pastActionsFile, gson.toJson(pastActions).getBytes());
        logger.info("Past actions saved to: " + pastActionsFile);   
        return this;     
    }

    private static String filterHtml(String htmlString) {
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

    private static String formatPastActions(List<ActionRecord> pastActions) {
        if (pastActions.isEmpty()) {
            return "No actions available.";
        }

        StringBuilder sb = new StringBuilder();
        for (ActionRecord action : pastActions) {
            sb.append(String.format("Step %d: Action: %s | Observation: %s | Coverage Percentage: %s%%\n",
                    action.getStep(), action.getAction(), action.getObservation(), action.getCoveragePercentage()));
        }
        return sb.toString();
    }     

    public static CommandParams getAuthCommandParams(){
        
        return CommandParams.builder()
            .url("http://localhost:9876/")
            .delay(1000)
            .interactions(100)
            .loadWaitTime(2000)
        .build();
    }

    @Override
    public Command andThen(Command c) throws Exception {
        throw new RuntimeException("not implemented");
    }

    public String status(){
        return "SUCCEED";
    }

    @Override
    public void tearDown() throws Exception {
        
        //release resources
        browser.close();
        browser.quit();
    }

    
}
