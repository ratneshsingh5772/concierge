# Personal Finance Concierge - Copilot Instructions

## Project Context
This is a Spring Boot application designed to track personal finance spending via natural language input.
- **Goal**: Parse natural language (e.g., "I just spent $15 on coffee") to extract amount and category, then log it.
- **Storage**: CSV-based persistence (using `opencsv`). No external database is currently configured.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 4.0.1 (WebMVC)
- **Build Tool**: Maven (use `./mvnw`)
- **Key Libraries**:
  - `lombok`: Use for boilerplate reduction.
  - `opencsv`: Use for reading/writing transaction logs.

## Architecture & Patterns
Since this is a fresh codebase, adhere to the following structure for new components:

### Package Structure
- `com.finance.concierge.controller`: REST controllers (e.g., `TransactionController`).
- `com.finance.concierge.service`: Business logic (e.g., `NaturalLanguageParser`, `TransactionService`).
- `com.finance.concierge.model`: Domain objects (e.g., `Transaction`).
- `com.finance.concierge.repository`: Data access (e.g., `CsvTransactionRepository`).

### Coding Conventions
- **Dependency Injection**: Always use Constructor Injection. Use Lombok's `@RequiredArgsConstructor` on the class.
- **Lombok**:
  - Use `@Data` for models.
  - Use `@Slf4j` for logging.
- **Configuration**:
  - Server runs on port `8081`.
  - Define file paths or constants in `application.properties`.

## Developer Workflow
- **Build**: `./mvnw clean install`
- **Run**: `./mvnw spring-boot:run`
- **Test**: `./mvnw test`

## Implementation Guidelines
- **Parsing Logic**: When implementing the parser, start with regex/string manipulation. If external AI APIs are needed later, abstract the parser behind an interface.
- **CSV Handling**: Ensure thread safety if writing to the CSV file from concurrent web requests.

## Category Management - IMPORTANT
⚠️ **Categories are PREDEFINED ONLY**
- Categories CANNOT be created, modified, or deleted by users
- Categories are global and shared across all users
- Only READ operations are allowed via `GET /api/categories`
- See `.github/copilot-category-instructions.md` for detailed rules

**Predefined Categories**: Food, Grocery, Transport, Bills, Entertainment, Shopping, Health, Education, Investment, Insurance, Credit Card Bill, Social Expense, Other

