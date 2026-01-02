# Frontend Integration Guide & AI Prompt

This document serves as a comprehensive guide and prompt for building the frontend of the **Finance Concierge** application. It includes the project structure, API specifications, and a ready-to-use prompt for AI code generation.

## 1. Tech Stack Requirements

- **Runtime/Package Manager**: [Bun](https://bun.sh/)
- **Framework**: [React 19](https://react.dev/)
- **Build Tool**: [Vite](https://vitejs.dev/)
- **State Management**: [Redux Toolkit](https://redux-toolkit.js.org/) (RTK) + RTK Query (optional but recommended)
- **Styling**: [Tailwind CSS](https://tailwindcss.com/)
- **HTTP Client**: Axios (recommended for interceptors) or Fetch
- **Routing**: React Router v7

## 2. Backend API Details

**Base URL**: `http://localhost:8081`

### Authentication (JWT)
The backend uses stateless JWT authentication.
- **Access Token**: Expires in 24h. Send in header: `Authorization: Bearer <token>`
- **Refresh Token**: Expires in 7 days. Used to get new access tokens.

#### Endpoints

| Method | Endpoint | Description | Payload / Params |
|--------|----------|-------------|------------------|
| `POST` | `/api/auth/register` | Register new user | `{ email, password, firstName, lastName, phoneNumber }` (Username is auto-generated from email) |
| `POST` | `/api/auth/login` | Login | `{ usernameOrEmail, password }` |
| `POST` | `/api/auth/refresh` | Refresh Token | `{ refreshToken }` |
| `GET` | `/api/auth/me` | Get Profile | Headers: `Authorization: Bearer <token>` |
| `POST` | `/api/auth/logout` | Logout | Headers: `Authorization: Bearer <token>` |

### Chat & Finance
| Method | Endpoint | Description | Payload / Params |
|--------|----------|-------------|------------------|
| `POST` | `/api/chat/message` | Send Message (SSE) | `{ message, userId }` (Returns `text/event-stream`) |
| `POST` | `/api/chat/message/json` | Send Message (JSON) | `{ message, userId }` |
| `GET` | `/api/chat/history/{userId}` | Get History | Path param: `userId` |
| `GET` | `/api/chat/stats/{userId}` | Get Stats | Path param: `userId` |
| `POST` | `/api/chat/reset` | Reset Session | `{ userId }` |

## 3. Recommended Project Structure

```
src/
├── app/
│   ├── store.ts          # Redux store configuration
│   └── hooks.ts          # Typed useSelector/useDispatch
├── assets/
├── components/
│   ├── common/           # Reusable UI components (Button, Input, Card)
│   ├── layout/           # Layout components (Navbar, Sidebar, ProtectedRoute)
│   └── chat/             # Chat specific components (MessageBubble, ChatInput)
├── features/
│   ├── auth/
│   │   ├── authSlice.ts  # Redux slice for auth (user, token, status)
│   │   ├── authAPI.ts    # API calls for auth
│   │   ├── LoginPage.tsx
│   │   └── RegisterPage.tsx
│   └── chat/
│       ├── chatSlice.ts  # Redux slice for chat history
│       └── ChatPage.tsx
├── services/
│   ├── api.ts            # Axios instance with interceptors
│   └── sse.ts            # Helper for Server-Sent Events
├── utils/
│   └── validation.ts
├── App.tsx
└── main.tsx
```

## 4. Implementation Strategy

### Step 1: API Client & Interceptors
Create an Axios instance that:
1.  Attaches the `Authorization` header automatically if a token exists in the Redux store or localStorage.
2.  Intercepts `401 Unauthorized` responses to attempt a token refresh using `/api/auth/refresh`.
3.  Logs the user out if refresh fails.

### Step 2: Redux Auth Slice
-   **State**: `user` (object), `accessToken` (string), `isAuthenticated` (boolean), `loading` (boolean), `error` (string).
-   **Actions**: `login`, `register`, `logout`, `refreshToken`.
-   **Persistence**: Persist the `refreshToken` in `localStorage` (or secure cookie if possible, but localStorage is easier for this MVP).

### Step 3: Chat Interface (SSE)
-   Use `fetch` or `EventSource` (with a polyfill for headers if needed, or `fetch-event-source`) to handle the streaming endpoint `/api/chat/message`.
-   Since standard `EventSource` doesn't support custom headers (like Authorization), use `fetch` with a reader or a library like `@microsoft/fetch-event-source` to pass the Bearer token.

---

## 5. AI Prompt (Copy & Paste this to an AI Coding Assistant)

You can use the following prompt to generate the project code:

> **Role**: Expert Frontend Developer
> **Task**: Create a React 19 application using Bun, Vite, Redux Toolkit, and Tailwind CSS for a "Personal Finance Concierge" app.
>
> **Context**:
> I have a backend API running at `http://localhost:8081`.
>
> **Requirements**:
>
> 1.  **Setup**:
>     -   Initialize project with Vite + React + TypeScript.
>     -   Configure Tailwind CSS.
>     -   Setup Redux Toolkit for state management.
>
> 2.  **Authentication (Feature 1)**:
>     -   Create an `authSlice` to manage user state and JWT tokens.
>     -   Implement `api.ts` using Axios with interceptors.
>         -   **Request**: Add `Authorization: Bearer <token>`.
>         -   **Response**: Intercept 401s, call `/api/auth/refresh` with the refresh token, update the store, and retry the original request.
>     -   Create **Login Page** (`/login`) taking `usernameOrEmail` and `password`.
>     -   Create **Register Page** (`/register`) taking `email`, `password`, `firstName`, `lastName`. (Note: Username is not required).
>     -   Create a `ProtectedRoute` component to redirect unauthenticated users.
>
> 3.  **Chat Interface (Feature 2)**:
>     -   Create a **Chat Page** (protected).
>     -   Layout: Sidebar (stats/history) + Main Chat Area.
>     -   **Streaming**: Implement a function to call `POST /api/chat/message`. This endpoint returns a `text/event-stream`. Handle the stream to display the AI response character-by-character or chunk-by-chunk.
>     -   **Headers**: Ensure the streaming request includes the JWT token.
>
> 4.  **UI/UX**:
>     -   Use Tailwind CSS for a clean, modern, "financial fintech" look (blues, whites, grays).
>     -   Responsive design.
>
> **API Reference**:
> -   **Login**: `POST /api/auth/login` -> `{ data: { accessToken, refreshToken, user: {...} } }`
> -   **Register**: `POST /api/auth/register` -> `{ email, password, ... }`
> -   **Refresh**: `POST /api/auth/refresh` -> `{ refreshToken }`
> -   **Stream Message**: `POST /api/chat/message` -> `{ message, userId }` (Requires Auth Header)
>
> Please provide the folder structure and the key code files (`store.ts`, `authSlice.ts`, `api.ts`, `LoginPage.tsx`, `ChatPage.tsx`).


