# Swagger/OpenAPI Documentation Guide

## Overview

The Finance Concierge API now includes comprehensive **Swagger/OpenAPI documentation** for easy API exploration, testing, and integration.

## Access Swagger UI

### Local Development

Once the application is running, access Swagger UI at:

```
http://localhost:8081/swagger-ui.html
```

Or:

```
http://localhost:8081/swagger-ui/index.html
```

### API Documentation JSON

Get the raw OpenAPI specification:

```
http://localhost:8081/v3/api-docs
http://localhost:8081/api-docs
```

## Features

✅ **Interactive API Documentation** - Explore all endpoints  
✅ **Try It Out** - Test APIs directly from browser  
✅ **JWT Authentication Support** - Built-in token authentication  
✅ **Request/Response Examples** - See sample data  
✅ **Schema Documentation** - Complete model definitions  
✅ **Auto-Generated** - Always up-to-date with code  

## Using Swagger UI

### 1. Launch Application

```bash
./run-java21.sh
```

### 2. Open Swagger UI

Navigate to: `http://localhost:8081/swagger-ui.html`

You'll see:
- **Authentication** - User authentication endpoints
- **Chat** - Chat and messaging endpoints
- **Health** - System health endpoints

### 3. Test Public Endpoints (No Authentication)

#### Register a New User

1. Expand **Authentication** section
2. Click on **POST /api/auth/register**
3. Click **Try it out**
4. Fill in the request body:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "TestPass123!",
  "firstName": "Test",
  "lastName": "User"
}
```
5. Click **Execute**
6. See the response with your JWT tokens

#### Login

1. Click on **POST /api/auth/login**
2. Click **Try it out**
3. Fill in credentials:
```json
{
  "usernameOrEmail": "testuser",
  "password": "TestPass123!"
}
```
4. Click **Execute**
5. Copy the `accessToken` from response

### 4. Authenticate for Protected Endpoints

1. Click the **Authorize** button (🔓 icon) at the top right
2. In the popup, enter your JWT token (without "Bearer" prefix)
3. Click **Authorize**
4. Click **Close**

Now you can access protected endpoints!

### 5. Test Protected Endpoints

#### Send a Message

1. Expand **Chat** section
2. Click on **POST /api/chat/message/json**
3. Click **Try it out**
4. Fill in the request:
```json
{
  "message": "I spent $30 on lunch",
  "userId": "testuser"
}
```
5. Click **Execute**
6. See the AI response

#### Get Chat History

1. Click on **GET /api/chat/history/{userId}**
2. Click **Try it out**
3. Enter userId: `testuser`
4. Click **Execute**
5. See all chat history

## API Endpoints Overview

### Authentication Endpoints (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login user |
| POST | `/api/auth/refresh` | Refresh access token |

### Authentication Endpoints (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/me` | Get current user profile |
| POST | `/api/auth/logout` | Logout user |

### Chat Endpoints (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat/message` | Send message (SSE streaming) |
| POST | `/api/chat/message/json` | Send message (JSON response) |
| POST | `/api/chat/reset` | Reset user session |
| GET | `/api/chat/history/{userId}` | Get chat history |
| GET | `/api/chat/history/{userId}/recent` | Get recent messages |
| GET | `/api/chat/history/{userId}/paginated` | Get paginated history |
| GET | `/api/chat/stats/{userId}` | Get user statistics |

### System Endpoints (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chat/health` | Health check |

## Configuration

### application.properties

```properties
# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.disable-swagger-default-url=true
springdoc.swagger-ui.display-request-duration=true
springdoc.show-actuator=true
```

### Customization

Edit `SwaggerConfig.java` to customize:
- API title and description
- Contact information
- License information
- Server URLs
- Security schemes

## Security Configuration

Swagger endpoints are configured as public in `SecurityConfig.java`:

```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/api-docs/**",
    "/swagger-resources/**",
    "/webjars/**"
).permitAll()
```

## Using Swagger with Postman

### Export OpenAPI Spec

1. Visit: `http://localhost:8081/v3/api-docs`
2. Copy the JSON response
3. In Postman: **Import** → **Raw text** → Paste JSON
4. All endpoints will be imported to Postman

## Sample Workflow

### Complete Testing Sequence

1. **Open Swagger UI**
   ```
   http://localhost:8081/swagger-ui.html
   ```

