# Chat Feature Workflow & API Guide

This document outlines the working process of the chat feature after a user logs in, detailing how to interact with the messaging APIs to build a complete conversational finance experience.

## 1. Prerequisites

- **User is Logged In**: The user must have successfully authenticated via `/api/auth/login` or `/api/auth/register`.
- **JWT Token**: The frontend must possess a valid `accessToken`.
- **Authorization Header**: All requests must include:
  ```
  Authorization: Bearer <your_access_token>
  ```

## 2. The Chat Loop (Sending & Receiving)

There are two ways to send messages. **Streaming (SSE)** is recommended for a better "AI-like" user experience, while **JSON** is easier to implement.

### Option A: Real-time Streaming (Recommended)

This method allows the text to appear character-by-character as the AI generates it.

**Endpoint:** `POST /api/chat/message`
**Content-Type:** `application/json`
**Accept:** `text/event-stream`

**Request Payload:**
```json
{
  "message": "I just spent $25 on an Uber",
  "userId": "1"  // The ID of the logged-in user
}
```

**Frontend Implementation Flow:**
1.  User types a message and presses "Send".
2.  Frontend immediately adds the *user's message* to the UI chat list.
3.  Frontend initiates a `fetch` request to the endpoint.
4.  **Important**: Standard `EventSource` API does not support custom headers (Authorization). You must use `fetch` with a stream reader or a library like `@microsoft/fetch-event-source`.
5.  As data chunks arrive, append them to the *bot's message* in the UI.
6.  Connection closes when the server finishes the response.

### Option B: Standard JSON (Simpler)

This method waits for the full response before showing anything.

**Endpoint:** `POST /api/chat/message/json`
**Content-Type:** `application/json`

**Request Payload:**
```json
{
  "message": "What is my budget status?",
  "userId": "1"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Message processed successfully",
  "data": {
    "response": "You have spent $150 out of your $200 Transport budget. You have $50 remaining.",
    "userId": "1",
    "timestamp": "2026-01-02T12:00:00"
  }
}
```

**Frontend Implementation Flow:**
1.  User sends message.
2.  Frontend shows a "typing..." indicator.
3.  Frontend awaits the full API response.
4.  Once received, Frontend removes "typing..." and displays the full response.

## 3. Loading Conversation History

When the user navigates to the chat page, you should load their previous interaction history.

**Endpoint:** `GET /api/chat/history/{userId}`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 101,
      "userId": "1",
      "userMessage": "I spent $15 on coffee",
      "agentResponse": "Logged $15 to Food & Dining.",
      "createdAt": "2026-01-02T10:30:00",
      "messageType": "EXPENSE_LOG"
    },
    // ... more messages
  ]
}
```

**Frontend Implementation Flow:**
1.  On component mount (`useEffect`), call this endpoint.
2.  Store the result in the Redux state or local state.
3.  Render the list of messages.
4.  Scroll to the bottom of the chat container.

**Pagination (Optional):**
For users with long histories, use:
`GET /api/chat/history/{userId}/paginated?page=0&size=20`

## 4. Resetting Context

The AI maintains "memory" of the current conversation session. If the user wants to switch topics completely or start over, use the reset endpoint.

**Endpoint:** `POST /api/chat/reset`

**Request Payload:**
```json
{
  "userId": "1"
}
```

**Frontend Implementation Flow:**
1.  User clicks a "New Chat" or "Clear Context" button.
2.  Call the API.
3.  On success, clear the chat messages from the UI (visually).
4.  Show a system message like "Started a new conversation session."

## 5. User Statistics

To show a dashboard or summary in the sidebar.

**Endpoint:** `GET /api/chat/stats/{userId}`

**Response:**
```json
{
  "success": true,
  "data": {
    "totalMessages": 45,
    "sessionActive": true,
    "lastActivity": "2026-01-02T14:30:00"
  }
}
```

## 6. Example User Scenarios

### Scenario 1: Logging an Expense
1.  **User**: "Spent $50 at Walmart"
2.  **Frontend**: Sends to `/api/chat/message`
3.  **Backend**: Parses "$50", "Walmart" -> Categorizes as "Groceries" -> Saves to DB.
4.  **AI Response**: "I've logged an expense of $50.00 for Groceries."

### Scenario 2: Asking for Insights
1.  **User**: "How much have I spent on food?"
2.  **Frontend**: Sends to `/api/chat/message`
3.  **Backend**: Queries DB for "Food" category total.
4.  **AI Response**: "You have spent a total of $125.50 on Food this month."

### Scenario 3: Budget Check
1.  **User**: "Am I over budget?"
2.  **Frontend**: Sends to `/api/chat/message`
3.  **Backend**: Checks defined budgets vs actual spending.
4.  **AI Response**: "You are within budget for Food, but you have exceeded your Entertainment budget by $20."

## 7. Error Handling

If the API returns an error (e.g., 401 Unauthorized):
1.  **Interceptor**: Catch the 401.
2.  **Refresh**: Attempt to call `/api/auth/refresh` with the stored refresh token.
3.  **Retry**: If refresh succeeds, retry the chat request with the new token.
4.  **Logout**: If refresh fails, redirect the user to the Login page.

