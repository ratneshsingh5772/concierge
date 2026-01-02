# Postman API Guide - Finance Concierge

## Base Configuration

**Base URL:** `http://localhost:8081`

**Server Port:** 8081 (configured in application.properties)

---

## API Endpoints

### 1. Health Check (GET)

**Purpose:** Check if the service is running

**Endpoint:** `GET http://localhost:8081/api/chat/health`

**Headers:** None required

**Body:** None

**Expected Response:**
```json
{
  "status": "ok",
  "agent": "finance-concierge"
}
```

**Postman Setup:**
1. Create a new request
2. Set method to `GET`
3. Enter URL: `http://localhost:8081/api/chat/health`
4. Click **Send**

---

### 2. Send Message - JSON Response (POST) ⭐ RECOMMENDED

**Purpose:** Send a message to the finance agent and get a JSON response (track expenses, check budgets, etc.)

**Endpoint:** `POST http://localhost:8081/api/chat/message/json`

**Headers:**
```
Content-Type: application/json
```

**Request Body (JSON):**
```json
{
  "message": "I spent $15 on coffee",
  "userId": "user123"
}
```

**Note:** `userId` is optional. If not provided, defaults to "default-user"

**Response Format:** JSON

**Expected Response:**
```json
{
  "status": "success",
  "response": "I have logged your $15 coffee expense under the 'Food' category.",
  "userId": "user123",
  "timestamp": 1704211200000
}
```

**Postman Setup:**
1. Create a new request
2. Set method to `POST`
3. Enter URL: `http://localhost:8081/api/chat/message/json`
4. Go to **Headers** tab:
   - Key: `Content-Type`
   - Value: `application/json`
5. Go to **Body** tab:
   - Select `raw`
   - Select `JSON` from dropdown
   - Enter the JSON body above
6. Click **Send**

**Example Messages to Try:**

```json
{
  "message": "I spent $50 on groceries",
  "userId": "user123"
}
```

```json
{
  "message": "What's my Food budget status?",
  "userId": "user123"
}
```

```json
{
  "message": "Show me all my expenses",
  "userId": "user123"
}
```

```json
{
  "message": "Set my Transport budget to $200",
  "userId": "user123"
}
```

---

### 3. Send Message - Streaming (POST)

**Purpose:** Send a message to the finance agent with streaming response (for real-time updates)

**Endpoint:** `POST http://localhost:8081/api/chat/message`

**Headers:**
```
Content-Type: application/json
```

**Request Body (JSON):**
```json
{
  "message": "I spent $15 on coffee",
  "userId": "user123"
}
```

**Note:** `userId` is optional. If not provided, defaults to "default-user"

**Response:** Server-Sent Events (SSE) - text/event-stream

The response will stream back as text chunks. Use this endpoint for real-time streaming in the web UI.

**Postman Setup:**
1. Create a new request
2. Set method to `POST`
3. Enter URL: `http://localhost:8081/api/chat/message`
4. Go to **Headers** tab:
   - Key: `Content-Type`
   - Value: `application/json`
5. Go to **Body** tab:
   - Select `raw`
   - Select `JSON` from dropdown
   - Enter the JSON body above
6. Click **Send**

**Note:** For testing SSE, the JSON endpoint (`/message/json`) is recommended for Postman as it provides cleaner responses.

---

### 4. Reset Session (POST)

**Purpose:** Clear the conversation history for a user

**Endpoint:** `POST http://localhost:8081/api/chat/reset`

**Headers:**
```
Content-Type: application/json
```

**Request Body (JSON):**
```json
{
  "userId": "user123"
}
```

**Note:** `userId` is optional. If not provided, defaults to "default-user"

**Expected Response:**
```json
{
  "status": "success",
  "message": "Session reset successfully"
}
```

**Postman Setup:**
1. Create a new request
2. Set method to `POST`
3. Enter URL: `http://localhost:8081/api/chat/reset`
4. Go to **Headers** tab:
   - Key: `Content-Type`
   - Value: `application/json`
5. Go to **Body** tab:
   - Select `raw`
   - Select `JSON` from dropdown
   - Enter the JSON body above
6. Click **Send**

---

## Quick Start Guide

