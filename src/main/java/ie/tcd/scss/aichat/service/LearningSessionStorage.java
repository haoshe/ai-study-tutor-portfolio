package ie.tcd.scss.aichat.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import ie.tcd.scss.aichat.dto.LearningSession;

@Service
public class LearningSessionStorage {
    private final Map<String, LearningSession> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new learning session and stores it in memory.
     *
     * @param userId The ID of the user (hardcoded for MVP)
     * @param documentContent The extracted text content from the PDF
     * @return The created learning session
     */
    public LearningSession createSession(String userId, String documentContent) {
        String sessionId = UUID.randomUUID().toString();
        LearningSession session = LearningSession.builder()
                .id(sessionId)
                .userId(userId)
                .documentContent(documentContent)
                .build();
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * Retrieves a session by its ID.
     *
     * @param sessionId The ID of the session to retrieve
     * @return The session if found
     */
    public Optional<LearningSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Updates an existing session.
     *
     * @param session The session to update
     */
    public void updateSession(LearningSession session) {
        sessions.put(session.getId(), session);
    }

    /**
     * Deletes a session.
     *
     * @param sessionId The ID of the session to delete
     */
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Retrieves all sessions for a user.
     *
     * @param userId The ID of the user
     * @return A map of session IDs to sessions
     */
    public Map<String, LearningSession> getUserSessions(String userId) {
        Map<String, LearningSession> userSessions = new ConcurrentHashMap<>();
        sessions.forEach((id, session) -> {
            if (session.getUserId().equals(userId)) {
                userSessions.put(id, session);
            }
        });
        return userSessions;
    }
}