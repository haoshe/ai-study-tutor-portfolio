package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.model.QuizSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.QuizSetRepository;
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
    private static final int MAX_TOKENS_PER_CHUNK = 20000; // Safe universal default for all GPT models
    private static final int CHARS_PER_TOKEN = 4; // Rough estimate: 1 token ≈ 4 chars
    private final QuizSetRepository quizSetRepository;
    private final UserRepository userRepository;
    
    public QuizService(ChatModel chatModel, QuizSetRepository quizSetRepository, 
                      UserRepository userRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.quizSetRepository = quizSetRepository;
        this.userRepository = userRepository;
    }

    /**
     * Split text into chunks to ensure compatibility with all OpenAI models
     * Always chunks input for consistent behavior and quality
     */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int maxChars = MAX_TOKENS_PER_CHUNK * CHARS_PER_TOKEN;
        
        // If text is small enough, return as single chunk
        if (text.length() <= maxChars) {
            chunks.add(text);
            return chunks;
        }
        
        // Split into chunks at natural boundaries
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            
            // Try to break at paragraph or sentence boundary
            if (end < text.length()) {
                int lastParagraph = text.lastIndexOf("\n\n", end);
                int lastSentence = text.lastIndexOf(". ", end);
                
                if (lastParagraph > start + (maxChars / 2)) {
                    end = lastParagraph + 2;
                } else if (lastSentence > start + (maxChars / 2)) {
                    end = lastSentence + 2;
                }
            }
            
            chunks.add(text.substring(start, end));
            start = end;
        }
        
        return chunks;
    }
    
    /**
     * This merged version:
     *  ✔ Uses chunking for large documents (HEAD)
     *  ✔ Saves quiz + questions to database (database branch)
     *  ✔ Supports difficulty + userId + title
     */
    public List<QuizQuestion> generateQuiz(
            String studyMaterial,
            Integer count,
            String difficulty,
            Long userId,
            String title
    ) {

        int numberOfQuestions = (count != null && count > 0) ? count : 5;
        String difficultyLevel = (difficulty != null) ? difficulty : "medium";

        // Split text into manageable chunks (HEAD feature)
        List<String> chunks = splitIntoChunks(studyMaterial);
        List<QuizQuestion> allQuestions = new ArrayList<>();

        // Equally distribute question generation across chunks
        int questionsPerChunk = (int) Math.ceil((double) numberOfQuestions / chunks.size());

        System.out.println("Processing " + chunks.size() + " chunk(s) for " + numberOfQuestions + " questions.");

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            // Last chunk receives remaining questions
            int toGenerate = (i == chunks.size() - 1)
                    ? (numberOfQuestions - allQuestions.size())
                    : questionsPerChunk;

            if (toGenerate <= 0) break;

            try {
                System.out.println("Generating " + toGenerate + " questions from chunk " + (i + 1));
                String prompt = buildQuizPrompt(chunk, toGenerate, difficultyLevel);

                String aiResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                List<QuizQuestion> chunkQuestions = parseQuizQuestions(aiResponse);
                allQuestions.addAll(chunkQuestions);

                if (allQuestions.size() >= numberOfQuestions) break;

            } catch (Exception e) {
                System.err.println("Error generating quiz for chunk " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Trim to the requested number
        List<QuizQuestion> finalQuestions =
                allQuestions.subList(0, Math.min(numberOfQuestions, allQuestions.size()));

        // Save the full quiz to the database
        saveQuizToDatabase(finalQuestions, studyMaterial, difficultyLevel, userId, title);

        return finalQuestions;
    }

    
    private void saveQuizToDatabase(List<QuizQuestion> questionDTOs, String studyMaterial, String difficulty, Long userId, String title) {
        System.out.println("=== SAVING QUIZ TO DATABASE ===");
        System.out.println("Number of questions to save: " + questionDTOs.size());
        
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        QuizSet quizSet = new QuizSet();
        quizSet.setUser(user);
        quizSet.setTitle(title != null ? title : "AI Generated Quiz");
        quizSet.setStudyMaterial(studyMaterial);
        quizSet.setDifficulty(difficulty);
        
        // Create question entities and establish bi-directional relationship
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
            entity.setQuizSet(quizSet);  // Set parent reference
            
            quizSet.getQuestions().add(entity);  // Add to parent's collection
            
            System.out.println("Added question " + i + ": " + dto.getQuestion().substring(0, Math.min(50, dto.getQuestion().length())));
        }
        
        System.out.println("Total questions in quiz: " + quizSet.getQuestions().size());
        
        // Save the set with cascade - will automatically save all questions
        QuizSet savedSet = quizSetRepository.save(quizSet);
        
        System.out.println("QuizSet saved with ID: " + savedSet.getId());
        System.out.println("=== END SAVING QUIZ ===");
    }
    
    private String buildQuizPrompt(String studyMaterial, int count, String difficulty) {
        String difficultyInstructions = getDifficultyInstructions(difficulty);
        
        return String.format("""
                Generate %d multiple-choice quiz questions from the following study material.
                
                Study Material:
                %s
                
                Requirements:
                - Each question should have exactly 4 options (A, B, C, D)
                - Only ONE option is correct
                - %s
                
                Format each question EXACTLY like this:
                Q: [Your question here]
                A: [Option A]
                B: [Option B]
                C: [Option C]
                D: [Option D]
                CORRECT: [A/B/C/D]
                EXPLAIN: [Explanation]
                
                Generate %d questions now:
                """, count, studyMaterial, difficultyInstructions, count);
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
