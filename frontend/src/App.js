// App.js - Main application component with authentication
import React, { useState, useEffect } from 'react';
import Auth from './components/Auth';
import StudyAssistant from './components/StudyAssistant';
import './App.css';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check if user is already logged in on mount
  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (token && storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        setUser(userData);
      } catch (error) {
        console.error('Error parsing stored user:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    
    setLoading(false);
  }, []);

  const handleLoginSuccess = (userData) => {
    setUser({
      id: userData.id,
      username: userData.username,
      email: userData.email
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  // Show loading state while checking authentication
  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  // Show login page if not authenticated
  if (!user) {
    return <Auth onLoginSuccess={handleLoginSuccess} />;
  }

  // Show study assistant if authenticated
  return (
    <div className="app">
      <header className="app-header">
        <div className="header-left">
          <div className="app-logo">
            <span className="logo-icon">📚</span>
            <span className="logo-text">Study Assistant</span>
          </div>
        </div>
        <div className="header-right">
          <div className="user-info">
            <span className="user-avatar">{user.username.charAt(0).toUpperCase()}</span>
            <span className="user-name">{user.username}</span>
          </div>
          <button onClick={handleLogout} className="logout-btn" title="Logout">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
          </button>
        </div>
      </header>
      <main className="app-main">
        <StudyAssistant userId={user.id} />
      </main>
    </div>
  );
}

export default App;