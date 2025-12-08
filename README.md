# README for Project 23 - AI Study Tutor

##Project Summary
This project provides an AI-powered tutoring platform that allows students to upload study materials (notes, lecture slides, PDFs, text) and receive:
Summaries & explanations
Automatically generated flashcards
Practice quizzes
Constructive feedback on understanding
The goal is to make studying interactive, adaptive, and personalized. The program is designed to aid students in theri studies by reducing the amount of prep time required to create study materials. Flashcards and quizzes have been proven to increase memory retention by forcing the brain to recall information learned. Our AI Study Assistant uses these techniques to help students study, without needing the time to create quizzes or flashcards of their own.



## 1. Build and Run
Clone the repository: git clone https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23.git cd csu33012-2526-project23


Set up environment:


Open Coder with new workspace using repository link.
Get OpenAI API key
AI API calls require valid OPENAI_API_KEY AND ENOUGH CREDIT
Set as environmental variable
Database Setup (MySQL)


Before starting the backend, run this script to create the database, user, and permissions:
Make the setup script executable:
chmod +x setup-database.sh
Run the script:
./setup-database.sh
This script will: create the aichat_db database create the MySQL user aichat_user with password aichat_pass grant all required privileges
Run backend:


run these commands in terminal so your .env file will be read by the program
 source .env 
mvn clean install 
source .env


then run this line to start the backend


mvn spring-boot:run


you should see a message like:
Runs on port 8080
optional: check the 'ports' tab to see that this port is up and running


##How to Start the Frontend (Beginner's Guide)
[Prerequisites:]
Access to Coder workspace
Backend running on port 8080
Create a .env file in the frontend folder
Add to frontend/.env: 
DANGEROUSLY_DISABLE_HOST_CHECK=true
CHOKIDAR_USEPOLLING=true
OpenAI API key set as an environment variable

[Step 1: Open Terminal]
Terminal → New Terminal in VSCode/Coder
Prompt: <your_Username> @workspaces:~$

[Step 2: Navigate to Frontend Directory]
cd /csu33012-2526-project23/frontend 
Prompt: <your_Username>@workspaces:/csu33012-2526-project23/frontend$

[Step 3: Install Dependencies (First Time Only)]
In Terminal: npm install
(Downloads required React libraries)

[Step 4: Start the Frontend Server]
In Terminal:: npm start
Wait for "Compiled successfully!"
Local: http://localhost:3000
Keep terminal open
This should automatically open the program in your browser

[If program does not open]Step 5: Open Frontend in Browser]
Method 1: PORTS tab → find port 3000 → click URL 
Method 2: Replace 8080 with 3000 in backend URL → paste in browser

##How to Use
AI API calls require valid OPENAI_API_KEY AND ENOUGH CREDIT
Upload a file: pptx, .pdf, or take some text and put it into the text box
Generate Flashcards: Click "Generate Flashcards" → View results
Generate Quiz:Click "Generate Quiz" → Click Quiz tab → View questions
Chat with the Ai Assistant about uploaded documents and data


## 2. Videos
Backend/ Code understanding Video
https://filesender2.heanet.ie/?s=download&token=4653c929-06a2-4465-8e31-164645cd13c8
Frontend/User Story Video:


## 3. Developer Diary
https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23/-/blob/5bcff5a8ee9996b5c085dfec7334bc0afa045def/developer-notes/diary.md

## 4. Notes on AI Use
Ai-usage-001 (Group/Tomas):
https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23/-/blob/d4d0284bc46d49c499983d025f1999adccd22bfe/developer-notes/ai-usage-001.md
Ai-usage-002 (Hao She):
https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23/-/blob/1ff1c7e7d1767a2510d2c12fe6cec01630ff8d59/developer-notes/ai-usage-002
AI-usage-003 (Abdul Rehan):
https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23/-/blob/f7d278ab45482bf58645bca3a08fcf01e8b4be13/developer-notes/ai-usage-003.md

## 5. Tests
* Where to find your tests and how to run them.
Tests are located in:
Src>Test>java/ie/tcd/scss/aichat
https://gitlab.scss.tcd.ie/csu33012-2526-group23/csu33012-2526-project23/-/tree/main/src/test/java?ref_type=heads

Run all Tests with the command: mvn clean test


## 6. Other Notes on Project
* Issues were created on the project timeline, have been resolved
All Developer updates on pushed code have been added to the project under:
docs>devlogs
Each developer has their own folder with devlogs for major changes they made.

## 7. Team
### Active
Abdul Wadood Rehan: rehana@tcd.ie
Anh Ngo Hung: ango@tcd.ie 
Fiachra Tobin: tobinfi@tcd.ie 
Hao She: hshe@tcd.ie
Tomas Vytautas Audejaitis: audejait@tcd.ie
### Inactive
*N/A
