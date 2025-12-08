package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.model.ChatMessage;
import ie.tcd.scss.aichat.model.ChatSession;
import ie.tcd.scss.aichat.repository.ChatMessageRepository;
import ie.tcd.scss.aichat.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    public ChatHistoryService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
    }

    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "New Chat");
        return sessionRepo.save(session);
    }

    public List<ChatSession> getSessions(Long userId) {
        return sessionRepo.findByUserId(userId);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        messageRepo.deleteBySessionId(sessionId);
        sessionRepo.deleteById(sessionId);
    }

    public ChatMessage saveMessage(Long sessionId, String role, String content) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        ChatMessage msg = new ChatMessage();
        msg.setSession(session);
        msg.setRole(role);
        msg.setContent(content);
        return messageRepo.save(msg);
    }

    public List<ChatMessage> getMessages(Long sessionId) {
        return messageRepo.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}