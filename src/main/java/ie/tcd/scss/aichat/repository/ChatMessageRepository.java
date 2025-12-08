package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
    
    @Modifying
    void deleteBySessionId(Long sessionId);
}