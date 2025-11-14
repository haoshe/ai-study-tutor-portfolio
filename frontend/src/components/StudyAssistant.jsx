import React, { useState } from 'react';
import './StudyAssistant.css';

const API_BASE_URL = ''; // Empty because we're using proxy

function StudyAssistant() {
  const [studyMaterial, setStudyMaterial] = useState('');
  const [flashcards, setFlashcards] = useState([]);
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('flashcards');
  const [visibleAnswers, setVisibleAnswers] = useState({});
  const [userAnswers, setUserAnswers] = useState({});


    const toggleAnswer = (index) => {
      setVisibleAnswers(prev => ({
        ...prev,
        [index]: !prev[index]
      }));
    };

  // Generate Flashcards
  const generateFlashcards = async () => {
    if (!studyMaterial.trim()) {
      setError('Please enter some study material');
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
          studyMaterial: studyMaterial,
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

  const handleAnswerSelect = (questionIndex, selectedOption) => {
    setUserAnswers(prev => ({
      ...prev,
      [questionIndex]: selectedOption
    }));
  };

  // Generate Quiz
  const generateQuiz = async () => {
    if (!studyMaterial.trim()) {
      setError('Please enter some study material');
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
          studyMaterial: studyMaterial,
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

  return (
    <div className="study-assistant">
      <h1>AI Study Assistant</h1>
      
      {/* Input Section */}
      <div className="input-section">
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
    <button 
      onClick={() => toggleAnswer(index)}
      className="toggle-answer-btn"
    >
      {visibleAnswers[index] ? 'Hide Answer' : 'Answer'}
    </button>
    {visibleAnswers[index] && (
      <div className="answer">
        <strong>A:</strong> {card.answer}
      </div>
    )}
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
                      {question.options.map((option, optIndex) => {
                        const isSelected = userAnswers[index] === option;
                        const isCorrect = optIndex === question.correctAnswer;
                        const showFeedback = userAnswers[index] !== undefined;
                        
                        return (
                          <div 
                            key={optIndex} 
                            className={`option ${isSelected && showFeedback ? (isCorrect ? 'correct' : 'incorrect') : ''}`}
                            onClick={() => handleAnswerSelect(index, option)}
                            style={{ cursor: 'pointer' }}
                          >
                            <input 
                              type="radio" 
                              name={`question-${index}`} 
                              id={`q${index}-opt${optIndex}`}
                              checked={isSelected}
                              onChange={() => {}}
                            />
                            <label htmlFor={`q${index}-opt${optIndex}`} style={{ cursor: 'pointer' }}>
                              {option}
                            </label>
                            {isSelected && showFeedback && (
                              <span className="feedback">
                                {isCorrect ? (
                                  <>
                                    <span className="feedback-text">Correct</span>
                                    <span className="feedback-symbol">✓</span>
                                  </>
                                ) : (
                                  <>
                                    <span className="feedback-text">Incorrect</span>
                                    <span className="feedback-symbol">✗</span>
                                  </>
                                )}
                              </span>
                            )}
                          </div>
                        );
                      })}
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
