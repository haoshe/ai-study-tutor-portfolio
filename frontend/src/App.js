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
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        fontSize: '18px',
        color: '#666'
      }}>
        Loading...
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
        <div className="user-info">
          <span>Welcome, <strong>{user.username}</strong>!</span>
        </div>
        <button onClick={handleLogout} className="logout-btn">
          Logout
        </button>
      </header>
      <StudyAssistant userId={user.id} />
    </div>
  );
}

export default App;