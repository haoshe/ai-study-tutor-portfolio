package ie.tcd.scss.aichat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptTemplateService {
    
    private final ChatClient chatClient;
    
    public PromptTemplateService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    public String chatWithPersona(String persona, String userMessage) {
        String systemPrompt = getSystemPromptForPersona(persona);
        
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
    
    public String chatWithTemplate(String templateName, Map<String, Object> variables) {
        String template = getTemplate(templateName);
        String prompt = fillTemplate(template, variables);
        
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
    
    private String getSystemPromptForPersona(String persona) {
        return switch (persona.toLowerCase()) {
            case "teacher" -> 
                "You are a patient and knowledgeable teacher. " +
                "Explain concepts clearly with examples. " +
                "Break down complex topics into simple steps.";
            
            case "code_reviewer" -> 
                "You are an experienced code reviewer. " +
                "Analyze code for bugs, performance issues, and best practices. " +
                "Provide constructive feedback with specific suggestions.";
            
            case "translator" -> 
                "You are a professional translator. " +
                "Translate text accurately while preserving tone and context. " +
                "Explain cultural nuances when relevant.";
            
            case "creative_writer" -> 
                "You are a creative writer with vivid imagination. " +
                "Write engaging stories with rich descriptions. " +
                "Use literary techniques to captivate readers.";
            
            default -> 
                "You are a helpful AI assistant.";
        };
    }
    
    private String getTemplate(String templateName) {
        return switch (templateName) {
            case "code_explanation" -> 
                "Explain the following code:\n\n{code}\n\n" +
                "Focus on: {focus}";
            
            case "email_draft" -> 
                "Draft a {tone} email about: {topic}\n" +
                "Recipient: {recipient}";
            
            case "summary" -> 
                "Summarize the following text in {length} words:\n\n{text}";
            
            case "brainstorm" -> 
                "Generate {count} creative ideas for: {topic}\n" +
                "Target audience: {audience}";
            
            default -> "{message}";
        };
    }
    
    private String fillTemplate(String template, Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", 
                                   entry.getValue().toString());
        }
        return result;
    }
}
