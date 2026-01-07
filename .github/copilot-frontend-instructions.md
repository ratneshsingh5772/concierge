# Personal Finance Concierge - Frontend Developer Guide

## Project Overview
A personal finance tracking application that allows users to manage expenses, budgets, and view analytics through a REST API backend.

**Base URL**: `http://localhost:8081`  
**Authentication**: JWT Bearer Token  
**Content-Type**: `application/json`

---

## Authentication & Session Management

### 1. User Registration
**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": 1,
      "username": "user_example_com",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "currencyCode": "USD",
      "role": "USER"
    }
  },
  "timestamp": "2026-01-07T10:30:00"
}
```

**Frontend Action**:
```javascript
// Store authentication data
sessionStorage.setItem('userId', response.data.user.id);
sessionStorage.setItem('token', response.data.accessToken);
sessionStorage.setItem('refreshToken', response.data.refreshToken);
sessionStorage.setItem('username', response.data.user.username);
```

---

### 2. User Login
**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "user@example.com",
  "password": "SecurePass123"
}
```

**Response**: Same as registration

**Frontend Action**: Same token storage as registration

---

### 3. Get Current User Profile
**Endpoint**: `GET /api/auth/me`  
**Auth Required**: ✅ Yes

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "user_example_com",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

---

## Categories API (Read-Only)

### Get All Categories
**Endpoint**: `GET /api/categories`  
**Auth Required**: ✅ Yes  
**User Scoped**: ❌ No (Global predefined categories)

**Headers**:
```
Authorization: Bearer {token}
```

**Response**:
```json
{
  "success": true,
  "message": "Retrieved 8 categories",
  "data": [
    {
      "id": 1,
      "name": "Food",
      "description": "Food and beverages",
      "icon": "🍔",
      "color": "#FF6B6B"
    },
    {
      "id": 2,
      "name": "Transport",
      "description": "Transportation costs",
      "icon": "🚗",
      "color": "#4ECDC4"
    },
    {
      "id": 3,
      "name": "Bills",
      "description": "Utility bills and rent",
      "icon": "📄",
      "color": "#95E1D3"
    },
    {
      "id": 4,
      "name": "Entertainment",
      "description": "Movies, games, leisure",
      "icon": "🎬",
      "color": "#F38181"
    },
    {
      "id": 5,
      "name": "Shopping",
      "description": "Clothing, electronics",
      "icon": "🛍️",
      "color": "#AA96DA"
    },
    {
      "id": 6,
      "name": "Health",
      "description": "Healthcare and fitness",
      "icon": "💊",
      "color": "#FCBAD3"
    },
    {
      "id": 7,
      "name": "Education",
      "description": "Books, courses, tuition",
      "icon": "📚",
      "color": "#FFFFD2"
    },
    {
      "id": 8,
      "name": "Other",
      "description": "Miscellaneous expenses",
      "icon": "📦",
      "color": "#A8D8EA"
    }
  ]
}
```

**Frontend Usage**:
```javascript
// Fetch categories once and cache
async function fetchCategories() {
  const token = sessionStorage.getItem('token');
  const response = await fetch('http://localhost:8081/api/categories', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  const data = await response.json();
  
  // Cache in localStorage for offline use
  localStorage.setItem('categories', JSON.stringify(data.data));
  return data.data;
}

// Render category dropdown
function renderCategorySelect(categories) {
  return categories.map(cat => `
    <option value="${cat.name}" data-icon="${cat.icon}" data-color="${cat.color}">
      ${cat.icon} ${cat.name}
    </option>
  `).join('');
}
```

**⚠️ IMPORTANT**: Categories are READ-ONLY
- ❌ Cannot create new categories
- ❌ Cannot modify existing categories
- ❌ Cannot delete categories
- ✅ Can only fetch and display

---

## Budget Management API (User-Scoped)

### 1. Get All Budgets for User
**Endpoint**: `GET /api/users/{userId}/budgets?period=MONTHLY`  
**Auth Required**: ✅ Yes  
**User Scoped**: ✅ Yes (can only access own budgets)

**Parameters**:
- `userId` (path) - User ID from login response
- `period` (query) - DAILY | WEEKLY | MONTHLY | YEARLY (default: MONTHLY)

