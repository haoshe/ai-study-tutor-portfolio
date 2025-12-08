package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.model.ChatMessage;
import ie.tcd.scss.aichat.model.ChatSession;
import ie.tcd.scss.aichat.service.ChatHistoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/chat/history")
@CrossOrigin(origins = "*")
public class ChatHistoryController {

    private final ChatHistoryService historyService;

    public ChatHistoryController(ChatHistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping("/session")
    public ResponseEntity<ChatSession> createSession(
            @RequestParam Long userId,
            @RequestParam(required = false) String title) {

        return ResponseEntity.ok(
                historyService.createSession(userId, title)
        );
    }

    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<ChatSession>> getSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(historyService.getSessions(userId));
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(historyService.getMessages(sessionId));
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessage> saveMessage(
            @RequestParam Long sessionId,
            @RequestParam String role,
            @RequestParam String content) {

        return ResponseEntity.ok(
                historyService.saveMessage(sessionId, role, content)
        );
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        historyService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }
}
