package ie.tcd.scss.aichat.dto;

import java.util.List;

public class FlashcardResponse {
    private List<Flashcard> flashcards;
    private String warning;
    
    public FlashcardResponse(List<Flashcard> flashcards, String warning) {
        this.flashcards = flashcards;
        this.warning = warning;
    }
    
    public List<Flashcard> getFlashcards() {
        return flashcards;
    }
    
    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }
    
    public String getWarning() {
        return warning;
    }
    
    public void setWarning(String warning) {
        this.warning = warning;
    }
}