2. **Register User**
   - Endpoint: `POST /api/auth/register`
   - Body: User details
   - Response: Tokens + user info

3. **Authorize**
   - Click 🔓 Authorize button
   - Enter access token
   - Click Authorize

4. **Send Message**
   - Endpoint: `POST /api/chat/message/json`
   - Body: `{"message": "I spent $50 on groceries", "userId": "testuser"}`
   - Response: AI-generated response

5. **View History**
   - Endpoint: `GET /api/chat/history/testuser`
   - Response: Complete chat history

6. **Get Statistics**
   - Endpoint: `GET /api/chat/stats/testuser`
   - Response: User statistics

## API Documentation Features

### Request Examples

Swagger automatically shows example requests:

```json
{
  "message": "string",
  "userId": "string"
}
```

### Response Examples

Example responses with status codes:

```json
{
  "success": true,
  "message": "Message processed successfully",
  "data": {
    "response": "I have logged your expense...",
    "userId": "testuser",
    "timestamp": 1735290000000
  },
  "timestamp": "2026-01-02T12:00:00"
}
```

### Schema Definitions

All DTOs are documented with:
- Field names
- Data types
- Required fields
- Validation rules
- Descriptions

## Production Considerations

### Disable Swagger in Production (Optional)

Add to `application-prod.properties`:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

Or use environment variable:

```bash
export SPRINGDOC_SWAGGER_UI_ENABLED=false
```

### Secure Swagger in Production

If you want to keep Swagger but secure it:

```java
// In SecurityConfig.java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
    .hasRole("ADMIN")
```

## Troubleshooting

### Issue: Swagger UI not loading

**Solution:**
1. Check application is running: `http://localhost:8081/api/chat/health`
2. Clear browser cache
3. Try: `http://localhost:8081/swagger-ui/index.html`

### Issue: 401 Unauthorized on protected endpoints

**Solution:**
1. Click Authorize button
2. Enter valid JWT token
3. Token should not include "Bearer" prefix in Swagger

### Issue: Cannot see all endpoints

**Solution:**
1. Scroll down in Swagger UI
2. Check endpoint tags are expanded
3. Verify endpoints are not filtered

### Issue: "Failed to load API definition"

**Solution:**
1. Check `application.properties` configuration
2. Verify SpringDoc dependency in `pom.xml`
3. Check console for errors

## Advanced Features

### Filtering Operations

Sort operations by:
- **Method**: Groups by HTTP method
- **Tag**: Groups by controller tag
- **Alpha**: Alphabetical order

Change sorting in UI or configure in properties:

```properties
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

### Request Duration Display

See how long each request takes:

```properties
springdoc.swagger-ui.display-request-duration=true
```

### Deep Linking

Share direct links to specific endpoints:

```
http://localhost:8081/swagger-ui.html#/Authentication/login
```

## Integration with CI/CD

### Generate OpenAPI Spec

```bash
# Start application
./run-java21.sh &

# Wait for startup
sleep 10

# Download OpenAPI spec
curl http://localhost:8081/v3/api-docs > openapi.json

# Validate spec
npx @apidevtools/swagger-cli validate openapi.json
```

### API Documentation in README

Include API docs link in your README:

```markdown
## API Documentation

Interactive API documentation available at:
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8081/v3/api-docs
```

## Comparison: Swagger vs Postman

| Feature | Swagger UI | Postman |
|---------|-----------|---------|
| Interactive Docs | ✅ Yes | ❌ No |
| Test Endpoints | ✅ Yes | ✅ Yes |
| Auto-Generated | ✅ Yes | ❌ Manual |
| Share with Team | ✅ URL | ✅ Collection |
| Authentication | ✅ Built-in | ✅ Advanced |
| Automation | ❌ Limited | ✅ Scripts |

**Recommendation:** Use both!
- Swagger for documentation and quick testing
- Postman for automated testing and complex workflows

## Summary

✅ **Swagger UI configured and ready**  
✅ **All endpoints documented**  
✅ **JWT authentication integrated**  
✅ **Interactive testing available**  
✅ **Auto-generated documentation**  
✅ **OpenAPI 3.0 specification**  
✅ **Production-ready configuration**  

Access your API documentation at: **http://localhost:8081/swagger-ui.html**

Happy API exploring! 🚀

