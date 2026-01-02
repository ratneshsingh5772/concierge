# Architecture Documentation - SOLID Principles Implementation

## Overview

This application follows **SOLID principles** and clean architecture patterns for maintainability, testability, and scalability.

## SOLID Principles Implementation

### 1. Single Responsibility Principle (SRP)
Each class has one and only one reason to change:

- **Controller Layer** (`ChatController`)
  - Only handles HTTP requests/responses
  - Delegates business logic to services
  - No direct interaction with agents or sessions

- **Service Layer** (`ChatService`, `SessionService`)
  - Contains business logic
  - No HTTP-specific code
  - Focuses on domain operations

- **Helper Classes** (`ResponseHelper`)
  - Specific utility functions for response processing
  - No business logic

- **Utility Classes** (`MessageUtils`, `SessionUtils`)
  - Pure utility functions
  - Stateless and reusable

### 2. Open/Closed Principle (OCP)
Open for extension, closed for modification:

- **Service Interfaces**: New implementations can be added without changing existing code
- **Exception Handling**: New exception types can be added to GlobalExceptionHandler
- **DTOs**: New fields can be added without breaking existing functionality

### 3. Liskov Substitution Principle (LSP)
Any implementation of an interface should be substitutable:

- `ChatServiceImpl` can be replaced with any `ChatService` implementation
- `SessionServiceImpl` can be replaced with any `SessionService` implementation
- Controllers depend on interfaces, not concrete implementations

### 4. Interface Segregation Principle (ISP)
Clients should not depend on interfaces they don't use:

- `ChatService`: Only chat-related methods
- `SessionService`: Only session-related methods
- Separate interfaces instead of one large interface

### 5. Dependency Inversion Principle (DIP)
Depend on abstractions, not concretions:

- Controller depends on `ChatService` interface, not `ChatServiceImpl`
- Controller depends on `SessionService` interface, not `SessionServiceImpl`
- Services receive dependencies via constructor injection

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         ChatController (REST API)                    │  │
│  │  - /api/chat/message (SSE)                          │  │
│  │  - /api/chat/message/json (JSON)                    │  │
│  │  - /api/chat/reset                                  │  │
│  │  - /api/chat/health                                 │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                          │
│  ┌─────────────────────┐      ┌─────────────────────────┐  │
│  │   ChatService       │      │   SessionService        │  │
│  │   (Interface)       │      │   (Interface)           │  │
│  └─────────────────────┘      └─────────────────────────┘  │
│           ↓                              ↓                  │
│  ┌─────────────────────┐      ┌─────────────────────────┐  │
│  │  ChatServiceImpl    │      │  SessionServiceImpl     │  │
│  └─────────────────────┘      └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Helper & Utility Layer                    │
│  ┌───────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ResponseHelper │  │MessageUtils  │  │SessionUtils    │  │
│  └───────────────┘  └──────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                       Domain Layer                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  FinanceAgent (Google ADK Integration)              │  │
│  │  InMemoryRunner (Configured via AgentConfig)        │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.finance.concierge/
├── common/                      # Shared components
│   └── ApiResponse.java        # Common response wrapper
│
├── config/                     # Configuration classes
│   └── AgentConfig.java       # Agent and runner configuration
│
├── controller/                 # REST API Controllers
│   └── ChatController.java    # Chat endpoints
│
├── dto/                        # Data Transfer Objects
│   ├── ChatRequestDTO.java
│   ├── ChatResponseDTO.java
│   └── SessionResetRequestDTO.java
│
├── exception/                  # Custom exceptions
│   ├── ChatServiceException.java
│   ├── SessionException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│
├── helper/                     # Helper classes
│   └── ResponseHelper.java    # Response processing helpers
│
├── service/                    # Service interfaces
│   ├── ChatService.java
│   └── SessionService.java
│
├── service/impl/              # Service implementations
│   ├── ChatServiceImpl.java
│   └── SessionServiceImpl.java
│
└── util/                      # Utility classes
    ├── MessageUtils.java
    └── SessionUtils.java
