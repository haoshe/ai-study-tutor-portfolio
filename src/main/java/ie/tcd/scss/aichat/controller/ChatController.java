package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.ChatRequest;
import ie.tcd.scss.aichat.dto.ChatResponse;
import ie.tcd.scss.aichat.dto.ConversationRequest;
import ie.tcd.scss.aichat.dto.Message;
import ie.tcd.scss.aichat.service.ChatService;
import ie.tcd.scss.aichat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final ConversationService conversationService;
    
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String response = chatService.chat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response, "gpt-4o-mini"));
    }
    
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatService.chatStream(request.getMessage());
    }
    
    @PostMapping("/conversation")
    public ResponseEntity<ChatResponse> conversationChat(@RequestBody ConversationRequest request) {
        String response = conversationService.chatWithContext(
            request.getSessionId(), 
            request.getMessage()
        );
        return ResponseEntity.ok(new ChatResponse(response, "gpt-4o-mini"));
    }
    
    @GetMapping("/conversation/{sessionId}/history")
    public ResponseEntity<List<Message>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(conversationService.getConversationHistory(sessionId));
    }
    
    @DeleteMapping("/conversation/{sessionId}")
    public ResponseEntity<Map<String, String>> clearConversation(@PathVariable String sessionId) {
        conversationService.clearConversation(sessionId);
        return ResponseEntity.ok(Map.of("message", "Conversation cleared"));
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<Set<String>> getActiveSessions() {
        return ResponseEntity.ok(conversationService.getActiveSessions());
    }
}