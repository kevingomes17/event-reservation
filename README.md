# Event Reservation REST API

## Introduction
Codebase is located in the "evtreg" folder. Instructions for Developers with Screenshots is in the "Dev Instructions - Event Reservation.pdf" as well. However, this README is more up-to-date.

## Technology
* Java Version: 1.8
* Framework: Spring Boot
* Database: H2 (In-memory)

## URLs
You might want to access the following URLs after you Launch the Application.
* REST API Basepath: http://localhost:8080/
* H2 DB Console: http://localhost:8080/h2-console
* Swagger JSON: http://localhost:8080/v2/api-docs

## Testing
* Unit Tests for the Repositories, Service & Controller are written. See Testing section for additional details.
* Integration Tests to verify appropriate handling of Race Condition(s) are written.
* There is an exported Postman Collection containing all the API endpoints that you can import and interact with the APIs.

### All Tests (Unit & Integration)
Follow the click path the screenshot below to execute all the Tests
![All Tests](/screenshots/RunAllTests.png?raw=true "All Tests")

### Race Condition Test
Follow the click path in the screenshot below to execute the Integration Tests. There are 2 tests (in the suite) that demonstrate how race condition is handled with the use of Semaphore; and the other one showing how race condition is not handled. 
![Run Integration Tests](/screenshots/RunIntegrationTests.png?raw=true "Run Integration Tests")

**Race Condition Handled**
![Race Condition Handled](/screenshots/RaceConditionHandled.png?raw=true "Race Condition Handled")

**Race Condition Not Handled**
![Race Condition Handled](/screenshots/RaceConditionNotHandled.png?raw=true "Race Condition Handled")

## API Documentation
This being a relatively small project, the code is self explanatory. However, I've integrated the appropriate library to auto generate Swagger spec v2. The JSON specification for the API is then rendered via the Swagger UI. See below screenshot. 
![Swagger V2 API Documentation](/screenshots/SwaggerEditorV2.png?raw=true "Swagger V2 API Documentation")

### Steps
* Open the online Swagger Editor by navigating to the following URL https://editor.swagger.io/
* Import the REST API Swagger v2 JSON (event-reservation-api-swagger-v2.json), included in the codebase and view the Documentation UI; where you interact with the REST APIs without having to open up postman.

