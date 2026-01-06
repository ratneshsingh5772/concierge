# Expense AI Parser Service Documentation

## Overview
The `ExpenseAIParserService` is a Spring Boot service that parses natural language expense messages using AI (Google Gemini) with regex-based fallback for reliability.

## Architecture

### Components

1. **ExpenseAIParserService** (Interface)
   - Defines contract for expense parsing
   - Follows Interface Segregation Principle (ISP)

2. **ExpenseAIParserServiceImpl** (Implementation)
   - Implements AI-based parsing with Google Gemini
   - Falls back to regex parsing if AI fails
   - Follows Single Responsibility Principle (SRP)

3. **ParsedExpenseDTO**
   - Data Transfer Object for parsed expense data
   - Contains: amount, currency, category, description, confidence

4. **AIExpenseResponseDTO**
   - DTO for deserializing AI JSON responses
   - Maps AI output to Java objects

5. **ExpenseParsingHelper**
   - Utility functions for parsing operations
   - Currency extraction, amount validation, etc.

6. **CategoryMappingUtil**
   - Maps keywords to categories
   - Extensible category mapping system

## How It Works

### 1. Parsing Flow

```
User Message → AI Parser → Success? → Return Result
                     ↓
                   Failure
                     ↓
              Regex Fallback → Return Result
```

### 2. AI Parsing (Primary)

The service sends a structured prompt to Google Gemini AI:

**System Prompt:**
```
You are an AI expense parser. Extract:
1. Amount (numeric value)
2. Currency (default USD)
3. Category (Food, Transport, Bills, Entertainment, etc.)
4. Description
5. Confidence (0.0 to 1.0)

Return JSON format:
{
  "amount": 15.50,
  "currency": "USD",
  "category": "Food",
  "description": "Coffee at Starbucks",
  "confidence": 0.95,
  "parsed_successfully": true
}
```

**Example Interactions:**

| User Message | Extracted Data |
|-------------|----------------|
| "I spent $15 on coffee" | amount: 15.00, category: Food, description: "Coffee purchase" |
| "Paid $50 for Uber" | amount: 50.00, category: Transport, description: "Taxi ride" |
| "Bought groceries for $75" | amount: 75.00, category: Food, description: "Grocery shopping" |
| "Movie tickets $25" | amount: 25.00, category: Entertainment, description: "Movie tickets" |

### 3. Regex Fallback (Secondary)

If AI parsing fails, the service uses regex patterns:

- **Amount Pattern:** `\$?([\d]+\.?[\d]*)`
- **Category Pattern:** Matches keywords like "food", "transport", "uber", "coffee", etc.
- **Category Mapping:** Uses CategoryMappingUtil to map keywords

### 4. Category Validation

All parsed categories are validated against the database:
- Checks if category exists in `categories` table
- Uses case-insensitive matching
- Falls back to "Food" if invalid

## Code Examples

### Basic Usage

```java
@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseAIParserService aiParserService;
    
    public void processExpenseMessage(String message) {
        // Parse the expense
        ParsedExpenseDTO parsed = aiParserService.parseExpense(message);
        
        if (parsed.isParsingSuccessful()) {
            System.out.println("Amount: " + parsed.getAmount());
            System.out.println("Category: " + parsed.getCategory());
            System.out.println("Description: " + parsed.getDescription());
            System.out.println("Confidence: " + parsed.getConfidence());
        } else {
            System.out.println("Failed to parse: " + parsed.getDescription());
        }
    }
}
```

### With User Context

```java
public void processWithContext(String message, Long userId) {
    ParsedExpenseDTO parsed = aiParserService.parseExpenseWithContext(message, userId);
    // Process the result...
}
```

### Integration with Expense Creation

```java
@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseAIParserService aiParserService;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    public Expense createExpenseFromMessage(String message, Long userId) {
        // Parse the message
        ParsedExpenseDTO parsed = aiParserService.parseExpense(message);
        
        if (!parsed.isParsingSuccessful()) {
            throw new IllegalArgumentException("Could not parse expense message");
        }
        
        // Find user and category
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findByNameIgnoreCase(parsed.getCategory())
            .orElseThrow(() -> new RuntimeException("Invalid category"));
        
        // Create expense
        Expense expense = Expense.builder()
            .user(user)
            .category(category)
            .amount(parsed.getAmount())
            .currency(parsed.getCurrency())
            .description(parsed.getDescription())
            .originalMessage(message)
            .expenseDate(LocalDate.now())
            .aiParsed(true)
            .aiConfidence(parsed.getConfidence())
            .build();
        
        return expenseRepository.save(expense);
    }
}
```

