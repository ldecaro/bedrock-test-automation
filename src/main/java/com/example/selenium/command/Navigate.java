package com.example.selenium.command;

import java.util.ArrayList;

public class Navigate extends AbstractNavigation {
    


    public static CommandParams getAuthCommandParams(){
        
        return CommandParams.builder()
            .url("http://localhost:3000/")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You are testing the authentication mechanism of this web application. Your login is bexiga@gmail.com and your password is 2024welcome1. The test case finsishes successfully once you are able to input authetication details and presented with a page containing issues for the bazinga blitz. Show me how many issues are there
            """)
        .build();
    }

    public static CommandParams getAmazonCommandParams(){
        
        return CommandParams.builder()
            .url("https://www.amazon.com/")
            .delay(3000)
            .interactions(100)
            .loadWaitTime(5000)
            .testType("bedrock")
            .outputDir("")
            .pastActions(new ArrayList<ActionRecord>())
            .testCase("""
                    You are testing the amazon.com web application. I want you to add the most expensive box of toilet paper into the shopping cart. The test cases finishes when the box of toilet paper is visible within the cart. Show the item listed in the shopping cart.
            """)
        .build();
    }    
}
