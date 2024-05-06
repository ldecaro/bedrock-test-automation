package com.example.selenium;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.selenium.command.Command;
import com.example.selenium.command.Navigate;


/**
 * @author Luiz Decaro
 *
 */
public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main( String[] args ){

        System.out.println("Usage: java App <url> <delay> <interactions> <load_wait_time> <test_type> <output_dir>");
        logger.info("Starting tests...");

        String chromeDriver = null;
        if( isWindows() ){
            chromeDriver = "chromedriver.exe";
        }else{
            chromeDriver = "chromedriver";
        }

        checkDriver(chromeDriver);

        String url = "http://localhost:3000/";// "http://localhost:9876/";
        int delay = 3000;
        int interactions = 100;
        int loadWaitTime = 5000;
        String testType = "bedrock";
        String outputDir = "C:\\Users\\luizd\\git\\bedrock-test-automation\\target\\output";        

        url = args.length > 0 ? args[0] : url;
        delay = args.length > 1 ? Integer.parseInt(args[1]) : delay;
        interactions = args.length > 2 ? Integer.parseInt(args[2]) : interactions;
        loadWaitTime = args.length > 3 ? Integer.parseInt(args[3]) : loadWaitTime;
        testType = args.length > 4 ? args[4] : testType;
        outputDir = args.length > 5 ? args[5] : outputDir;

        logger.info(String.format("Using <url=%s> <delay=%s> <interactions=%s> <load_wait_time=%s> <test_type=%s> <output_dir=%s>", url, delay, interactions, loadWaitTime, testType, outputDir));


        Command command = new Navigate();
        try {
            command.execute(Navigate.getAuthCommandParams());
            
        } catch (Exception e) {
            logger.error("Error running the test: "+e.getMessage(), e);
        }  
    }    

    private static boolean isBinaryAvailable(String binaryName) {
        try {
            // Try to execute the command to find the binary
            Process process = Runtime.getRuntime().exec("which " + binaryName);
            //print the output of the command
            logger.info( new String(process.getInputStream().readAllBytes()));
            int exitCode = process.waitFor();

            // If the exit code is 0, the binary was found
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            // If any exception occurs, assume the binary is not available
            return false;
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Windows");
    }
    
    private static void checkDriver(String chromeDriver){

        if( ! isBinaryAvailable(chromeDriver) ){

            logger.info("Chrome driver is not available in the system PATH. Will check if we have a system property set with the correct location (webdriver.chrome.driver)");
            if( System.getProperty("webdriver.chrome.driver") == null ){

                logger.info("Chrome driver is not set as a system property (webdriver.chrome.driver). Setting a default location for it... ");
                System.setProperty("webdriver.chrome.driver", "C:\\Users\\luizd\\git\\bedrock-test-automation\\drivers\\123\\chromedriver-win64\\chromedriver.exe");
                logger.info("Set system property webdriver.chrome.driver with a default location of your chrome driver. Current set location is:  "+System.getProperty("webdriver.chrome.driver"));
            }else{

                logger.info("Chrome driver is available a system property (webdriver.chrome.driver) in the following location: "+System.getProperty("webdriver.chrome.driver"));
            }
        }else{
            logger.info("Chrome driver is available in the system PATH");
        }
    }
}
