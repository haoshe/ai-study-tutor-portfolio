# csu33012-2526-project23

# CSU33012 Group Project — AI Study Tutor

**Group 23 — 2025**

## Project Summary

This project provides an AI-powered tutoring platform that allows students to upload study materials (notes, lecture slides, PDFs, text) and receive:

* Summaries & explanations
* Automatically generated flashcards
* Practice quizzes
* Constructive feedback on understanding

The goal is to make studying interactive, adaptive, and personalized.

---

## Features

Feature: Description
Document Upload: Accepts text/PDFs containing study content.
AI Feedback Engine: Uses LLM-based processing to analyze material and generate guidance.
Flashcard Generator: Extracts key terms & concepts into flashcards.
Quiz/Test Builder: Creates practice questions based on material difficulty.
User Session History: Stores previous uploads, flashcards, and quizzes.

---

## Tech Stack

Area: Tools Used
Backend: Java, Spring Boot
Database: H2 (local) → PostgreSQL (deployment)
AI Integration: OpenAI / Spring AI API
Testing: JUnit, Mockito, Spring Test
DevOps: GitLab CI/CD, Docker, Linux
Communication: RESTful API with JSON

---

## Getting Started (Backend - Local Development)

AI QUIZ AND FLASHCARD GENERATION ONLY WORK ON OPEN AI NO OTHER AIs

1. Clone the repository:
   git clone [https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23.git](https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23.git)
   cd csu33012-2526-project23
2. Set up environment:

   * Install IDE (e.g., VSCode)
   * Get OpenAI API key
   * AI API calls require valid OPENAI_API_KEY AND ENOUGH CREDIT
   * Set as environment variable
3. Run backend:
   mvn spring-boot:run

   * Runs on port 8080

---

## How to Start the Frontend (Beginner's Guide)

### Prerequisites

* Access to Coder workspace
* Backend running on port 8080
* Add to frontend/.env: DANGEROUSLY_DISABLE_HOST_CHECK=true
* OpenAI API key set as environment variable

### Step 1: Open Terminal

* Terminal → New Terminal in VSCode/Coder
* Prompt: hshe@workspaces:~$

### Step 2: Navigate to Frontend Directory

cd ~/csu33012-2526-project23/frontend
Prompt: hshe@workspaces:~/csu33012-2526-project23/frontend$

### Step 3: Install Dependencies (First Time Only)

npm install

* Downloads required React libraries

### Step 4: Start the Frontend Server

npm start

* Wait for "Compiled successfully!"
* Local: [http://localhost:3000](http://localhost:3000)
* Keep terminal open

### Step 5: Open Frontend in Browser

Method 1: PORTS tab → find port 3000 → click URL
Method 2: Replace 8080 with 3000 in backend URL → paste in browser

### Step 6: Verify Frontend

* Title: "AI Study Assistant"
* Text box: "Enter your study material here..."
* Buttons: "Generate Flashcards" & "Generate Quiz"
* Tabs: "Flashcards (0)" & "Quiz (0)"

---

## How to Use


* AI API calls require valid OPENAI_API_KEY AND ENOUGH CREDIT
* Generate Flashcards: Paste material → Click "Generate Flashcards" → View results
* Generate Quiz: Paste material → Click "Generate Quiz" → Click Quiz tab → View questions

---

## Stop Frontend

* Ctrl+C in terminal running npm start

---

## Troubleshooting

1. "Cannot find module" → npm not installed
2. "Port 3000 in use" → Stop other instances or pkill -f node
3. "Invalid Host Header" → Ensure .env contains DANGEROUSLY_DISABLE_HOST_CHECK=true
4. Buttons fail → Backend not running
5. Blank page/errors → Stop frontend, run npm start again

---

## Quick Reference Commands

cd ~/csu33012-2526-project23/frontend
npm install  # first time only
npm start
Ctrl+C       # stop frontend
pkill -f node # if port in use

---

## Known Issues

* AI API calls require valid OPENAI_API_KEY AND ENOUGH CREDIT
* May need IDE/terminal restart for env variables

---

## Authors / Contacts

Name: Tomas Audejaitis
Email: [audejait@tcd.ie](mailto:audejait@tcd.ie)
Role: CI/CD and DevOps, README, merging
Hao She: AI creation and application
Abdul: data storage and file upload
Ngo Hung: backend
Fiachra: Frontend
