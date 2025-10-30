package ie.tcd.scss.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Message {
    private String role; // "user" or "assistant"
    private String content;
    private LocalDateTime timestamp;
}
