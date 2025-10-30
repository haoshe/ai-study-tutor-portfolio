package ie.tcd.scss.aichat.controller;


import ie.tcd.scss.aichat.dto.ChatRequest;
import ie.tcd.scss.aichat.dto.ChatResponse;
import ie.tcd.scss.aichat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String response = chatService.chat(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(response, "gpt-5-nano"));
    }
}
