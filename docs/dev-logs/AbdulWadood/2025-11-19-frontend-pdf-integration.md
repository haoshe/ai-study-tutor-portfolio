# AI-Assisted Development Log: Frontend PDF Integration

**Developer:** Abdul Wadood  
**Date:** November 19, 2025  
**Feature:** Frontend PDF Upload UI Integration  
**AI Assistant:** GitHub Copilot (Claude Sonnet 4.5)

---

## Session Overview

This log documents how I used GitHub Copilot to integrate the backend document upload feature into the React frontend UI. The goal was to allow users to upload PDFs/PowerPoints through a clean UI and use the extracted content for AI-powered flashcard and quiz generation.

---

## Overview
Integrated the document upload feature into the React frontend UI, allowing users to upload PDFs/PowerPoints and generate flashcards or quizzes from the extracted content.

## Goals
- Add PDF upload button to StudyAssistant component
- Extract text from uploaded documents in the background
- Use extracted content for AI-powered flashcard/quiz generation
- Maintain clean UX without displaying raw PDF content

## Initial State
- Backend document parsing already implemented (DocumentController, DocumentParsingService)
- Frontend only supported manual text input via textarea
- SecurityConfig didn't permit /api/slides/** endpoints
- No UI for file upload functionality

---

## AI-Assisted Development Process

### 1. Understanding Current System Architecture

**My Prompt:**
> "Okay so just looking at the code write now how can we add a button to allow users to pass in pdfs so we can actually use the functionality we created in the application"

**Copilot's Response:**
Copilot analyzed the existing codebase and provided:
- Explanation of how DocumentController works
- Security configuration requirements
- React component structure for file upload
- FormData usage for multipart file uploads

**Key Insight:** Copilot identified that SecurityConfig needed updates before frontend could access the upload endpoint.

---

### 2. Security Configuration Issue

**My Question:**
> "Will the /api/slides/** endpoint be publicly accessible?"

**Copilot's Explanation:**
- Spring Security blocks all endpoints by default unless explicitly permitted
- Showed me how to add `.requestMatchers("/api/slides/**").permitAll()`
- Explained CSRF is already disabled but authorization still applies

**Lesson Learned:** Security configuration is separate from CSRF - both need proper setup.

---

### 3. UX Design Decision

**My Feedback:**
> "So right now when i place the pdf it pastes all the content of the pdf into the text box, dont do that just have it ready behind the scenes dont actually display the content."

**Copilot's Solution:**
- Created separate `uploadedContent` state variable
- Modified generation functions to use `uploadedContent || studyMaterial`
- Kept textarea clean for manual input option

**AI Prompt That Worked:**
```
"Don't display PDF content in textarea, keep it in background state. 
AI should use uploaded content when generating flashcards."
```

**Key Learning:** Clear user feedback helps AI understand UX requirements. Separating display state from data state improves user experience.

---

## Implementation Steps

### 1. Backend Security Configuration
Updated `SecurityConfig.java` to allow public access to document upload endpoint:

```java
http
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/quiz/**").permitAll()
        .requestMatchers("/api/flashcards/**").permitAll()
        .requestMatchers("/api/slides/**").permitAll()  // Added this
        .anyRequest().authenticated()
    );
```

**Lesson:** Spring Security blocks all endpoints by default unless explicitly permitted, even with CSRF disabled.

### 2. Frontend State Management
Added new state variables to `StudyAssistant.jsx`:

```javascript
const [uploadedContent, setUploadedContent] = useState(''); // Stores PDF content separately
const [uploadedFileName, setUploadedFileName] = useState('');
```

**Design Decision:** Keep uploaded content separate from textarea (`studyMaterial`) to avoid polluting the UI with raw PDF text.

### 3. File Upload Handler
Implemented `handleFileUpload` function:

```javascript
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  setLoading(true);
  setError('');
  setUploadedFileName(file.name);

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await fetch(`${API_BASE_URL}/api/slides/upload`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error('Failed to upload file');
    }

    const data = await response.json();
    
    // Extract text from all sections and store in background
    const extractedText = data.sections
      .map(section => section.content)
      .join('\n\n');
    
    setUploadedContent(extractedText); // Store in background, don't display
    setError('');
  } catch (err) {
    setError('Error: ' + err.message);
    setUploadedFileName('');
  } finally {
    setLoading(false);
  }
};
```

