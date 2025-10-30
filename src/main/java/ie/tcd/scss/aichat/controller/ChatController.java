package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.ChatRequest;
import ie.tcd.scss.aichat.dto.ChatResponse;
import ie.tcd.scss.aichat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String response = chatService.chat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response, "gpt-5-nano")); // gpt-4o-mini?
    }
    
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatService.chatStream(request.getMessage());
    }
}