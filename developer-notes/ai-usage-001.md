##AI Usage Documentation
##Overview
This document details the use of generative AI tools throughout the development of the AI Chat application project (CSU33012-2526, Project 23). It covers strategies, tools, models, key prompts, and reflections on effectiveness.

Tools and Models Used
Primary AI Assistant
Tool: Claude AI (Anthropic)
Model: Claude Sonnet 4.5
Access Method: Web interface (claude.ai)
Usage Period: Throughout project development (November-December 2025)
Secondary Tools
ChatGPT: Used occasionally for alternative perspectives on technical problems
Model: GPT-4
Access Method: Web interface

###AI Usage Strategy
1. Debugging and Problem Resolution
Approach: Use AI as a first-line debugging assistant to identify root causes quickly.
When Applied:
Compilation errors with unclear error messages
Merge conflicts in GitLab
Test failures with Mockito framework
System configuration issues (file watcher limits)
Strategy:
Provide complete error logs to AI
Include relevant code context
Request explanation of root cause before solutions
Evaluate multiple solution options
Implement chosen solution independently
Verify results before proceeding
2. Test Development
Approach: Use AI to generate comprehensive test cases while maintaining understanding of test logic.
When Applied:
Creating new test suites for features
Updating existing tests after API changes
Ensuring edge case coverage
Strategy:
Provide complete implementation code
Specify exact testing requirements
Request tests with clear expected behaviors
Review generated tests for correctness
Integrate tests into existing test files
Run and verify all tests pass
3. Documentation Generation
Approach: Use AI to create structured documentation from conversational problem-solving sessions.
When Applied:
Development logs
Scrum meeting summaries
Technical reports
Git workflow documentation
Strategy:
Request specific documentation format
Provide context and requirements
Review for accuracy and completeness
Edit for project-specific details
Ensure professional formatting
4. Code Understanding and Refactoring
Approach: Use AI to explain complex code patterns and suggest improvements.
When Applied:
Understanding controller response patterns
Analyzing JSON serialization issues
Learning Mockito stubbing vs verification
Strategy:
Ask for explanations of specific patterns
Request examples of correct usage
Understand reasoning before implementing
Apply learned patterns independently

##Main Prompts and Use Cases
Debugging Session Example
Context: Compilation errors after adding Remember Me feature
Initial Prompt:
help me figure out why this code is failing
[Error logs included]
Follow-up Prompts:
"so these tests were working earlier, with earlier features, but now are failing, any ideas on why?"
"okay in login request there was a change in the constructor, there is a new addition, rememberMe, and i believe if i just add a T or F value it will mess up the remember me process"
Outcome:
Identified constructor signature mismatch
Understood backward compatibility concerns
Implemented appropriate default values
Fixed 8 compilation errors and 2 Mockito errors
Test Generation Example
Context: Need comprehensive testing for Remember Me functionality
Main Prompt:
now i want to create some tests to check that the functionality of the rememberMe feature is operational. I want you to write tests with expected return messages that show a response for when the correct response is to have a true value, when it is to have a false value, and when true is expected but false received, and when false is expected but true is received
[Complete implementation code included]
Outcome:
Generated 6 comprehensive test methods
Covered positive and negative scenarios
Included edge cases and parameter verification
All tests passed on first run (100% success rate)
Git Workflow Guidance
Context: Merge conflicts and branch management issues
Key Prompts:
"How do I check for merge conflicts before merging?"
"The merge didn't include my latest changes, why?"
"Help me resolve these GitLab inline conflicts"
Outcome:
Learned proper git workflow commands
Understood merge-base and ancestry tracking
Successfully resolved conflicts in GitLab UI
Created reusable git workflow documentation
System Configuration
Context: React development server failing to start
Main Prompt:
Error: EMFILE: too many open files, watch
[System context and error details]
Outcome:
Diagnosed file watcher limit issue
Evaluated system limits (all adequate)
Implemented polling mode solution
Created permanent .env configuration

