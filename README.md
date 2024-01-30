# Math Expression API Evaluator

## User Story

As a developer, I want a Java program to concurrently evaluate mathematical expressions using a web API. The program should efficiently distribute requests, enforce a rate limit, and provide clear results.

## Project Structure

- `src/main/java/org/example/MathExpressionAPIEvaluator.java`: Contains the main program logic for evaluating expressions.
- `src/main/java/org/example/Client.java`: Represents a client making API requests.
- `src/main/java/org/example/MathExpressionAPIEvaluator.RateLimiter.java`: Manages the rate of API requests.

## Technology Used

- **Java**: Programming language used for the implementation.
- **Math.js API**: Web API (https://api.mathjs.org/v4/) used for mathematical expression evaluation.
- **ExecutorService**: Utilized for concurrent processing of expression evaluations.
- **File Input/Output**: Read expressions from a file and print results.
- **Rate Limiting**: Implemented a rate limiter to control the number of API requests.

## Rate Limiting

The program includes a rate limiter to control the number of requests made to the API. It uses a token-based approach, ensuring a maximum of 500 requests per second.
