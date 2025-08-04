# 🚀 Eliza Project - Final Development Tasks

## 📋 PROJECT OVERVIEW
**Eliza** is an AI tutoring platform built with Android Compose, following NowInAndroid architecture patterns. It uses Gemma 3n model for AI inference and provides interactive learning experiences through courses, chapters, exercises, and chat functionality.

## **VIDEO functionality** 
- [ ] make sure that the entire video functionality works and the implementation is seamless, currently the video functionality is not tested but the infrastructure seems reasonable 


## 🗂️ **ARCHITECTURE & CODE CLEANUP** (last thing to do)
*Priority: MEDIUM - Technical Debt*

### **Module Organization**
- [ ] **Move Assets from RAG to core module** - Relocate asset-related code from `:ai:rag` to appropriate `:core` module, update all import statements and dependencies, ensure no circular dependencies, test asset accessibility after move

- [ ] **Optimize common module usage** - Audit `:core:common` module and move rarely used components to appropriate modules, evaluate markdown renderer placement, reduce unnecessary dependencies, document proper module usage patterns


---



## 🌍 **LANGUAGE SYSTEM IMPLEMENTATION**
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


### **Test System Data Flow LAST TASK** DATA FLOW is a huge task, this will be final as it takes a lot of time! 
- [ ] **Fix test retaking answer persistence** - Ensure test answers properly save to database when retaking tests, fix UserAnswer record updates, implement proper data flow for retaken tests, ensure "best attempt" progress tracking works correctly
This is very important! a proper data flow is what makes the app working, make sure the chat messages and sessions are also stored and created! currently there are no data updates throughout the app! 

### **CRITICAL: Video Chat Message Persistence** 
- [ ] **Implement complete video message persistence** - Currently video messages are LOST when navigating away from chat because they only exist in local UI state. Need to:
  - Store ChatMessageVideo and ChatMessageVideoRequest in database
  - Link video files to chat sessions for retrieval
  - Restore video messages when returning to chat sessions
  - Track video metadata (duration, file size, local path) in database
  - Handle video file cleanup when chat sessions are deleted
This makes video explanations actually usable - without this, users lose access to videos immediately upon navigation! 



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