##Reflections on AI Tool Usage
[What Worked Well]
1. Rapid Problem Diagnosis
Experience: AI consistently identified root causes of errors within the first exchange.
Example: Constructor mismatch errors were immediately diagnosed with clear explanation of why tests broke.
Value: Saved approximately 1.5-2 hours of manual debugging per session.
2. Multiple Solution Options
Experience: AI provided 2-3 solution approaches with pros/cons for each.
Example: LoginRequest constructor fix offered three approaches:
Add third parameter (chosen - fastest)
Add overloaded constructor (better backward compatibility)
Use builder pattern (most flexible)
Value: Enabled informed decision-making rather than blind implementation.
3. Test Generation Quality
Experience: Generated tests were comprehensive, well-structured, and followed best practices.
Specific strengths:
Clear test method names
Given/When/Then structure
Edge case coverage
Consistent with existing test style
Value: Created 6 tests in ~5 minutes vs ~2 hours manually, with 100% pass rate.
4. Learning Through Explanation
Experience: AI explained why solutions work, not just what to do.
Example: Detailed explanation of Mockito stubbing vs verification helped understand the framework deeply.
Value: Enhanced learning rather than just fixing immediate problems.
5. Documentation Efficiency
Experience: AI transformed conversational problem-solving into professional documentation.
Value: Generated ~10,000 words of professional documentation in ~30 minutes vs 5-6 hours manually.

[What Did Not Work Well]
1. Initial Context Gathering
Challenge: AI required significant upfront context to provide accurate solutions.
Example: For merge conflict resolution, needed to provide:
Git log output
Branch history
Diff results
Error messages
Learning: Prepare comprehensive context before starting AI session.
Mitigation: Created a checklist of information to gather before asking AI.
2. Project-Specific Details
Challenge: AI couldn't know project-specific conventions without explicit information.
Example: Test file organization strategy (separate vs consolidated files) required explaining project structure.
Learning: AI provides generic best practices; student must adapt to project context.
Mitigation: Always reviewed AI suggestions against existing codebase patterns.
3. Overly Verbose Responses
Challenge: Sometimes AI provided more explanation than necessary for simple fixes.
Example: Simple "add third parameter" fix came with extensive background on DTO design patterns.
Learning: Can request concise responses when appropriate.
Mitigation: Used follow-up prompts like "just show me the fix" when time-constrained.
4. Assumptions About Code Organization
Challenge: AI sometimes assumed code organization that didn't match actual project structure.
Example: Suggested creating separate test file when consolidation was project standard.
Learning: Always validate AI suggestions against actual project architecture.
Mitigation: Explicitly stated project preferences in prompts.
5. PDF Generation Limitation
Challenge: AI couldn't directly create PDF files for reports.
Example: Request for "make me a pdf file" resulted in Markdown that needed manual conversion.
Learning: Understand tool limitations before making requests.
Mitigation: Used browser print-to-PDF for final documentation.


##Strategies and Best Practices Developed
1. Context Provision Strategy
Approach: Always provide complete context in first prompt.
Includes:
Complete error messages (not truncated)
Relevant code files (complete, not snippets)
System information when relevant
Desired outcome explicitly stated
Rationale: Reduces back-and-forth; gets accurate answers faster.
2. Solution Evaluation Framework
Process:
Request 2-3 solution options
Evaluate pros/cons of each
Ask clarifying questions about tradeoffs
Choose solution based on project context
Implement independently
Verify with tests
Rationale: Maintains critical thinking; ensures appropriate solution for project.
3. Learning-Focused Prompting
Pattern: Always ask "why" before "how".
Examples:
"Why did these tests break?" before "How do I fix them?"
"Why is this the recommended approach?" before implementing
Rationale: Ensures understanding; builds transferable knowledge.
4. Verification Protocol
Process:
Review AI-generated code line-by-line
Understand what each line does
Test in isolation before integrating
Verify against project requirements
Check for consistency with existing code
Rationale: Catches errors; ensures code quality; maintains understanding.
5. Documentation-as-You-Go
Strategy: Request documentation generation after solving problems.
Benefits:
Creates record of decisions made
Useful for team communication
Valuable for future reference
Demonstrates understanding
Implementation: At end of each AI session, request summary documentation.

