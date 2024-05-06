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

        public CommandParams build() {
            if(params.url == null){
                throw new IllegalArgumentException("URL is required");
            }
            return params;
        }
    }
}
