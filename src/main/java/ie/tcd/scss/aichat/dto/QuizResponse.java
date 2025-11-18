package ie.tcd.scss.aichat.dto;

import java.util.List;

public class QuizResponse {
    private List<QuizQuestion> questions;
    private String warning;
    
    public QuizResponse(List<QuizQuestion> questions, String warning) {
        this.questions = questions;
        this.warning = warning;
    }
    
    public List<QuizQuestion> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }
    
    public String getWarning() {
        return warning;
    }
    
    public void setWarning(String warning) {
        this.warning = warning;
    }
}