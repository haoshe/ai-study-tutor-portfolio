package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.Message;
import ie.tcd.scss.aichat.model.ChatMessage;
import ie.tcd.scss.aichat.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConversationService {
    
    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;
    
    public ConversationService(ChatModel chatModel, ChatMessageRepository chatMessageRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.chatMessageRepository = chatMessageRepository;
    }
    
    public String chatWithContext(String sessionId, String userMessage) {
        // Load conversation history from database
        List<ChatMessage> dbMessages = Collections.emptyList();
        try {
            Long sessionIdLong = Long.parseLong(sessionId);
            dbMessages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionIdLong);
        } catch (NumberFormatException e) {
            // If sessionId is not a valid Long, continue with empty history
        }
        
        // Build messages list for AI
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        
        // Add system message
        messages.add(new SystemMessage(
            "You are a helpful AI study assistant. " +
            "Help users understand their study materials, answer questions, and explain concepts clearly. " +
            "Maintain context from previous messages in this conversation."
        ));
        
        // Add conversation history from database
        for (ChatMessage msg : dbMessages) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        
        // Add the current user message
        messages.add(new UserMessage(userMessage));
        
        // Get AI response
        String response = chatClient.prompt(new Prompt(messages))
                .call()
                .content();
        
        return response;
    }
    
    public List<Message> getConversationHistory(String sessionId) {
        try {
            Long sessionIdLong = Long.parseLong(sessionId);
            List<ChatMessage> dbMessages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionIdLong);
            List<Message> messages = new ArrayList<>();
            for (ChatMessage msg : dbMessages) {
                messages.add(new Message(msg.getRole(), msg.getContent(), msg.getTimestamp()));
            }
            return messages;
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }
    
    public void clearConversation(String sessionId) {
        // No-op: deletion is handled by ChatHistoryService
    }
    
    public Set<String> getActiveSessions() {
        return Collections.emptySet();
    }
}