### Step 1: Ensure Server is Running
Before testing, make sure your application is running on port 8081.

Run the application:
```bash
./mvnw spring-boot:run
```

Or if you're on Windows:
```bash
mvnw.cmd spring-boot:run
```

### Step 2: Create a Postman Collection

1. Open Postman
2. Click **Collections** in the left sidebar
3. Click **New Collection**
4. Name it "Finance Concierge API"
5. Add the three requests above to this collection

### Step 3: Test the API

**Test Sequence:**

1. **First, check if server is running:**
   - Use the Health Check endpoint
   - Should return `{"status": "ok", "agent": "finance-concierge"}`

2. **Send a message to log an expense:**
   ```json
   {
     "message": "I spent $30 on lunch",
     "userId": "testuser"
   }
   ```

3. **Check your budget:**
   ```json
   {
     "message": "What's my Food budget?",
     "userId": "testuser"
   }
   ```

4. **Reset session when done:**
   ```json
   {
     "userId": "testuser"
   }
   ```

---

## Handling Server-Sent Events (SSE)

The `/message` endpoint returns Server-Sent Events, which means:

- The response is streamed in chunks
- In Postman, you'll see the response appear as plain text
- The connection stays open until the response is complete
- For better SSE testing, you might want to use tools like:
  - Browser's EventSource API
  - curl with `--no-buffer` flag
  - The web UI (index.html) included in the project

**Example with curl:**
```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{"message": "I spent $25 on coffee", "userId": "user123"}' \
  --no-buffer
```

---

## Common Issues & Troubleshooting

### Issue 1: Connection Refused
**Problem:** Cannot connect to http://localhost:8081

**Solution:**
- Verify the application is running: `./mvnw spring-boot:run`
- Check logs for any startup errors
- Ensure port 8081 is not in use by another application

### Issue 2: 404 Not Found
**Problem:** Endpoint not found

**Solution:**
- Verify the URL path is correct (starts with `/api/chat/`)
- Check that you're using the correct HTTP method (GET/POST)

### Issue 3: Empty Response
**Problem:** No response from `/message` endpoint

**Solution:**
- Check application logs for errors
- Ensure Google API key is configured in application.properties
- Verify the request body is valid JSON

### Issue 4: Jackson Deserialization Error
**Problem:** Errors related to LinkedHashMap deserialization

**Solution:**
- This is an internal issue with the tool response format
- Check the FinanceAgent.java implementation
- Ensure tool methods return proper types

---

## Environment Variables (Optional)

Create a Postman Environment for easier configuration:

1. Click **Environments** in Postman
2. Create new environment: "Finance Concierge Local"
3. Add variable:
   - **Variable:** `baseUrl`
   - **Initial Value:** `http://localhost:8081`
   - **Current Value:** `http://localhost:8081`
4. Use in requests: `{{baseUrl}}/api/chat/health`

---

## Sample Postman Collection (JSON)

You can import this directly into Postman:

```json
{
  "info": {
    "name": "Finance Concierge API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8081/api/chat/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8081",
          "path": ["api", "chat", "health"]
        }
      }
    },
    {
      "name": "Send Message (JSON Response)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"message\": \"I spent $15 on coffee\",\n  \"userId\": \"user123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8081/api/chat/message/json",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8081",
          "path": ["api", "chat", "message", "json"]
        }
      }
    },
    {
      "name": "Send Message (Streaming)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"message\": \"I spent $15 on coffee\",\n  \"userId\": \"user123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8081/api/chat/message",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8081",
          "path": ["api", "chat", "message"]
        }
      }
    },
    {
      "name": "Reset Session",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"userId\": \"user123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8081/api/chat/reset",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8081",
          "path": ["api", "chat", "reset"]
        }
      }
    }
  ]
}
```

Save this JSON to a file and import it into Postman via **Import** button.

---

## Additional Notes

- The application uses session management, so multiple users can interact independently
- Each user's conversation history is maintained in memory
- Sessions can be reset using the `/reset` endpoint
- The agent can track expenses, manage budgets, and provide financial insights
- All data is stored in `expenses.csv` in the project root directory

---

## Need Help?

Check the application logs for detailed error messages:
```bash
tail -f logs/application.log
```

Or view console output when running via Maven.

