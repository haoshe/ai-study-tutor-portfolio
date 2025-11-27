Frontend Input Source Fix, File Upload Bug Resolution & Backend 403 Diagnosis

Date: November 20, 2025
Developer: Fiachra
Branch: feature/addPDFfield

Objective

Fix multiple usability issues in the StudyAssistant component related to input source selection (Text vs File), ensure the file upload UI works as intended, improve error handling, and diagnose backend failures when generating flashcards from large PDFs (e.g., Great Expectations).

Background

The original StudyAssistant component automatically switched sources (file/text), causing persistent error states, unpredictable behaviour when switching inputs, and making it impossible to select "File Upload" unless a file was already uploaded. Additionally, users reported recurring 403 errors when interacting with backend endpoints.

Development Process
Step 1: Add Explicit Input Source Selection

Problem:
The input method (text vs file) was implicit and auto-switching, leading to:

Persistent error messages

Unexpected fallback behaviour

Confusion as to which content was used

Fix:
Added inputSource state:

const [inputSource, setInputSource] = useState('text');


Result:
Users now explicitly choose Text Input or File Upload.

Step 2: Fix File Upload Radio Button Bug (Blocker)

Issue:
The File Upload radio button was disabled until a file was uploaded:

disabled={!uploadedContent}


This made it impossible to select File Upload, making file features inaccessible.

Fix:
Removed the disabling logic:

<input
  type="radio"
  value="file"
  checked={inputSource === 'file'}
  onChange={(e) => setInputSource(e.target.value)}
/>


Result:
File Upload can now be selected anytime.

Step 3: Add File Size Validation

Fix:
Implemented a 10MB limit before the file is even sent to backend.

Result:
Prevents backend crashes, removes unnecessary API calls.

Step 4: Error State Improvements

Fixes Implemented:

Clear errors when switching between input sources

File upload failures no longer block text input

Failed file uploads automatically reset back to Text Input

Added a Clear File button to manually reset uploaded file state

Result:
Error recovery is now predictable and user-friendly.

Step 5: Updated Content Source Logic

Replaced automatic fallback logic with explicit decision:

let contentToUse = inputSource === 'file'
  ? uploadedContent
  : studyMaterial;


Result:
Users always know which content is being used.

Step 6: CSS / UI Enhancements

Added new UI sections:

Input Source Selector

File info area

Clear File button

Source notes

Improved clarity and usability.

Step 7: Comprehensive Testing Checklist

Created a 20-section testing suite including:

Input source switching

File upload flows

Size/type validation

Error recovery

Flashcards + Quiz generation

Scoring, feedback, warnings

Loading states

Edge cases

UI layout integrity

Browser console checks

Result:
Full system validation available for cross-team testing.

Step 8: Backend Work Outstanding (From Tomas Feedback)

Remaining backend tasks identified:

Improve PDF parsing reliability

Reject >10MB files at backend

Return consistent JSON response format

Add detailed error messages

Add content validation

Improve logging

Status: Still blocking full feature completion.

Step 9: Diagnose Flashcards Failing With Great Expectations

Symptom:
Flashcards failed to generate → "403 Forbidden".

Finding:
our PDF text extraction is working (you can see the warning about fonts being loaded)
But we're sending TOO MUCH text to OpenAI - Great Expectations has ~256,000 tokens, but you OpenAI limit is only 200,000 tokens per minute

Possible Solution:
Break input into multiple chunks, chunk size depending on the users key

Files Modified
frontend/src/components/StudyAssistant.jsx
frontend/src/components/StudyAssistant.css

Sign-off

Status:

Input selection system fixed

File upload UI fixed

Error handling improved

Full testing suite provided


Pending:
Fix of file upload size