```

## Component Responsibilities

### Controllers
- **ChatController**: Handles HTTP requests, validates input, delegates to services

### Services
- **ChatService**: Business logic for chat operations
- **SessionService**: Session management and lifecycle

### DTOs (Data Transfer Objects)
- **ChatRequestDTO**: Input validation for chat requests
- **ChatResponseDTO**: Structured response for chat
- **SessionResetRequestDTO**: Input for session reset

### Common
- **ApiResponse**: Standardized response wrapper for all endpoints

### Exception Handling
- **GlobalExceptionHandler**: Centralized exception handling
- **Custom Exceptions**: Domain-specific exceptions

### Helpers
- **ResponseHelper**: Process and format agent responses

### Utilities
- **MessageUtils**: Message validation and processing
- **SessionUtils**: Session-related utility functions

## Benefits of This Architecture

### 1. Testability
- Each layer can be tested independently
- Services can be mocked in controller tests
- Business logic is separated from HTTP concerns

### 2. Maintainability
- Clear separation of concerns
- Easy to locate and modify code
- Changes in one layer don't affect others

### 3. Scalability
- New services can be added easily
- New endpoints just need to use existing services
- Easy to add new features

### 4. Reusability
- Utilities and helpers can be reused across the application
- Service interfaces can be implemented differently for different needs

### 5. Error Handling
- Centralized error handling
- Consistent error responses
- Proper logging at each layer

## API Response Structure

All JSON endpoints return a consistent response format:

```json
{
  "success": true,
  "message": "Message processed successfully",
  "data": {
    "response": "I have logged your expense...",
    "userId": "user123",
    "timestamp": 1735290000000
  },
  "timestamp": "2026-01-02T11:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Failed to process message",
  "statusCode": 500,
  "timestamp": "2026-01-02T11:30:00",
  "path": "/api/chat/message/json"
}
```

### Validation Error Response
```json
{
  "success": false,
  "error": "Validation failed",
  "data": {
    "message": "Message cannot be blank"
  },
  "statusCode": 400,
  "timestamp": "2026-01-02T11:30:00"
}
```

## Exception Hierarchy

```
Exception
  └── RuntimeException
        ├── ChatServiceException    # Chat-related errors
        └── SessionException        # Session-related errors
```

All exceptions are caught by `GlobalExceptionHandler` which:
1. Logs the error
2. Creates appropriate ApiResponse
3. Sets correct HTTP status code
4. Returns formatted JSON response

## Dependency Injection Flow

```
Application Start
    ↓
AgentConfig creates InMemoryRunner bean
    ↓
Spring injects InMemoryRunner into:
    ├── ChatServiceImpl
    └── SessionServiceImpl
    ↓
Spring injects services into:
    └── ChatController
```

## Best Practices Followed

1. **Constructor Injection**: Using `@RequiredArgsConstructor` (Lombok)
2. **Final Fields**: Immutable dependencies
3. **Interface Segregation**: Separate service interfaces
4. **DTO Pattern**: Separate request/response objects
5. **Global Exception Handling**: `@RestControllerAdvice`
6. **Validation**: Using `@Valid` and Jakarta Validation
7. **Logging**: Structured logging at each layer
8. **Utility Classes**: `@UtilityClass` for stateless utilities
9. **Builder Pattern**: Using `@Builder` for complex objects
10. **Immutability**: DTOs with `@Data` and `@Builder`

## Testing Strategy

### Unit Tests
- Test services with mocked dependencies
- Test utilities in isolation
- Test DTOs and validation

### Integration Tests
- Test controllers with mocked services
- Test complete flow with test containers

### Example Test Structure
```java
@SpringBootTest
class ChatServiceImplTest {
    @Mock
    private InMemoryRunner runner;
    
    @Mock
    private SessionService sessionService;
    
    @InjectMocks
    private ChatServiceImpl chatService;
    
    @Test
    void shouldProcessMessageSuccessfully() {
        // Given
        // When
        // Then
    }
}
```

## Future Enhancements

1. **Caching Layer**: Add Redis for session caching
2. **Rate Limiting**: Implement rate limiting per user
3. **Async Processing**: Use `@Async` for heavy operations
4. **Metrics**: Add Prometheus metrics
5. **Security**: Add authentication/authorization
6. **Database**: Replace CSV with proper database
7. **Event Sourcing**: Add event-driven architecture
8. **Circuit Breaker**: Add resilience patterns

## Configuration

### application.properties
```properties
# Google API Configuration
google.api.key=YOUR_API_KEY_HERE

# Server Configuration
server.port=8081

# Logging
logging.level.com.finance.concierge=INFO
```

## Running the Application

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Test
./mvnw test
```

## API Usage Examples

### Send Message (JSON)
```bash
curl -X POST http://localhost:8081/api/chat/message/json \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I spent $30 on lunch",
    "userId": "user123"
  }'
```

### Reset Session
```bash
curl -X POST http://localhost:8081/api/chat/reset \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123"
  }'
```

### Health Check
```bash
curl http://localhost:8081/api/chat/health
```

## Conclusion

This architecture provides a solid foundation for building scalable, maintainable, and testable Spring Boot applications. It follows industry best practices and SOLID principles to ensure high code quality and developer productivity.

