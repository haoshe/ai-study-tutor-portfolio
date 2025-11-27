package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.model.FlashcardSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.FlashcardRepository;
import ie.tcd.scss.aichat.repository.FlashcardSetRepository;
import ie.tcd.scss.aichat.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for generating flashcards from study material using OpenAI
 */
@Service
public class FlashcardService {

    private final ChatClient chatClient;

    // From database branch
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final UserRepository userRepository;

    // ================================
    //   CHUNKING CONSTANTS (HEAD)
    // ================================
    private static final int MAX_TOKENS_PER_CHUNK = 20000; // Safe default for GPT models
    private static final int CHARS_PER_TOKEN = 4; // Approx mapping: 1 token ≈ 4 chars

    public FlashcardService(
            ChatModel chatModel,
            FlashcardRepository flashcardRepository,
            FlashcardSetRepository flashcardSetRepository,
            UserRepository userRepository
    ) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.flashcardRepository = flashcardRepository;
        this.flashcardSetRepository = flashcardSetRepository;
        this.userRepository = userRepository;
    }

    // ============================================================
    //   TEXT CHUNKING (FROM HEAD — COMMENTS PRESERVED)
    // ============================================================
    /**
     * Split text into chunks to ensure compatibility with all OpenAI models.
     * Always chunks input for consistent behavior and quality.
     */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int maxChars = MAX_TOKENS_PER_CHUNK * CHARS_PER_TOKEN;

        if (text.length() <= maxChars) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());

            // Prefer splitting at natural boundaries
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

    // The following is the attempt to merge the chunking capabilities of feature/addPDFfield with Database.
    /**
     * Generate flashcards using AI.
     * Merged version:
     *  Uses chunking 
     *  Saves flashcards to DB 
     */
    public List<Flashcard> generateFlashcards(
            String studyMaterial,
            Integer count,
            Long userId,
            String title
    ) {
        int numberOfCards = (count != null && count > 0) ? count : 5;

        // Split into manageable chunks
        List<String> chunks = splitIntoChunks(studyMaterial);
        List<Flashcard> allFlashcards = new ArrayList<>();

        int cardsPerChunk = (int) Math.ceil((double) numberOfCards / chunks.size());
        System.out.println("Processing " + chunks.size() + " chunk(s) for " + numberOfCards + " flashcards");

        // Generate flashcards chunk by chunk
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            int cardsToGenerate =
                    (i == chunks.size() - 1)
                            ? (numberOfCards - allFlashcards.size())
                            : cardsPerChunk;

            if (cardsToGenerate <= 0) break;

            try {
                System.out.println("Generating " + cardsToGenerate + " flashcards from chunk " + (i + 1));
                List<Flashcard> chunkCards = generateFlashcardsForChunk(chunk, cardsToGenerate);
                allFlashcards.addAll(chunkCards);
            } catch (Exception e) {
                System.err.println("Error generating flashcards for chunk " + (i + 1) + ": " + e.getMessage());
            }
        }

        List<Flashcard> finalCards =
                allFlashcards.subList(0, Math.min(numberOfCards, allFlashcards.size()));

        saveFlashcardsToDatabase(finalCards, studyMaterial, userId, title);

        return finalCards;
    }

    // Single chunk generation
    private List<Flashcard> generateFlashcardsForChunk(String studyMaterial, int count) {
        String prompt = buildFlashcardPrompt(studyMaterial, count);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseFlashcards(aiResponse);
    }

    // Database save logic from database branch
    private void saveFlashcardsToDatabase(
            List<Flashcard> flashcardDTOs,
            String studyMaterial,
            Long userId,
            String title
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FlashcardSet set = new FlashcardSet();
        set.setUser(user);
        set.setTitle(title != null ? title : "AI Generated Flashcards");
        set.setStudyMaterial(studyMaterial);

        FlashcardSet savedSet = flashcardSetRepository.save(set);

        for (int i = 0; i < flashcardDTOs.size(); i++) {
            Flashcard dto = flashcardDTOs.get(i);

            ie.tcd.scss.aichat.model.Flashcard entity =
                    new ie.tcd.scss.aichat.model.Flashcard();

            entity.setFlashcardSet(savedSet);
            entity.setQuestion(dto.getQuestion());
            entity.setAnswer(dto.getAnswer());
            entity.setPosition(i);

            flashcardRepository.save(entity);
        }
    }

    //Prompt building + parsing
    private String buildFlashcardPrompt(String studyMaterial, int count) {
        return String.format("""
                Generate %d flashcards from the following study material.

                Study Material:
                %s

                Instructions:
                - Create clear, concise questions that test key concepts
                - Provide accurate, complete answers
                - Focus on the most important points
                - Make questions specific

                Format:
                Q: ...
                A: ...
                """, count, studyMaterial);
    }

    private List<Flashcard> parseFlashcards(String aiResponse) {
        List<Flashcard> flashcards = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "Q:\\s*(.+?)\\s*A:\\s*(.+?)(?=Q:|$)",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(aiResponse);

        while (matcher.find()) {
            String question = matcher.group(1).trim();
            String answer = matcher.group(2).trim();
            flashcards.add(new Flashcard(question, answer));
        }

        if (flashcards.isEmpty()) {
            flashcards = parseFlashcardsSimple(aiResponse);
        }

        return flashcards;
    }

    private List<Flashcard> parseFlashcardsSimple(String aiResponse) {
        List<Flashcard> results = new ArrayList<>();
        String[] lines = aiResponse.split("\n");

        String question = null;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("Q:")) {
                question = line.substring(2).trim();
            } else if (line.startsWith("A:") && question != null) {
                results.add(new Flashcard(question, line.substring(2).trim()));
                question = null;
            }
        }

        return results;
    }
}
