Devlog: Flashcard and Quiz Controller Updates
Date: 2025-11-24
 Author: Tomas Audejaitis
 Project: AI Chat – Flashcard & Quiz APIs

1. Overview
During development of the Flashcard and Quiz APIs, I encountered several issues related to JSON responses and type mismatches. This devlog documents the investigation, fixes applied, and outcomes.

2. Issues Encountered
2.1 FlashcardController
Initial implementation used a FlashcardResponse class that did not exist.


Method signature expected ResponseEntity<List<Flashcard>>, causing this compilation error:


Type mismatch: cannot convert from ResponseEntity<FlashcardResponse> to ResponseEntity<List<Flashcard>>

Unit tests failed with JSONPath errors:


Expected to find an object with property ['flashcards'] in path $ but found JSONArray

Analysis: Returning a raw List<Flashcard> serializes as a JSON array ([...]), but tests expected a JSON object containing a "flashcards" field.

2.2 QuizController
Similar problem: returning a raw List<QuizQuestion> from generateQuiz().


Test failures:


Expected to find an object with property ['questions'] in path $ but found JSONArray

Analysis: Again, returning a raw list → JSON array, but tests expect a JSON object with a "questions" key.

3. Solution Approach
Wrap the lists (flashcards or questions) in a Map<String,Object> before returning.


Include optional "warning" messages if the number of generated items is less than requested.


Adjust method return types as needed to match ResponseEntity<Map<String,Object>> or ResponseEntity<?>.


Preserve existing validation, authentication, and error handling logic.



4. Implementation Details
4.1 FlashcardController
Wrapped the flashcards list in a map with an optional warning:


Map<String, Object> response = new HashMap<>();
response.put("flashcards", flashcards);
if (warning != null) {
    response.put("warning", warning);
}
return ResponseEntity.ok(response);

Method signature updated:


public ResponseEntity<Map<String, Object>> generateFlashcards(...)

Outcome: JSON now matches test expectations ($.flashcards), preserving the response variable.

4.2 QuizController
Wrapped questions list in a map:


Map<String, Object> response = new HashMap<>();
response.put("questions", questions);

if (questionCount != null && questions.size() < questionCount) {
    response.put("warning", String.format(
        "You requested %d questions but only %d could be generated.",
        questionCount, questions.size()
    ));
}
return ResponseEntity.ok(response);

Return type: ResponseEntity<?>


Outcome: JSON now matches test expectations ($.questions). Optional warnings included if fewer questions than requested.

5. Challenges
Ensuring backward compatibility with tests expecting specific JSON structure.


Preserving the optional warning logic while converting from a raw list to a JSON object.


Maintaining proper handling of unauthenticated requests (default user ID) without breaking service calls.



6. Results
FlashcardController and QuizController now produce JSON objects, not arrays.


Unit tests referencing $.flashcards and $.questions now pass.


No additional DTOs required.


Optional warnings provide useful feedback to the user if fewer items are generated than requested.



7. Lessons Learned
Returning raw lists in Spring Boot APIs can cause subtle JSONPath test failures when the test expects a named object field.


Using a Map<String,Object> as a wrapper is an effective and simple solution to match expected JSON structures without creating extra DTO classes.


Clearly documenting changes in devlogs helps maintain traceability between issues, fixes, and outcomes.



8. Next Steps
Consider creating optional DTOs for flashcards and quizzes to formalize API responses.


Refactor repeated map-wrapping logic into a helper method for cleaner code.


Add integration tests to cover optional warnings and edge cases with fewer generated items.




