# Bedrock Test Automation using Selenium

This project implements an automated web UI testing framework using Selenium to remotely control a browser and simulate user interactions on a site to thoroughly test it for bugs or issues. A LLM (Claude3 Sonnet) is used to implement the navigation and test the web application. LLM is aware of HTML source code, actionable elements, his past actions and remaining interactions available.

## What it does
The framework is used to automatically test a website by simulating different user flows through the site, clicking buttons, entering data and more to try to cover all possible paths and find any bugs or issues. It reads test data from JSON files to drive the automation and records each action taken to allow replaying or analyzing the full sequence of interactions.

## Amazon Q generated

This project appears to be testing a web application using Selenium. Some key things it is doing:

- It is using Selenium to automate interactions with a web browser to simulate user actions on a website. Selenium can click elements, fill out forms, etc.
- It is reading in test data from JSON files that define things like the list of interactable elements on the page, past actions taken, and more. This data is being used to drive the test automation.
- It is using JSoup to parse the HTML of pages, allowing it to find and select elements to interact with.
- It is recording each action taken in a list, so the full sequence of interactions can be replayed or analyzed.
- It supports running tests against multiple test types or frameworks by specifying the "testType" parameter, like "bedrock".
- The main method parses command line arguments to configure test parameters like the URL, delay between actions, number of interactions, etc.
- It appears the goal is to automatically test a website by simulating different user flows through the site, clicking buttons, entering data and more to try to cover all possible paths and find any bugs or issues.
- The output is being saved to JSON files, which could later be analyzed to check test coverage, find failures, or recreate the steps of a test.
In summary, this project is implementing an automated web UI testing framework using Selenium to remotely control a browser and simulate user interactions on a site to thoroughly test it for bugs or issues. Let me know if any part needs more explanation!

## Requirements
To run this project, the following are needed:

- [Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html) or higher
- [Apache Maven](https://maven.apache.org/download.cgi)
- [ChromeDriver](https://googlechromelabs.github.io/chrome-for-testing/) (or other browser driver depending on browser used for testing)
- An [AWS account](https://aws.amazon.com) with access to Amazon Bedrock and Claude3 Sonnet.
- Python 3 in case you decide to create a http server to run the sample application

## Running the tests

- Clone the project repository
- Run `mvn package` to build the application
- Authenticate your CLI to your AWS environment
- Using python3 create a simple HTTP server to run the sample application.
```
cd test-sites
python3 -m http.server 9876
```
- Run 
```
java -jar target/genai-selenium-1.0-SNAPSHOT.jar <url> <delay> <interactions> <loadWaitTime> <testType> <outputDir>
```
(Example java -jar target/genai-selenium-1.0-SNAPSHOT.jar http://localhost:9876 1000 10 1000 bedrock ./ )

## Contributing

Guideline for [contributing](CONTRIBUTING.md)

## License

This project is licensed under the [MIT-0](LICENSE) license.