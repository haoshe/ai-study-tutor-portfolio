package ie.tcd.scss.aichat.dto;

import lombok.Data;

@Data
public class ConversationRequest {
    private String sessionId;
    private String message;
}