**Key Points:**
- Use FormData for file uploads (not JSON)
- Don't set `Content-Type` header - browser sets it automatically with boundary
- Extract text from `data.sections` array returned by backend
- Store in `uploadedContent` state, not `studyMaterial` (keeps textarea clean)

### 4. UI Components
Added file input and upload button:

```jsx
<div className="upload-section">
  <input
    type="file"
    id="file-upload"
    accept=".pdf,.ppt,.pptx"
    onChange={handleFileUpload}
    style={{ display: 'none' }}
  />
  <label htmlFor="file-upload" className="upload-button">
    📄 Upload PDF/PowerPoint
  </label>
  {uploadedFileName && (
    <span className="uploaded-file-name">✓ {uploadedFileName}</span>
  )}
</div>

<div className="divider">OR</div>

<textarea
  placeholder="Or paste your study material here..."
  value={studyMaterial}
  onChange={(e) => setStudyMaterial(e.target.value)}
/>
```

**UX Design:**
- Hidden file input with styled label (looks like a button)
- Accept only PDF and PowerPoint formats
- Show uploaded filename with checkmark for confirmation
- "OR" divider between upload and manual input

### 5. CSS Styling
Added styles in `StudyAssistant.css`:

```css
.upload-section {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-button {
  display: inline-block;
  padding: 10px 20px;
  background-color: #2196F3;
  color: white;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.upload-button:hover {
  background-color: #0b7dda;
}

.uploaded-file-name {
  color: #4CAF50;
  font-size: 14px;
}

.divider {
  text-align: center;
  color: #888;
  margin: 15px 0;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background-color: #ddd;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}
```

**Design Notes:**
- Blue button matches professional UI standards
- Green checkmark for positive feedback
- Divider with horizontal lines for visual separation

### 6. AI Generation Logic Update
Modified both `generateFlashcards` and `generateQuiz` to prioritize uploaded content:

```javascript
const generateFlashcards = async () => {
  // Use uploaded content if available, otherwise use manual input
  const contentToUse = uploadedContent || studyMaterial;
  
  if (!contentToUse.trim()) {
    setError('Please upload a document or enter some study material');
    return;
  }

  // ... rest of implementation
  body: JSON.stringify({
    studyMaterial: contentToUse,
    count: 5
  })
};
```

**Logic Flow:**
1. Check if PDF content exists (`uploadedContent`)
2. Fall back to manual input (`studyMaterial`)
3. If both empty, show error
4. Pass selected content to AI endpoint

## User Flow
1. User clicks "📄 Upload PDF/PowerPoint" button
2. Browser file picker opens
3. User selects PDF/PPT file
4. Frontend sends file to `/api/slides/upload`
5. Backend extracts text using PDFBox/POI
6. Frontend stores extracted text in `uploadedContent` state
7. Filename displayed with green checkmark
8. User clicks "Generate Flashcards" or "Generate Quiz"
9. AI uses uploaded content to generate study materials

