package ie.tcd.scss.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private String model;
}