## Configuration

### Application Properties

```properties
# Google AI Configuration
google.api.key=${GOOGLE_API_KEY}
google.ai.model=gemini-2.0-flash-exp

# Or use gemini-pro for production
# google.ai.model=gemini-pro
```

### Environment Variables

```bash
export GOOGLE_API_KEY=your_actual_api_key_here
```

## Testing

### Unit Test Example

```java
@SpringBootTest
class ExpenseAIParserServiceTest {
    
    @Autowired
    private ExpenseAIParserService parserService;
    
    @Test
    void testCoffeeParsing() {
        String message = "I spent $15 on coffee";
        ParsedExpenseDTO result = parserService.parseExpense(message);
        
        assertTrue(result.isParsingSuccessful());
        assertEquals(new BigDecimal("15.00"), result.getAmount());
        assertEquals("Food", result.getCategory());
        assertEquals("USD", result.getCurrency());
    }
    
    @Test
    void testUberParsing() {
        String message = "Paid $50 for Uber ride";
        ParsedExpenseDTO result = parserService.parseExpense(message);
        
        assertTrue(result.isParsingSuccessful());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals("Transport", result.getCategory());
    }
    
    @Test
    void testInvalidMessage() {
        String message = "Hello world";
        ParsedExpenseDTO result = parserService.parseExpense(message);
        
        assertFalse(result.isParsingSuccessful());
    }
}
```

## Future Enhancements

### 1. Real Google Gemini Integration

Replace the mock implementation with actual Google AI SDK:

```java
private String callGeminiAPI(String systemPrompt, String userPrompt) {
    // Initialize Gemini client
    GenerativeModel model = new GenerativeModel(modelName, googleApiKey);
    
    // Combine prompts
    String fullPrompt = systemPrompt + "\n\nUser: " + userPrompt;
    
    // Generate response
    GenerateContentResponse response = model.generateContent(fullPrompt);
    
    return response.getText();
}
```

### 2. Context-Aware Parsing

Use user's historical spending patterns:

```java
@Override
public ParsedExpenseDTO parseExpenseWithContext(String userMessage, Long userId) {
    // Get user's common categories
    List<String> commonCategories = getUserCommonCategories(userId);
    
    // Adjust AI prompt with user context
    String enhancedPrompt = buildContextualPrompt(userMessage, commonCategories);
    
    return parseWithAI(enhancedPrompt);
}
```

### 3. Multi-Currency Support

Enhance currency detection and conversion:

```java
private BigDecimal convertToUSD(BigDecimal amount, String currency) {
    if ("USD".equals(currency)) return amount;
    
    // Call currency conversion API
    BigDecimal rate = exchangeRateService.getRate(currency, "USD");
    return amount.multiply(rate);
}
```

### 4. Batch Processing

Support multiple expenses in one message:

```java
public List<ParsedExpenseDTO> parseMultipleExpenses(String message) {
    // Split message into individual expenses
    // Parse each one separately
    // Return list of parsed expenses
}
```

## Error Handling

The service handles errors gracefully:

1. **AI Failure:** Falls back to regex parsing
2. **Invalid JSON:** Catches parsing exceptions
3. **Missing Data:** Returns failed result with error message
4. **Invalid Category:** Attempts normalization, falls back to default

## Performance Considerations

- **AI Latency:** Google Gemini calls can take 1-3 seconds
- **Fallback Speed:** Regex parsing is instant
- **Caching:** Consider caching common patterns
- **Async Processing:** For bulk operations, use async processing

## Security

- **API Key Protection:** Store in environment variables
- **Input Validation:** Sanitize user messages
- **Rate Limiting:** Implement rate limits for AI calls
- **Data Privacy:** Don't log sensitive expense details

## Logging

The service provides comprehensive logging:

```
INFO  - Parsing expense from message: I spent $15 on coffee
DEBUG - System Prompt: You are an AI expense parser...
DEBUG - User Prompt: Parse this expense message: "I spent $15 on coffee"
DEBUG - AI Response: {"amount": 15.00, "currency": "USD"...}
INFO  - Successfully parsed with AI: ParsedExpenseDTO(amount=15.00...)
```

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## Support

For issues or questions:
- Check logs for detailed error messages
- Verify Google API key is configured
- Ensure categories exist in database
- Test with simple messages first

## License

MIT License - See LICENSE file for details

