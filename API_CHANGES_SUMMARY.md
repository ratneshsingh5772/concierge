# API Changes Summary - JSON Response Support

## Changes Made

### 1. New Endpoint Added: `/api/chat/message/json`

A new endpoint has been added that returns JSON responses instead of Server-Sent Events (SSE).

#### Endpoint Details:
- **URL:** `POST http://localhost:8081/api/chat/message/json`
- **Content-Type:** `application/json`
- **Response Type:** JSON (not SSE)

#### Request Format:
```json
{
  "message": "I spent $15 on coffee",
  "userId": "user123"
}
```

#### Response Format:
```json
{
  "status": "success",
  "response": "I have logged your $15 coffee expense under the 'Food' category.",
  "userId": "user123",
  "timestamp": 1704211200000
}
```

### 2. Original Endpoint Remains: `/api/chat/message`

The original streaming endpoint is still available for use with the web UI.

- **URL:** `POST http://localhost:8081/api/chat/message`
- **Response Type:** Server-Sent Events (SSE) - `text/event-stream`

### 3. New Response Class Added

A new `ChatResponse` class has been added:

```java
@Data
public static class ChatResponse {
    private String status;
    private String response;
    private String userId;
    private long timestamp;
}
```

## Benefits

✅ **Easy Postman Testing:** JSON responses are much easier to work with in Postman
✅ **Better Integration:** Easier to integrate with other services expecting JSON
✅ **Structured Responses:** Standardized response format with status, response, userId, and timestamp
✅ **Error Handling:** Proper error responses in JSON format
✅ **Backwards Compatible:** Original SSE endpoint still works for web UI

## Usage Examples

### Example 1: Log an Expense

**Request:**
```bash
curl -X POST http://localhost:8081/api/chat/message/json \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I spent $30 on lunch",
    "userId": "user123"
  }'
```

**Response:**
```json
{
  "status": "success",
  "response": "I have logged your $30 lunch expense under the 'Food' category.",
  "userId": "user123",
  "timestamp": 1735290000000
}
```

### Example 2: Check Budget

**Request:**
```bash
curl -X POST http://localhost:8081/api/chat/message/json \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What is my Food budget status?",
    "userId": "user123"
  }'
```

**Response:**
```json
{
  "status": "success",
  "response": "You have spent $45.00 out of $500.00 on Food. Remaining: $455.00.",
  "userId": "user123",
  "timestamp": 1735290100000
}
```

### Example 3: Error Handling

**Response (on error):**
```json
{
  "status": "error",
  "response": "An error occurred: Connection timeout",
  "userId": "user123",
  "timestamp": 1735290200000
}
```

## Migration Guide

### For Postman Users

**Old Way (SSE - harder to test):**
```
POST http://localhost:8081/api/chat/message
```
Response comes as streaming text.

**New Way (JSON - recommended):**
```
POST http://localhost:8081/api/chat/message/json
```
Response comes as structured JSON object.

### For API Integrations

If you're integrating this API with another service:

1. **Use the JSON endpoint:** `/api/chat/message/json`
2. **Parse the response:** Extract the `response` field for the actual message
3. **Check status:** Always check the `status` field ("success" or "error")
4. **Use timestamp:** The `timestamp` field can be used for logging or tracking

## Files Modified

1. **ChatController.java** - Added new `sendMessageJson()` method and `ChatResponse` class
2. **POSTMAN_GUIDE.md** - Updated with instructions for the new JSON endpoint
3. **Removed** - `run.sh` (unnecessary file)

## Testing the New Endpoint

### Using Postman:

1. Create a new POST request
2. URL: `http://localhost:8081/api/chat/message/json`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
   ```json
   {
     "message": "I spent $20 on coffee",
     "userId": "testuser"
   }
   ```
5. Click Send
6. You should see a clean JSON response!

### Using curl:

```bash
curl -X POST http://localhost:8081/api/chat/message/json \
  -H "Content-Type: application/json" \
  -d '{"message": "I spent $20 on coffee", "userId": "testuser"}'
```

## Next Steps

1. **Test the new endpoint** using Postman or curl
2. **Update any integrations** to use the JSON endpoint
3. **Use the streaming endpoint** only for the web UI where real-time updates are needed

## Questions?

Check the updated **POSTMAN_GUIDE.md** for detailed instructions and examples.

