# Feature Course Suggestions Module

The `feature:course-suggestions` module is responsible for providing course suggestions to the user. It uses the AI tutor to generate personalized course recommendations based on the user's interests and learning goals.

## Responsibilities

- **Course Suggestions:** Generates and displays a list of recommended courses for the user.
- **AI Integration:** Integrates with the `:ai:service` module to generate the course suggestions.
- **User Preferences:** Takes the user's preferences into account when generating the suggestions.

## Dependencies

This module has the following dependencies:

- **`:core:data`:** For accessing the user's data.
- **`:core:model`:** For the course data models.
- **`:core:common`:** For shared utilities.
- **`:ai:service`:** For the course suggestion service.
- **`:ai:rag`:** For the Retrieval-Augmented Generation functionality.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous operations.

## Usage

The `courseSuggestionsScreen` is the main entry point for this feature. It can be added to the navigation graph in the `:app` module.