**Request**:
```javascript
const userId = sessionStorage.getItem('userId');
const token = sessionStorage.getItem('token');

fetch(`http://localhost:8081/api/users/${userId}/budgets?period=MONTHLY`, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
```

**Response**:
```json
{
  "success": true,
  "message": "Retrieved 3 budget(s)",
  "data": [
    {
      "id": 1,
      "categoryName": "Food",
      "categoryIcon": "🍔",
      "categoryColor": "#FF6B6B",
      "budgetAmount": 500.00,
      "currentSpending": 320.50,
      "remaining": 179.50,
      "percentageUsed": 64.1,
      "budgetPeriod": "MONTHLY",
      "alertThreshold": 80.0,
      "isTotalBudget": false,
      "isOverBudget": false,
      "isNearLimit": false,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-07T10:00:00"
    },
    {
      "id": 2,
      "categoryName": "Transport",
      "categoryIcon": "🚗",
      "categoryColor": "#4ECDC4",
      "budgetAmount": 200.00,
      "currentSpending": 175.00,
      "remaining": 25.00,
      "percentageUsed": 87.5,
      "budgetPeriod": "MONTHLY",
      "alertThreshold": 80.0,
      "isTotalBudget": false,
      "isOverBudget": false,
      "isNearLimit": true,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-07T10:00:00"
    }
  ]
}
```

**Frontend Display**:
```javascript
// Render budget progress bars
function renderBudgetProgress(budgets) {
  return budgets.map(budget => `
    <div class="budget-card" style="border-left: 4px solid ${budget.categoryColor}">
      <div class="budget-header">
        <span class="category-icon">${budget.categoryIcon}</span>
        <span class="category-name">${budget.categoryName}</span>
      </div>
      <div class="budget-amount">
        $${budget.currentSpending.toFixed(2)} / $${budget.budgetAmount.toFixed(2)}
      </div>
      <div class="progress-bar">
        <div class="progress-fill ${budget.isOverBudget ? 'over-budget' : ''}" 
             style="width: ${Math.min(budget.percentageUsed, 100)}%; background: ${budget.categoryColor}">
        </div>
      </div>
      <div class="budget-info">
        <span>Remaining: $${budget.remaining.toFixed(2)}</span>
        <span>${budget.percentageUsed.toFixed(1)}% used</span>
      </div>
      ${budget.isNearLimit ? '<span class="alert">⚠️ Near limit!</span>' : ''}
      ${budget.isOverBudget ? '<span class="alert danger">🚨 Over budget!</span>' : ''}
    </div>
  `).join('');
}
```

---

### 2. Create Category Budget
**Endpoint**: `POST /api/users/{userId}/budgets/category`  
**Auth Required**: ✅ Yes

**Request**:
```javascript
const userId = sessionStorage.getItem('userId');
const token = sessionStorage.getItem('token');

