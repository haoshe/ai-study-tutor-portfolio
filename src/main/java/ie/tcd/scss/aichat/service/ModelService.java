package ie.tcd.scss.aichat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ModelService {
    
    private final ChatModel defaultChatModel;
    
    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;
    
    public ModelService(ChatModel chatModel) {
        this.defaultChatModel = chatModel;
    }
    
    public String chatWithModel(String modelName, String userMessage, Double temperature) {
        ChatClient.Builder builder = ChatClient.builder(defaultChatModel);
        
        // Configure options based on model
        Map<String, Object> options = Map.of(
            "model", modelName,
            "temperature", temperature != null ? temperature : 0.7
        );
        
        return builder.build()
                .prompt()
                .user(userMessage)
                .options(OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(temperature != null ? temperature : 0.7)
                        .build())
                .call()
                .content();
    }
    
    public Map<String, String> getAvailableModels() {
        return Map.of(
            "gpt-4o", "GPT-4o - Most capable",
            "gpt-4o-mini", "GPT-4o Mini - Fast and efficient",
            "gpt-4-turbo", "GPT-4 Turbo - Balanced performance",
            "gpt-3.5-turbo", "GPT-3.5 Turbo - Legacy model"
        );
    }
}