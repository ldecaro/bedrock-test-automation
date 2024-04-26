package com.example.selenium;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.selenium.command.ActionRecord;
import com.example.selenium.command.Command;
import com.example.selenium.command.CommandParams;
import com.example.selenium.command.TestCalculator;


/**
 * Hello world!
 *
 */
public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main( String[] args ){

        System.out.println("Usage: java App <url> <delay> <interactions> <load_wait_time> <test_type> <output_dir>");
        logger.info("Starting tests...");
        // if (args.length < 6) {
        //     System.out.println("Usage: java App <url> <delay> <interactions> <load_wait_time> <test_type> <output_dir>");
        //     return;
        // }

        String url = "http://localhost:9876/";
        int delay = 1000;
        int interactions = 100;
        int loadWaitTime = 1000;
        String testType = "bedrock";
        String outputDir = "C:\\Users\\luizd\\git\\genai-selenium\\target\\output";        

        url = args.length > 0 ? args[0] : url;
        delay = args.length > 1 ? Integer.parseInt(args[1]) : delay;
        interactions = args.length > 2 ? Integer.parseInt(args[2]) : interactions;
        loadWaitTime = args.length > 3 ? Integer.parseInt(args[3]) : loadWaitTime;
        testType = args.length > 4 ? args[4] : testType;
        outputDir = args.length > 5 ? args[5] : outputDir;

        List<ActionRecord> pastActions = new ArrayList<>();
        Command command = new TestCalculator();
        try {
            command.execute(CommandParams.builder()
                .url(url)
                .delay(delay)
                .interactions(interactions)
                .loadWaitTime(loadWaitTime)
                .testType(testType)
                .outputDir(outputDir)
                .pastActions(pastActions)
                .build());
            
        } catch (Exception e) {
            logger.error("Error running the test: "+e.getMessage(), e);
        }  
    }          
}