fetch(`http://localhost:8081/api/users/${userId}/budgets/category`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    categoryName: "Food",
    budgetAmount: 500.00,
    budgetPeriod: "MONTHLY",
    alertThreshold: 80.0
  })
})
```

**Request Body**:
```json
{
  "categoryName": "Food",
  "budgetAmount": 500.00,
  "budgetPeriod": "MONTHLY",
  "alertThreshold": 80.0
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Budget set successfully for Food",
  "data": {
    "id": 1,
    "categoryName": "Food",
    "budgetAmount": 500.00,
    "currentSpending": 0.00,
    "remaining": 500.00,
    "percentageUsed": 0.0,
    "budgetPeriod": "MONTHLY",
    "alertThreshold": 80.0,
    "isTotalBudget": false
  }
}
```

---

### 3. Create Total Budget
**Endpoint**: `POST /api/users/{userId}/budgets/total`  
**Auth Required**: ✅ Yes

**Request Body**:
```json
{
  "budgetAmount": 2000.00,
  "budgetPeriod": "MONTHLY",
  "alertThreshold": 90.0,
  "isTotalBudget": true
}
```

**Response**: Similar to category budget

---

### 4. Update Budget
**Endpoint**: `PUT /api/users/{userId}/budgets/{budgetId}`  
**Auth Required**: ✅ Yes

**Request**:
```javascript
fetch(`http://localhost:8081/api/users/${userId}/budgets/${budgetId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    categoryName: "Food",
    budgetAmount: 600.00,
    budgetPeriod: "MONTHLY",
    alertThreshold: 75.0
  })
})
```

---

### 5. Delete Budget
**Endpoint**: `DELETE /api/users/{userId}/budgets/{budgetId}`  
**Auth Required**: ✅ Yes

**Request**:
```javascript
fetch(`http://localhost:8081/api/users/${userId}/budgets/${budgetId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
```

**Response**:
```json
{
  "success": true,
  "message": "Budget deleted successfully",
  "data": null
}
```

---

## Analytics API (User-Scoped)

### 1. Get Analytics Summary
**Endpoint**: `GET /api/users/{userId}/analytics/summary`  
**Auth Required**: ✅ Yes

**Response**:
```json
{
  "success": true,
  "data": {
    "totalSpentLast10Days": 450.75,
    "projectedMonthlySpend": 1200.00,
    "highestDailySpend": {
      "date": "2026-01-05",
      "amount": 85.50
    }
  }
}
```

**Frontend Display**:
```javascript
function renderSummary(summary) {
  return `
    <div class="summary-cards">
      <div class="card">
        <h3>Last 10 Days</h3>
        <p class="amount">$${summary.totalSpentLast10Days.toFixed(2)}</p>
      </div>
      <div class="card">
        <h3>Projected Monthly</h3>
        <p class="amount">$${summary.projectedMonthlySpend.toFixed(2)}</p>
      </div>
      <div class="card">
        <h3>Highest Daily Spend</h3>
        <p class="amount">$${summary.highestDailySpend.amount.toFixed(2)}</p>
        <p class="date">${summary.highestDailySpend.date}</p>
      </div>
    </div>
  `;
}
```

---

### 2. Get Daily Trend
**Endpoint**: `GET /api/users/{userId}/analytics/trend?days=10`  
**Auth Required**: ✅ Yes

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "date": "2026-01-01",
      "day": "Wed",
      "amount": 45.00
    },
    {
      "date": "2026-01-02",
      "day": "Thu",
      "amount": 32.50
    }
    // ... more days
  ]
}
```

**Frontend Chart**:
```javascript
// Use Chart.js or similar
function renderTrendChart(trendData) {
  const labels = trendData.map(d => d.day);
  const amounts = trendData.map(d => d.amount);
  
  new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Daily Spending',
        data: amounts,
        borderColor: '#4ECDC4',
        backgroundColor: 'rgba(78, 205, 196, 0.1)'
      }]
    }
  });
}
```

---

### 3. Get Monthly Spending
**Endpoint**: `GET /api/users/{userId}/analytics/monthly-spend?year=2026`  
**Auth Required**: ✅ Yes

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "month": "January",
      "monthNumber": 1,
      "amount": 450.75
    },
    {
      "month": "February",
      "monthNumber": 2,
      "amount": 0.00
    }
    // ... 12 months total
  ]
}
```

---

### 4. Get Forecast
**Endpoint**: `GET /api/users/{userId}/analytics/forecast`  
**Auth Required**: ✅ Yes

**Response**:
```json
{
  "success": true,
  "data": {
    "predictedMonthEndSpend": 1050.00,
    "predictedYearEndSpend": 12600.00,
    "nextLikelySpend": {
      "category": "Food",
      "predictedAmount": 45.00,
      "confidence": "High"
    },
    "aiAnalysis": "Based on your spending habits, you are on track to spend $1050.00 this month and $12600.00 this year. Your most frequent expense category is Food."
  }
}
```

---

## Error Handling

### Error Response Format
```json
{
  "success": false,
  "message": "Error message here",
  "data": null,
  "timestamp": "2026-01-07T10:30:00"
}
```

### Common HTTP Status Codes

| Status | Meaning | Action |
|--------|---------|--------|
| 200 | Success | Process data |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Show validation errors to user |
| 401 | Unauthorized | Redirect to login, token expired |
| 403 | Forbidden | "You don't have permission" message |
| 404 | Not Found | "Resource not found" message |
| 500 | Server Error | "Something went wrong" message |

### Frontend Error Handler
```javascript
async function apiCall(url, options = {}) {
  const token = sessionStorage.getItem('token');
  
  const response = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...options.headers
    }
  });
  
  if (response.status === 401) {
    // Token expired, redirect to login
    sessionStorage.clear();
    window.location.href = '/login';
    return;
  }
  
  if (response.status === 403) {
    showError('You do not have permission to access this resource');
    return;
  }
  
  if (response.status === 404) {
    showError('Resource not found');
    return;
  }
  
  if (!response.ok) {
    const error = await response.json();
    showError(error.message || 'An error occurred');
    throw new Error(error.message);
  }
  
  return await response.json();
}
```

---

## Complete Frontend Example

### Login Flow
```javascript
async function login(username, password) {
  try {
    const response = await fetch('http://localhost:8081/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    
    if (data.success) {
      // Store auth data
      sessionStorage.setItem('userId', data.data.user.id);
      sessionStorage.setItem('token', data.data.accessToken);
      sessionStorage.setItem('username', data.data.user.username);
      
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } else {
      showError(data.message);
    }
  } catch (error) {
    showError('Login failed: ' + error.message);
  }
}
```

### Dashboard Initialization
```javascript
async function initDashboard() {
  const userId = sessionStorage.getItem('userId');
  const token = sessionStorage.getItem('token');
  
  if (!userId || !token) {
    window.location.href = '/login';
    return;
  }
  
  try {
    // Fetch all dashboard data
    const [budgets, summary, categories] = await Promise.all([
      apiCall(`http://localhost:8081/api/users/${userId}/budgets?period=MONTHLY`),
      apiCall(`http://localhost:8081/api/users/${userId}/analytics/summary`),
      apiCall('http://localhost:8081/api/categories')
    ]);
    
    // Render components
    renderBudgets(budgets.data);
    renderSummary(summary.data);
    renderCategorySelect(categories.data);
    
  } catch (error) {
    showError('Failed to load dashboard: ' + error.message);
  }
}
```

---

## Best Practices

### 1. Token Management
```javascript
// Check token expiration
function isTokenExpired() {
  const token = sessionStorage.getItem('token');
  if (!token) return true;
  
  // Decode JWT (use jwt-decode library)
  const decoded = jwtDecode(token);
  return decoded.exp * 1000 < Date.now();
}

// Auto-refresh before expiration
setInterval(() => {
  if (isTokenExpired()) {
    refreshToken();
  }
}, 60000); // Check every minute
```

### 2. Caching
```javascript
// Cache categories (they rarely change)
async function getCategories() {
  const cached = localStorage.getItem('categories');
  if (cached) {
    return JSON.parse(cached);
  }
  
  const response = await apiCall('http://localhost:8081/api/categories');
  localStorage.setItem('categories', JSON.stringify(response.data));
  return response.data;
}
```

### 3. Loading States
```javascript
function setLoading(isLoading) {
  const loader = document.getElementById('loader');
  loader.style.display = isLoading ? 'block' : 'none';
}

async function loadDashboard() {
  setLoading(true);
  try {
    await initDashboard();
  } finally {
    setLoading(false);
  }
}
```

---

## API Summary Table

| Endpoint | Method | Auth | User-Scoped | Purpose |
|----------|--------|------|-------------|---------|
| `/api/auth/register` | POST | ❌ | ❌ | Register new user |
| `/api/auth/login` | POST | ❌ | ❌ | Login user |
| `/api/auth/me` | GET | ✅ | ❌ | Get current user |
| `/api/categories` | GET | ✅ | ❌ | Get all categories (read-only) |
| `/api/users/{userId}/budgets` | GET | ✅ | ✅ | Get user budgets |
| `/api/users/{userId}/budgets/category` | POST | ✅ | ✅ | Create category budget |
| `/api/users/{userId}/budgets/total` | POST | ✅ | ✅ | Create total budget |
| `/api/users/{userId}/budgets/{budgetId}` | PUT | ✅ | ✅ | Update budget |
| `/api/users/{userId}/budgets/{budgetId}` | DELETE | ✅ | ✅ | Delete budget |
| `/api/users/{userId}/analytics/summary` | GET | ✅ | ✅ | Get analytics summary |
| `/api/users/{userId}/analytics/trend` | GET | ✅ | ✅ | Get daily trend |
| `/api/users/{userId}/analytics/monthly-spend` | GET | ✅ | ✅ | Get monthly spending |
| `/api/users/{userId}/analytics/forecast` | GET | ✅ | ✅ | Get spending forecast |

---

*Last Updated: January 7, 2026*  
*API Version: 2.0 (Production)*