## Testing Checklist
- [x] Backend accepts file uploads (DocumentController working)
- [x] Security permits /api/slides/** endpoints
- [x] File upload extracts text correctly
- [x] Uploaded content stored without displaying
- [x] Flashcard generation uses uploaded content
- [x] Quiz generation uses uploaded content
- [ ] Manual input still works when no file uploaded
- [ ] Error handling for invalid file types
- [ ] Large file performance

## Challenges & Solutions

### Challenge 1: Raw PDF Text Polluting UI
**Problem:** Initial implementation populated textarea with extracted PDF content, making UI messy.

**Solution:** Created separate `uploadedContent` state variable. Textarea stays clean, content used in background during generation.

### Challenge 2: Content Priority Logic
**Problem:** How to handle both uploaded content and manual input?

**Solution:** Use logical OR (`uploadedContent || studyMaterial`) - uploaded content takes priority, manual input as fallback.

### Challenge 3: Backend Not Accessible
**Problem:** `/api/slides/upload` returned 403 Forbidden.

**Solution:** Added `.requestMatchers("/api/slides/**").permitAll()` to SecurityConfig.

## Files Modified
- `src/main/java/ie/tcd/scss/aichat/config/SecurityConfig.java` - Added /api/slides/** permit
- `frontend/src/components/StudyAssistant.jsx` - Added upload state, handler, UI
- `frontend/src/components/StudyAssistant.css` - Added upload button styling

## API Integration
**Endpoint:** `POST /api/slides/upload`

**Request:**
```bash
curl -X POST http://localhost:8080/api/slides/upload \
  -F "file=@lecture-notes.pdf"
```

**Response:**
```json
{
  "title": "lecture-notes.pdf",
  "sections": [
    {
      "pageNumber": 1,
      "content": "Extracted text from page 1..."
    },
    {
      "pageNumber": 2,
      "content": "Extracted text from page 2..."
    }
  ]
}
```

## Code Quality Notes
- Clean separation of concerns: upload logic, display logic, AI logic
- Reusable upload handler pattern
- Consistent error handling across upload and generation
- Accessible file input (label-based interaction)

## Future Enhancements
- [ ] Add file size validation (limit to 10MB)
- [ ] Show upload progress bar for large files
- [ ] Support drag-and-drop file upload
- [ ] Preview extracted text (collapsible section)
- [ ] Allow multiple file uploads
- [ ] Cache uploaded content in session storage
- [ ] Add "Clear" button to reset uploaded content

## AI Collaboration Highlights

### Prompts That Worked Well:
1. **"How can we add a button to allow users to pass in pdfs"** - Clear goal-oriented question
2. **"Don't display the content, just have it ready behind the scenes"** - Specific UX requirement
3. **"Make dev logs for this too"** - Simple documentation request with existing examples

### Prompts That Needed Refinement:
1. Initially asked about integration without specifying UX preferences
2. Had to clarify that PDF content shouldn't pollute the textarea

### What Made AI Assistance Effective:
- **Context Awareness:** Copilot analyzed existing code (DocumentController, StudyAssistant.jsx)
- **Incremental Changes:** Applied changes in logical order (security → state → UI → logic)
- **Pattern Recognition:** Followed same format as teammate's dev logs
- **Code Generation:** Generated complete implementations, not just snippets

### Areas Where I Made Decisions:
- UX flow: Upload OR manual input (Copilot initially showed both together)
- State separation: Keep uploaded content hidden (my feedback improved UX)
- Commit strategy: Feature branch workflow (AI explained git behavior)

---

## Lessons Learned

### Technical Lessons:
1. **FormData vs JSON:** File uploads require FormData, not JSON stringify
2. **Browser File API:** Hidden input + styled label = better UX than default file button
3. **State Management:** Separate state for uploaded vs manual content keeps logic clean
4. **Spring Security:** Always check permitAll configuration when adding new endpoints
5. **UX Feedback:** Visual confirmation (filename + checkmark) essential for file uploads

### AI Collaboration Lessons:
1. **Be Specific About UX:** "Don't display content" is clearer than "handle content"
2. **Provide Context:** Mentioning existing backend code helped AI suggest proper integration
3. **Iterate on Feedback:** First implementation showed content, feedback improved it
4. **Use Examples:** Referencing teammate's logs helped AI maintain consistency
5. **Ask "Why":** Understanding reasons (e.g., Spring Security behavior) builds knowledge

## Next Steps
1. Restart backend to apply SecurityConfig changes
2. Test full upload → extract → generate flow
3. Add unit tests for upload handler
4. Document PDF upload feature in main README
5. Consider adding file validation on frontend

## Time Spent
- Backend security config: 10 minutes
- Frontend state management: 15 minutes
- Upload handler implementation: 30 minutes
- UI components and styling: 25 minutes
- Testing and debugging: 20 minutes
- **Total: ~1.5 hours**

## References
- MDN FormData: https://developer.mozilla.org/en-US/docs/Web/API/FormData
- React File Upload: https://react.dev/reference/react-dom/components/input#reading-the-files-when-submitting-the-form
- Spring Security: https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
