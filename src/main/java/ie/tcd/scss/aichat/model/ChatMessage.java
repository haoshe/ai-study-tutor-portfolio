package ie.tcd.scss.aichat.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    private String role;     // "user" or "assistant"
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private LocalDateTime timestamp = LocalDateTime.now();
}