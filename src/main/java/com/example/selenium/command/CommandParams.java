package com.example.selenium.command;

import java.util.ArrayList;
import java.util.List;

public class CommandParams {
    
    private String url  =   null;
    private Integer delay = 1000;
    private Integer interactions = 100;
    private Integer loadWaitTime = 1000;
    private String testType = "bedrock";
    private String outputDir = "C:\\Users\\luizd\\git\\genai-selenium\\target\\output";
    private List<ActionRecord> pastActions = new ArrayList<>();
    private String testCase = "";
    private Boolean setIds = Boolean.FALSE;

    private CommandParams() {
    }

    //getters
    public String getUrl() {
        return url;
    }
    public Integer getDelay() {
        return delay;
    }
    public Integer getInteractions() {
        return interactions;
    }
    public Integer getLoadWaitTime() {
        return loadWaitTime;
    }
    public String getTestType() {
        return testType;
    }
    public String getOutputDir() {
        return outputDir;
    }
    public List<ActionRecord> getPastActions() {
        return pastActions;
    }
    public String getTestCase() {
        return testCase;
    }
    public Boolean setIds() {
        return setIds;
    }

    //builder pattern to create CommandParams using fluent language
    public static Builder builder() {
        return new Builder();
    }

    //builder pattern to create CommandParams using fluent language
    public static class Builder {
        private CommandParams params = new CommandParams();

        public Builder url(String url) {
            params.url = url;
            return this;
        }

        public Builder delay(Integer delay) {
            params.delay = delay;
            return this;
        }

        public Builder interactions(Integer interactions) {
            params.interactions = interactions;
            return this;
        }

        public Builder loadWaitTime(Integer loadWaitTime) {
            params.loadWaitTime = loadWaitTime;
            return this;
        }

        public Builder testType(String testType) {
            params.testType = testType;
            return this;
        }

        public Builder outputDir(String outputDir) {
            params.outputDir = outputDir;
            return this;
        }

        public Builder pastActions(List<ActionRecord> pastActions) {
            params.pastActions = pastActions;
            return this;
        }
        public Builder testCase(String testCase) {
            params.testCase = testCase;
            return this;
        }
        public Builder setIds(Boolean setIds) {
            params.setIds = setIds;
            return this;
        }

        public CommandParams build() {
            if(params.url == null){
                throw new IllegalArgumentException("URL is required");
            }
            return params;
        }
    }

    public static CommandParams getLoginFFQ(){
        
        return CommandParams.builder()
            .url("URL")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .setIds(Boolean.TRUE)
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You need to authenticate in an Okta application. Athentication has 3 steps. Input username (myID) and use USERNAME. Input password and use PASSWORD. Then choose a second factor verification and choose Get a push notification. The test finishes successfully when you are able to request a push notification. The test fails if you cannot authenticate after 10 retries or if you cannot find an action after 2 retries.
            """) // when you authenticate and you land on an insurance quoting application or Farmers Insurance FastQuote application
        .build();
    }       

    public static CommandParams getFFQQuote(){
        
        return CommandParams.builder()
            .url("URL")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .setIds(Boolean.TRUE)
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You are testing the an insurance quoting app. 
                    You will create a new insurance quote (get a new quote) for a Business.
                    Click on product type Business. 
                    Use zip code 92646.
                    Click on start a new quote.
                    Generate values for input fields and submit request for a new quote.  
                    The test case finishes successfully once you are able to request a new quote for a business.
                    If you receive an error page explaining we are not available or if you could not reach the quote submission form for some reason the test fails.
            """)
        .build();
    }    

    public static CommandParams getAuthCommandParams(){
        
        return CommandParams.builder()
            .url("http://localhost:3000/")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .setIds(Boolean.TRUE)
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You are testing the authentication mechanism of this web application. Your login is EMAIL and your password is PASSWORD. Click Sign in button then input email and continue and input password and continue. The test case finishes successfully once you are able to input authetication details, login and visualize a page with a list of Backlogs. Tell me how much backlog exists for Bazinga Blitz.
            """)
        .build();
    }
    // presented with a page containing issues for the Bazinga Blitz. Show me how many issues are there

    public static CommandParams getTestAmazonShoppingCartParams(){
        
        return CommandParams.builder()
            .url("https://www.amazon.com/")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .setIds(Boolean.FALSE)
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You are testing the amazon.com web application. Your test case is to add to cart the most expensive pen. The test case finishes when the pen is visible within the cart. You should monitor the number of items in the cart.
            """)
        .build();
    }    

    public static CommandParams getWalmartCommandParams(){
        
        return CommandParams.builder()
            .url("https://www.walmart.com/")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You are testing an e-commerce web application. I want you to add the most expensive box of toilet paper into the shopping cart. You search for the product and add it to the shopping cart. The test cases finishes when the box of toilet paper is visible within the cart. Show the item listed in the shopping cart.
            """)
        .build();
    }        

    public static CommandParams getSolveCaptchaParams(){

        return CommandParams.builder()
            .url("https://www.amazon.com/")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    Human: Answer the following captcha. Your answer should output ONLY the value of the captcha
                    Assitant: The answer to the captcha is 
            """)
        .build();
    }    
}
