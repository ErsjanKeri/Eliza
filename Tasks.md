# 🚀 Eliza Project - Final Development Tasks

## 📋 PROJECT OVERVIEW
**Eliza** is an AI tutoring platform built with Android Compose, following NowInAndroid architecture patterns. It uses Gemma 3n model for AI inference and provides interactive learning experiences through courses, chapters, exercises, and chat functionality.

---

## 🎨 **CATEGORY 1: UI/UX DESIGN & STYLING FIXES**
*Priority: CRITICAL - User Experience*

### **Button & Theme Consistency**
- [X] **Replace all buttons with square-edged design matching main page style** - Remove any rounded corners from buttons throughout the app, ensure consistent square button styling across all features (home, chat, chapter, test interfaces), update `core:designsystem` ElizaButton component with square shape, all our buttons should have light blue background and white text, just like in the main page! 

- [X] **Fix inconsistent pinkish background usage** - Remove pinkish backgrounds from all pages except Eliza chat interface!, ensure slight blue and white theme consistency throughout the app, audit all color usage in theme files and UI components

- [X] **Completely redesign retake question UI** - Remove horrible shadows from retake question interface, redesign to match rest of application styling, ensure consistent design language with other components, simplify and clean up the retake UI elements

- [X] **Standardize app-wide color scheme** - Establish and enforce slight blue and white theme across all screens, create color guidelines documentation, remove any color inconsistencies, ensure Eliza chat responses maintain pinkish theme while everything else uses blue/white


---

## 🔧 **CATEGORY 2: CORE FUNCTIONALITY FIXES**
*Priority: CRITICAL - Broken Features*


### **UI Element Removal**
- [X] **Remove specified buttons from test results** - Delete "back to chapter", "main home", "Retake Test", and "retry exercise" buttons from test results screen, ensure navigation still works, and also clicking on the "Home" icon in the bottom bar, we navigate to the home and additionally clean up unused navigation logic

- [X] **Remove retake exercise button** - Delete retake exercise button from exercise interfaces, maintain exercise functionality through other UI flows, clean up related event handlers

- [X] Next and previous buttons while taking a test, do not have the same height! 
- [X] the take test button in chapter still its text is weird 
- [X] in window, loading model, should be instead exactly "Eliza is getting ready" 

- [X] **Hide generated question UI elements** - the problem with the generated question is that its answer is spoiled! it should not be spoiled! only after user tries it then the explanation is also displayed! currently we are kind of spoiling the test exercise!

### **Model Management**
- [ ] **Fix model switching functionality** - Implement proper model switching in ElizaModelManager, ensure selected model persists across app sessions, add loading indicators during model switching, handle model initialization correctly after switching, currently the model picker does not work on switching! upon clicking the alternative to gemma-3n-4b which is the gemma-3n-2b nothing changes! Here focus on gallery how they do it! 

### **Test System Data Flow**
- [ ] **Fix test retaking answer persistence** - Ensure test answers properly save to database when retaking tests, fix UserAnswer record updates, implement proper data flow for retaken tests, ensure "best attempt" progress tracking works correctly
This is very important! a proper data flow is what makes the app working, make sure the chat messages and sessions are also stored and created! currently there are no data updates throughout the app! 


### **Exercise Context System**
- [ ] **Implement exercise context as system prompt together with RAG** - Add question text and answer options as system prompt context for exercise help, ensure AI recognizes both question and selected answer, modify prompt engineering to include exercise context directly, remove RAG dependency for exercise context, currently I am unsure whether the RAG toggle works!

---

## 🧭 **CATEGORY 3: CHAT INTERFACE & SIDEBAR REDESIGN**
*Priority: HIGH - Core User Interface*

### **Sidebar Implementation**
- [ ] **Implement proper chat sidebar functionality** - The sidebar button is currently not showing! not sure if it works properly! 

- [ ] **Redesign top chat bar layout** - Position RAG toggle on far left (simple toggle saying "RAG enhanced"), place model picker in center, place sidebar button on far right, all in same row with proper spacing and alignment

- [ ] **Remove current RAG toggle background styling** - Replace current large background RAG component with simple toggle, clean up excessive styling, ensure minimal and clean design


### **Chat Functionality**
- [ ] **Add stop button to chat interface** - Implement stop/cancel button for ongoing AI responses, add proper response cancellation logic, provide immediate response stopping when button is pressed (search here exactly how Gallery does it! and copy them!)


- [ ] **Ensure RAG slider positioning works** - Verify RAG toggle functions properly in new left position, maintain RAG enhancement functionality, ensure toggle state persistence

---

## 🗂️ **CATEGORY 4: ARCHITECTURE & CODE CLEANUP**
*Priority: MEDIUM - Technical Debt*

### **Module Organization**
- [ ] **Move Assets from RAG to core module** - Relocate asset-related code from `:ai:rag` to appropriate `:core` module, update all import statements and dependencies, ensure no circular dependencies, test asset accessibility after move

- [ ] **Optimize common module usage** - Audit `:core:common` module and move rarely used components to appropriate modules, evaluate markdown renderer placement, reduce unnecessary dependencies, document proper module usage patterns


---

## 🌍 **CATEGORY 5: LANGUAGE SYSTEM IMPLEMENTATION**
*Priority: HIGH - Core Feature Requirement*

### **Multi-language Support**
- [ ] **Implement Albanian language support** - Add Albanian language option to language settings, ensure AI responses in Albanian, translate all app strings to Albanian, implement Albanian text rendering and formatting

- [ ] **Implement English language support** - Add English language option to language settings, ensure AI responses in English, translate all app strings to English, set English as default fallback language

- [ ] **Implement German language support** - Add German language option to language settings, ensure AI responses in German, translate all app strings to German, implement German text rendering and formatting

- [ ] **Create language selection interface** - Build language selection screen/component referenced on main page, implement language preference storage in DataStore, ensure selected language persists across app sessions

- [ ] **Integrate language with AI model responses** - Modify AI prompt generation to include selected language context, ensure all AI responses respect selected language setting, implement proper language parameter passing to AI service

### **Language System Architecture**
- [ ] **Implement language persistence system** - Store language preference in DataStore, load language setting on app startup, apply language to all app text and AI responses, handle language changes dynamically

---

## 🎥 **CATEGORY 6: VIDEO & MEDIA FEATURES**
*Priority: MEDIUM - Enhanced Features*

### **Video Message System**
- [ ] **Add video request functionality to chat** - Implement video request button in chat interface, handle video response integration in messages, create video player component for chat messages

- [ ] **Implement local video storage** - Set up local video download and storage system, handle video caching and management, implement proper error handling for video failures, ensure offline video playback capability

- [ ] **Integrate video with existing explanation system** - Connect video messages with current video explanation system, ensure proper video message display in chat, handle video loading states and errors


---

## 📝 **DEVELOPMENT NOTES**

**Critical Design Requirements:**
- Square buttons throughout (like main page)
- Blue/white theme everywhere except Eliza chat responses (pinkish)
- Clean, minimal RAG toggle design
- Three-language support (Albanian, English, German)
- Local video storage
- System prompt for exercise context (not RAG)

**Architecture Patterns:**
- Follow NowInAndroid modular architecture
- Maintain proper separation of concerns
- Use Hilt for dependency injection
- Implement proper state management with ViewModels

**Testing Priorities:**
1. Model selection and switching
2. Language system functionality
3. Chat sidebar and interface
4. Test retaking data flow
5. Exercise help system
6. Video message functionality