package com.example.selenium.bedrock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;
import software.amazon.awssdk.services.bedrockruntime.model.ResponseStream;

public class BedrockClient implements BedrockService {

    private static Logger logger    =   LogManager.getLogger(BedrockClient.class);

    private static final String CLAUDE_SONNET = "anthropic.claude-3-sonnet-20240229-v1:0";
    @SuppressWarnings("unused")
    private static final String CLAUDE_HAIKU = "anthropic.claude-3-haiku-20240307-v1:0";
    private BedrockRuntimeAsyncClient client = null;

    private  BedrockClientConfig config    =   null;

    public BedrockClient() {
        this(BedrockClientConfig.builder().maxTokens(200000).modelName(BedrockClient.CLAUDE_SONNET).build());
    }

    public BedrockClient(BedrockClientConfig config) {
        if(  config == null){
            throw new IllegalArgumentException("apiKey and config cannot be null");
        }
        this.config = config;

        client = BedrockRuntimeAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();        
    } 

    @Override
    public String invoke(String prompt) {
        
        JSONObject messagesApiResponse = invokeModelWithResponseStream(prompt);
        return messagesApiResponse.toString(2);
    }


    /**
     * Invokes Anthropic Claude 3 Haiku and processes the response stream.
     *
     * @param prompt The prompt for the model to complete.
     * @return A JSON object containing the complete response along with some metadata.
     */
    private JSONObject invokeModelWithResponseStream(String prompt) {

        String modelId = config.getModelName();

        // Prepare the JSON payload for the Messages API request
        var payload = new JSONObject()
                .put("anthropic_version", "bedrock-2023-05-31")
                .put("max_tokens", config.getMaxTokens())
                .put("temperature", 0.2)
                .append("messages", new JSONObject()
                        .put("role", "user")
                        .append("content", new JSONObject()
                                .put("type", "text")
                                .put("text", prompt)
                        ));

        // Create the request object using the payload and the model ID
        var request = InvokeModelWithResponseStreamRequest.builder()
                .contentType("application/json")
                .body(SdkBytes.fromUtf8String(payload.toString()))
                .modelId(modelId)
                .build();

        // Create a handler to print the stream in real-time and add metadata to a response object
        JSONObject structuredResponse = new JSONObject();
        var handler = createMessagesApiResponseStreamHandler(structuredResponse);

        // Invoke the model with the request payload and the response stream handler
        client.invokeModelWithResponseStream(request, handler).join();

        return structuredResponse;
    }

    private static InvokeModelWithResponseStreamResponseHandler createMessagesApiResponseStreamHandler(JSONObject structuredResponse) {
        AtomicReference<String> completeMessage = new AtomicReference<>("");

        Consumer<ResponseStream> responseStreamHandler = event -> event.accept(InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                .onChunk(c -> {
                    // Decode the chunk
                    var chunk = new JSONObject(c.bytes().asUtf8String());

                    // The Messages API returns different types:
                    var chunkType = chunk.getString("type");
                    if ("message_start".equals(chunkType)) {
                        // The first chunk contains information about the message role
                        String role = chunk.optJSONObject("message").optString("role");
                        structuredResponse.put("role", role);

                    } else if ("content_block_delta".equals(chunkType)) {
                        // These chunks contain the text fragments
                        var text = chunk.optJSONObject("delta").optString("text");
                        // Print the text fragment to the console ...
                        if( logger.isDebugEnabled() )
                            logger.debug(text);
                        // ... and append it to the complete message
                        completeMessage.getAndUpdate(current -> current + text);

                    } else if ("message_delta".equals(chunkType)) {
                        // This chunk contains the stop reason
                        var stopReason = chunk.optJSONObject("delta").optString("stop_reason");
                        structuredResponse.put("stop_reason", stopReason);

                    } else if ("message_stop".equals(chunkType)) {
                        // The last chunk contains the metrics
                        JSONObject metrics = chunk.optJSONObject("amazon-bedrock-invocationMetrics");
                        structuredResponse.put("metrics", new JSONObject()
                                .put("inputTokenCount", metrics.optString("inputTokenCount"))
                                .put("outputTokenCount", metrics.optString("outputTokenCount"))
                                .put("firstByteLatency", metrics.optString("firstByteLatency"))
                                .put("invocationLatency", metrics.optString("invocationLatency")));
                    }
                })
                .build());

        return InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(responseStreamHandler))
                .onComplete(() ->
                        // Add the complete message to the response object
                        structuredResponse.append("content", new JSONObject()
                                .put("type", "text")
                                .put("text", completeMessage.get())))
                .build();
    }


    public static class BedrockClientConfig {

        private int maxTokens;
        private String modelName;
        
    
        private BedrockClientConfig() {
        }

        private BedrockClientConfig(int maxTokens, String modelName) {
            this.maxTokens = maxTokens;
            this.modelName = modelName;
        }

        //getters
        public int getMaxTokens() {
            return maxTokens;
        }
       public String getModelName() {
           return modelName;
       }

        private static class BedrockClientConfigBuilder {
            private int maxTokens;
            private String modelName;

            public BedrockClientConfigBuilder() {}
            public BedrockClientConfigBuilder maxTokens(int maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }
            public BedrockClientConfigBuilder modelName(String modelName) {
               this.modelName = modelName;
                return this;
            }
            public BedrockClientConfig build() {
                return new BedrockClientConfig(this.maxTokens, this.modelName);
            }
        }
        // Getters and setters for configuration properties
        // builder method to create a BedrockClientConfig object
        public static BedrockClientConfigBuilder builder() {
            return new BedrockClientConfig.BedrockClientConfigBuilder();
        }
    }    
    

    public static void main (String args[]){

        logger.info("Starting client");
        BedrockClient client = new BedrockClient();
        String response = client.invoke("Write a haiku about the weather");
        logger.info(response);
    }
}
