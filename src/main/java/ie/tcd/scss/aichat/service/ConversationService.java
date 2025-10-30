package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {
    
    private final ChatClient chatClient;
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    
    public ConversationService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    public String chatWithContext(String sessionId, String userMessage) {
        // Get or create conversation history
        List<Message> history = conversations.computeIfAbsent(
            sessionId, 
            k -> new ArrayList<>()
        );
        
        // Add user message to history
        history.add(new Message("user", userMessage, LocalDateTime.now()));
        
        // Build messages list for AI
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        
        // Add system message
        messages.add(new SystemMessage("You are a helpful AI assistant. Maintain context from previous messages in this conversation."));
        
        // Add conversation history
        for (Message msg : history) {
            if (msg.getRole().equals("user")) {
                messages.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole().equals("assistant")) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        
        // Get AI response
        String response = chatClient.prompt(new Prompt(messages))
                .call()
                .content();
        
        // Add assistant response to history
        history.add(new Message("assistant", response, LocalDateTime.now()));
        
        return response;
    }
    
    public List<Message> getConversationHistory(String sessionId) {
        return conversations.getOrDefault(sessionId, Collections.emptyList());
    }
    
    public void clearConversation(String sessionId) {
        conversations.remove(sessionId);
    }
    
    public Set<String> getActiveSessions() {
        return conversations.keySet();
    }
}
