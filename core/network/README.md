# Core Network Module

The `core:network` module is responsible for handling all network-related operations in the Eliza application. It provides a clean and efficient way to communicate with the backend server, abstracting the complexities of network requests from the rest of the application.

## Responsibilities

- **API Service Definitions:** Defines the Retrofit service interfaces for communicating with the backend APIs.
- **Network Client:** Provides a configured OkHttp client that can be used for making network requests.
- **Data Transfer Objects (DTOs):** Defines the data transfer objects that are used to serialize and deserialize data from the network.
- **Dependency Injection:** Provides the necessary Hilt modules for injecting the network components into other parts of the application.

## Dependencies

This module has the following dependencies:

- **`:core:common`:** For shared utilities.
- **`:core:model`:** For the application's data models.
- **`Hilt`:** For dependency injection.
- **`Coroutines`:** For asynchronous network operations.
- **`Retrofit`:** For making type-safe HTTP requests.
- **`OkHttp`:** For the underlying HTTP client.
- **`Serialization`:** For parsing JSON data.

## Usage

To make network requests from your module, you will typically interact with the repositories in the `:core:data` module, which in turn use the services provided by this module. The API services and the OkHttp client are not meant to be accessed directly from the feature modules.

The network components are provided as singletons using Hilt, so they can be injected into the repositories as needed.
