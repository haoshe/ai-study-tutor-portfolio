package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.model.QuizSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.QuizSetRepository;
import ie.tcd.scss.aichat.repository.QuizQuestionRepository;
import ie.tcd.scss.aichat.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuizService {
    
    private final ChatClient chatClient;
    private final QuizSetRepository quizSetRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserRepository userRepository;
    
    public QuizService(ChatModel chatModel, QuizSetRepository quizSetRepository, 
                      QuizQuestionRepository quizQuestionRepository, UserRepository userRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.quizSetRepository = quizSetRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.userRepository = userRepository;
    }
    
    public List<QuizQuestion> generateQuiz(String studyMaterial, Integer count, String difficulty, Long userId, String title) {
        int numberOfQuestions = (count != null && count > 0) ? count : 5;
        String difficultyLevel = (difficulty != null) ? difficulty : "medium";
        
        String prompt = buildQuizPrompt(studyMaterial, numberOfQuestions, difficultyLevel);
        
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        List<QuizQuestion> questions = parseQuizQuestions(aiResponse);
        saveQuizToDatabase(questions, studyMaterial, difficultyLevel, userId, title);
        
        return questions;
    }
    
    private void saveQuizToDatabase(List<QuizQuestion> questionDTOs, String studyMaterial, String difficulty, Long userId, String title) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        QuizSet quizSet = new QuizSet();
        quizSet.setUser(user);
        quizSet.setTitle(title != null ? title : "AI Generated Quiz");
        quizSet.setStudyMaterial(studyMaterial);
        quizSet.setDifficulty(difficulty);
        
        QuizSet savedQuizSet = quizSetRepository.save(quizSet);
        
        for (int i = 0; i < questionDTOs.size(); i++) {
            QuizQuestion dto = questionDTOs.get(i);
            ie.tcd.scss.aichat.model.QuizQuestion entity = new ie.tcd.scss.aichat.model.QuizQuestion();
            entity.setQuestion(dto.getQuestion());
            
            List<String> options = dto.getOptions();
            if (options.size() >= 4) {
                entity.setOptionA(options.get(0));
                entity.setOptionB(options.get(1));
                entity.setOptionC(options.get(2));
                entity.setOptionD(options.get(3));
            }
            
            char correctLetter = (char) ('A' + dto.getCorrectAnswer());
            entity.setCorrectAnswer(String.valueOf(correctLetter));
            entity.setExplanation(dto.getExplanation());
            entity.setPosition(i);
            entity.setQuizSet(savedQuizSet);
            
            quizQuestionRepository.save(entity);
        }
    }
    
    private String buildQuizPrompt(String studyMaterial, int count, String difficulty) {
        String difficultyInstructions = getDifficultyInstructions(difficulty);
        
return String.format("""
            Generate UP TO %d multiple-choice quiz questions from the following study material.
            
            Study Material:
            %s
            
            Requirements:
            - **CRITICAL: Only create questions using information EXPLICITLY in the study material above**
            - **DO NOT use outside knowledge, general computer science concepts, or theoretical applications**
            - **DO NOT create questions about what COULD be done with the material - only about what IS in the material**
            - **CRITICAL: DO NOT generate repetitive or duplicate questions**
            - **CRITICAL: If you can only generate fewer than %d UNIQUE questions from the material, generate only as many as you can without repetition**
            - **Quality over quantity - it's better to generate 3 unique questions than 10 repetitive ones**
            - **Each question must have UNIQUE question text - no duplicate phrasing**
            - Each question should have exactly 4 options (A, B, C, D)
            - Only ONE option is correct
            - **CRITICAL: Distribute correct answers evenly across all options (A, B, C, D)**
            - **Each question should have the correct answer in a DIFFERENT position**
            - Wrong answers (distractors) must be plausible but clearly incorrect
            - Distractors should be related to the topic (not obviously wrong)
            - %s
            - Include a brief explanation for why the correct answer is right
            - If the material lacks educational content or is repetitive gibberish, return nothing
            
            Format each question EXACTLY like this:
            Q: [Your question here]
            A: [Option A]
            B: [Option B]
            C: [Option C]
            D: [Option D]
            CORRECT: [A/B/C/D]
            EXPLAIN: [Explanation of correct answer]
            
            Generate UP TO %d unique, non-repetitive questions now:
            """, count, studyMaterial, count, difficultyInstructions, count);
    }
    
    private String getDifficultyInstructions(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "Questions should test basic recall";
            case "hard" -> "Questions should require deep analysis";
            default -> "Questions should test understanding";
        };
    }
    
    private List<QuizQuestion> parseQuizQuestions(String aiResponse) {
        List<QuizQuestion> questions = new ArrayList<>();
        Pattern pattern = Pattern.compile(
            "Q:\\s*(.+?)\\s*A:\\s*(.+?)\\s*B:\\s*(.+?)\\s*C:\\s*(.+?)\\s*D:\\s*(.+?)\\s*CORRECT:\\s*([A-D])\\s*EXPLAIN:\\s*(.+?)(?=Q:|$)",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(aiResponse);
        
        while (matcher.find()) {
            String question = matcher.group(1).trim();
            List<String> options = Arrays.asList(
                matcher.group(2).trim(),
                matcher.group(3).trim(),
                matcher.group(4).trim(),
                matcher.group(5).trim()
            );
            int correctIndex = matcher.group(6).trim().charAt(0) - 'A';
            String explanation = matcher.group(7).trim();
            
            questions.add(new QuizQuestion(question, options, correctIndex, explanation));
        }
        
        return questions;
    }
}