##Quantitative Impact
[Time Savings]
1. Debugging: ~1.5-2 hours saved per session
2. Test Creation: ~1.75 hours saved
3. Documentation: ~5-6 hours saved
4. Total Estimated Savings: ~8.75 hours per major feature
[Code Quality Metrics]
1. Test Pass Rate: 100% (105/105 tests passing)
2. Compilation Errors Resolved: 10
3. Test Coverage: 6 new tests for Remember Me feature
4. Documentation Generated: ~10,000 words
##Learning Outcomes
[Concepts Mastered:]
Mockito stubbing vs verification patterns
DTO backward compatibility design
Git merge conflict resolution strategies
JWT token expiration configuration
React file watcher configuration

[Comparison with Alternative Approaches]
1. Traditional Debugging (No AI)
Approach: Stack Overflow, documentation, trial-and-error
Estimated Time: 3-4 hours for Remember Me issues

Pros:
Deeper dive into documentation
More diverse perspectives (multiple Stack Overflow answers)

Cons:
Much slower
May find outdated solutions
Requires more searching and filtering
Pair Programming with Peer
[Approach: Work with another student to solve problems]
Estimated Time: 1-2 hours

Pros:
Human interaction and discussion
Shared learning experience
Accountability

Cons:
Requires coordinating schedules
May both be stuck on same issue
Limited to peer's knowledge level
[AI-Assisted (Actual Approach)]
Approach: Use Claude for diagnosis and guidance
Actual Time: ~90 minutes total

Pros:
Immediate availability
Expert-level explanations
Multiple solution options
Documentation generation

Cons:
Requires careful verification
May create dependency
Learning transfer requires intentional effort
Conclusion: AI assistance was most time-efficient while still enabling learning when used with verification and understanding protocols.

##Ethical Considerations and Academic Integrity
Appropriate Use Guidelines Followed
Transparency: Documenting all AI usage in this report
Understanding: Ensuring comprehension of all AI-provided solutions
Independence: Implementing all production code personally
Decision-Making: Making all technical choices independently
Verification: Testing and validating all AI suggestions
Boundaries Maintained
Used AI For:
✅ Debugging assistance
✅ Test generation with review
✅ Documentation creation
✅ Concept explanation
✅ Best practice guidance
Did NOT Use AI For:

❌ Algorithmic problem-solving
❌ Design decisions
❌ Architecture choices
❌ Blind copy-paste of solutions
Alignment with Industry Practice
Observation: Professional developers routinely use AI assistants (GitHub Copilot, ChatGPT, etc.)
Skill Development: Learning to use AI tools effectively is a valuable professional competency.
Academic Value: This experience mirrors real-world development practices while maintaining learning objectives.

##Future Improvements

1. Create AI prompt templates for common tasks
2. Develop project style guide to share with AI in initial prompts
3. Set up context-gathering checklist before starting AI sessions
4. Implement peer review of AI-generated code
5. Track AI usage time more precisely for better metrics
[For Team Adoption]
1. Document team AI usage guidelines
2. Share successful prompt patterns
3. Create repository of useful AI interactions
4. Establish code review process for AI-generated content
5. Define clear boundaries for appropriate AI use

##Conclusion
The strategic use of Claude AI as a development assistant proved highly valuable for this project, saving approximately 8.75 hours while maintaining code quality and supporting learning objectives. Success required:
Careful context provision to get accurate responses
Critical evaluation of all AI suggestions
Independent implementation and verification
Active learning from explanations provided
Transparent documentation of usage
The experience demonstrated that AI tools, when used thoughtfully, can enhance productivity and learning without compromising academic integrity or skill development. The key is maintaining active engagement with the material rather than passive acceptance of AI-generated solutions.

##References
AI Tools Documentation
Claude AI: https://www.anthropic.com/claude
Model: Sonnet 4.5
Access: claude.ai web interface
Technical References Learned Through AI Assistance
Mockito Documentation: https://site.mockito.org/
JWT (RFC 7519): https://tools.ietf.org/html/rfc7519
Spring Boot Testing: https://spring.io/guides/gs/testing-web/
Git Merge Strategies: https://git-scm.com/docs/git-merge
Project Context
Course: CSU33012-2526
Project: Project 23 - AI Chat Application
Team: Backend, Frontend, AI Integration, DevOps
Timeline: November-December 2025

This document was created with AI assistance for structure and formatting, but all content reflects actual usage, strategies, and reflections from the project development process.

