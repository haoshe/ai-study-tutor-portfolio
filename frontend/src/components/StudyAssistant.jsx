import React, { useState } from 'react';
import './StudyAssistant.css';

const API_BASE_URL = ''; // Empty because we're using proxy

function StudyAssistant() {
  const [studyMaterial, setStudyMaterial] = useState('');
  const [uploadedContent, setUploadedContent] = useState(''); // Stores PDF content separately
  const [flashcards, setFlashcards] = useState([]);
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('flashcards');
  const [uploadedFileName, setUploadedFileName] = useState('');

  // Generate Flashcards
  const generateFlashcards = async () => {
    // Use uploaded content if available, otherwise use manual input
    const contentToUse = uploadedContent || studyMaterial;
    
    if (!contentToUse.trim()) {
      setError('Please upload a document or enter some study material');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      const response = await fetch(`${API_BASE_URL}/api/flashcards/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          studyMaterial: contentToUse,
          count: 5
        })
      });

      if (!response.ok) {
        throw new Error('Failed to generate flashcards');
      }

      const data = await response.json();
      setFlashcards(data);
      setError('');
    } catch (err) {
      setError('Error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Generate Quiz
  const generateQuiz = async () => {
    // Use uploaded content if available, otherwise use manual input
    const contentToUse = uploadedContent || studyMaterial;
    
    if (!contentToUse.trim()) {
      setError('Please upload a document or enter some study material');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      const response = await fetch(`${API_BASE_URL}/api/quiz/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          studyMaterial: contentToUse,
          questionCount: 5,
          difficulty: 'MEDIUM'
        })
      });

      if (!response.ok) {
        throw new Error('Failed to generate quiz');
      }

      const data = await response.json();
      setQuizzes(data);
      setError('');
    } catch (err) {
      setError('Error: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Upload PDF/PPT Document
  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Check file type
    const validTypes = ['application/pdf', 'application/vnd.ms-powerpoint', 
                        'application/vnd.openxmlformats-officedocument.presentationml.presentation'];
    if (!validTypes.includes(file.type)) {
      setError('Please upload a PDF or PowerPoint file');
      return;
    }

    setLoading(true);
    setError('');
    setUploadedFileName(file.name);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(`${API_BASE_URL}/api/slides/upload`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error('Failed to upload document');
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

  return (
    <div className="study-assistant">
      <h1>AI Study Assistant</h1>
      
      {/* Input Section */}
      <div className="input-section">
        {/* File Upload */}
        <div className="upload-section">
          <label htmlFor="file-upload" className="upload-button">
            📄 Upload PDF/PowerPoint
          </label>
          <input
            id="file-upload"
            type="file"
            accept=".pdf,.ppt,.pptx"
            onChange={handleFileUpload}
            style={{ display: 'none' }}
            disabled={loading}
          />
          {uploadedFileName && (
            <span className="uploaded-file-name">✓ {uploadedFileName}</span>
          )}
        </div>

        <div className="divider">OR</div>

        <textarea
          placeholder="Enter your study material here..."
          value={studyMaterial}
          onChange={(e) => setStudyMaterial(e.target.value)}
          rows={8}
        />
        
        <div className="button-group">
          <button onClick={generateFlashcards} disabled={loading}>
            {loading && activeTab === 'flashcards' ? 'Generating...' : 'Generate Flashcards'}
          </button>
          <button onClick={generateQuiz} disabled={loading}>
            {loading && activeTab === 'quiz' ? 'Generating...' : 'Generate Quiz'}
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}
      </div>

      {/* Results Section */}
      <div className="results-section">
        <div className="tabs">
          <button 
            className={activeTab === 'flashcards' ? 'active' : ''}
            onClick={() => setActiveTab('flashcards')}
          >
            Flashcards ({flashcards.length})
          </button>
          <button 
            className={activeTab === 'quiz' ? 'active' : ''}
            onClick={() => setActiveTab('quiz')}
          >
            Quiz ({quizzes.length})
          </button>
        </div>

        {/* Flashcards Display */}
        {activeTab === 'flashcards' && (
          <div className="flashcards-container">
            {flashcards.length === 0 ? (
              <p className="no-results">No flashcards generated yet</p>
            ) : (
              flashcards.map((card, index) => (
                <div key={index} className="flashcard">
                  <div className="question">
                    <strong>Q:</strong> {card.question}
                  </div>
                  <div className="answer">
                    <strong>A:</strong> {card.answer}
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Quiz Display */}
        {activeTab === 'quiz' && (
          <div className="quiz-container">
            {quizzes.length === 0 ? (
              <p className="no-results">No quiz generated yet</p>
            ) : (
              quizzes.map((question, index) => (
                <div key={index} className="quiz-question">
                  <h3>Question {index + 1}</h3>
                  <p className="question-text">{question.question}</p>
                  <div className="options">
                    {question.options.map((option, optIndex) => (
                      <div key={optIndex} className="option">
                        <input 
                          type="radio" 
                          name={`question-${index}`} 
                          id={`q${index}-opt${optIndex}`}
                        />
                        <label htmlFor={`q${index}-opt${optIndex}`}>{option}</label>
                      </div>
                    ))}
                  </div>
                  <div className="correct-answer">
                    <strong>Correct Answer:</strong> {question.correctAnswer}
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default StudyAssistant;
