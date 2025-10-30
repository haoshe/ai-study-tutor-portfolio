package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.ChatResponse;
import ie.tcd.scss.aichat.service.ModelService;
import ie.tcd.scss.aichat.service.PromptTemplateService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/advanced")
@RequiredArgsConstructor
public class AdvancedChatController {
    
    private final PromptTemplateService promptTemplateService;
    private final ModelService modelService;
    
    @PostMapping("/persona")
    public ResponseEntity<ChatResponse> chatWithPersona(@RequestBody PersonaRequest request) {
        String response = promptTemplateService.chatWithPersona(
            request.getPersona(), 
            request.getMessage()
        );
        return ResponseEntity.ok(new ChatResponse(response, "gpt-4o-mini"));
    }
    
    @PostMapping("/template")
    public ResponseEntity<ChatResponse> chatWithTemplate(@RequestBody TemplateRequest request) {
        String response = promptTemplateService.chatWithTemplate(
            request.getTemplateName(), 
            request.getVariables()
        );
        return ResponseEntity.ok(new ChatResponse(response, "gpt-4o-mini"));
    }
    
    @PostMapping("/model")
    public ResponseEntity<ChatResponse> chatWithModel(@RequestBody ModelRequest request) {
        String response = modelService.chatWithModel(
            request.getModel(),
            request.getMessage(),
            request.getTemperature()
        );
        return ResponseEntity.ok(new ChatResponse(response, request.getModel()));
    }
    
    @GetMapping("/models")
    public ResponseEntity<Map<String, String>> getAvailableModels() {
        return ResponseEntity.ok(modelService.getAvailableModels());
    }
    
    @Data
    public static class PersonaRequest {
        private String persona;
        private String message;
    }
    
    @Data
    public static class TemplateRequest {
        private String templateName;
        private Map<String, Object> variables;
    }
    
    @Data
    public static class ModelRequest {
        private String model;
        private String message;
        private Double temperature;
    }
}
