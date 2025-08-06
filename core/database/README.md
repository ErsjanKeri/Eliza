# Core Database Module

The `core:database` module is responsible for managing the application's local database. It uses the Room persistence library to provide an abstraction layer over SQLite, making it easier to work with the database while ensuring type safety and compile-time verification of SQL queries.

## Responsibilities

- **Database Definition:** Defines the `ElizaDatabase`, which is the main entry point for accessing the database.
- **Data Access Objects (DAOs):** Contains the DAOs that define the methods for interacting with the database tables.
- **Entity Definitions:** Defines the Room entities that represent the tables in the database.
- **Type Converters:** Includes type converters to allow Room to store custom data types that are not natively supported by SQLite.

## Dependencies

This module has the following dependencies:

- **`:core:common`:** For shared utilities.
- **`:core:model`:** For the application's data models.
- **`Room`:** For the database implementation.
- **`Coroutines`:** For asynchronous database operations.
- **`Serialization`:** For converting complex data types to and from a format that can be stored in the database.

## Usage

To use the database, you will typically interact with it through the repositories in the `:core:data` module. The DAOs and the `ElizaDatabase` are not meant to be accessed directly from the feature modules.

The `ElizaDatabase` is provided as a singleton using Hilt, so it can be injected into the repositories as needed.
