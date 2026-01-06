# 💰 Budget Management API - Complete Guide

## Overview

The Budget Management API allows users to set **category-wise budgets** and **total budgets**. The AI system dynamically uses these budgets when reporting spending status.

---

## 🎯 KEY FEATURES

✅ **Dynamic Budgets** - Set custom budgets per category  
✅ **Total Budget** - Set overall spending limit  
✅ **Multiple Periods** - Daily, Weekly, Monthly, Yearly  
✅ **Alert Thresholds** - Get notified at X% usage  
✅ **AI Integration** - AI agent uses YOUR budgets automatically  
✅ **Real-time Updates** - Changes reflect immediately  

---

## 📊 BASE URL

```
http://localhost:8081/api/budgets
```

**Authentication:** All endpoints require JWT Bearer token

---

## 🚀 ENDPOINTS

### 1. Set Category Budget

**Create or update budget for a specific category**

```http
POST /api/budgets/category
Content-Type: application/json
Authorization: Bearer <token>

{
  "categoryName": "Food",
  "budgetAmount": 250.00,
  "budgetPeriod": "MONTHLY",
  "alertThreshold": 80.0
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "categoryName": "Food",
    "categoryIcon": "🍔",
    "categoryColor": "#FF6B6B",
    "budgetAmount": 250.00,
    "currentSpending": 125.50,
    "remaining": 124.50,
    "percentageUsed": 50.20,
    "budgetPeriod": "MONTHLY",
    "alertThreshold": 80.0,
    "isTotalBudget": false,
    "isOverBudget": false,
    "isNearLimit": false,
    "createdAt": "2026-01-05T10:30:00",
    "updatedAt": "2026-01-05T10:30:00"
  },
  "message": "Budget set successfully for Food"
}
```

---

### 2. Set Total Budget

**Set overall monthly spending limit**

```http
POST /api/budgets/total
Content-Type: application/json
Authorization: Bearer <token>

{
  "budgetAmount": 2000.00,
  "budgetPeriod": "MONTHLY",
  "alertThreshold": 90.0
}
```

---

### 3. Get All Budgets

**Retrieve all budgets for the user**

```http
GET /api/budgets?period=MONTHLY
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "categoryName": "Food",
      "budgetAmount": 250.00,
      "currentSpending": 125.50,
      "remaining": 124.50,
      "percentageUsed": 50.20,
      "isOverBudget": false
    },
    {
      "id": 2,
      "categoryName": "Transport",
      "budgetAmount": 150.00,
      "currentSpending": 180.00,
      "remaining": -30.00,
      "percentageUsed": 120.00,
      "isOverBudget": true
    }
  ],
  "message": "Retrieved 8 budget(s)"
}
```

---

### 4. Get Category Budget

**Get budget for specific category**

```http
GET /api/budgets/category/Food?period=MONTHLY
Authorization: Bearer <token>
```

---

### 5. Get Total Budget

```http
GET /api/budgets/total?period=MONTHLY
Authorization: Bearer <token>
```

---

### 6. Delete Budget

```http
DELETE /api/budgets/{budgetId}
Authorization: Bearer <token>
```

---

### 7. Get Budget Limits Map

**Get all budget limits as a simple map (used by AI agent)**

```http
GET /api/budgets/limits
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "Food": 250.0,
    "Transport": 150.0,
    "Bills": 300.0,
    "Entertainment": 200.0,
    "Shopping": 250.0,
    "Health": 200.0,
    "Education": 150.0,
    "Other": 100.0
  }
}
```

---

### 8. Batch Set Budgets

**Set multiple budgets at once**

```http
POST /api/budgets/batch
Content-Type: application/json
Authorization: Bearer <token>

[
  {
    "categoryName": "Food",
    "budgetAmount": 250.00,
    "budgetPeriod": "MONTHLY"
  },
  {
    "categoryName": "Transport",
    "budgetAmount": 150.00,
    "budgetPeriod": "MONTHLY"
  },
  {
    "categoryName": "Bills",
    "budgetAmount": 400.00,
    "budgetPeriod": "MONTHLY"
  }
]
```

---

## 📋 REQUEST PARAMETERS

### BudgetRequestDTO

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `categoryName` | String | No* | Category name | "Food" |
| `budgetAmount` | BigDecimal | Yes | Budget amount | 250.00 |
| `budgetPeriod` | String | No | DAILY/WEEKLY/MONTHLY/YEARLY | "MONTHLY" |
| `alertThreshold` | BigDecimal | No | Alert at X% | 80.0 |
| `isTotalBudget` | Boolean | No | Is this total budget? | false |

**Required for category budget, null for total budget*

---

## 📄 RESPONSE FIELDS

### BudgetResponseDTO

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Budget ID |
| `categoryName` | String | Category name (null for total) |
| `categoryIcon` | String | Category emoji |
| `categoryColor` | String | Category color hex code |
| `budgetAmount` | BigDecimal | Budget limit |
| `currentSpending` | BigDecimal | Amount spent so far |
| `remaining` | BigDecimal | Budget remaining |
| `percentageUsed` | Double | % of budget used |
| `budgetPeriod` | String | Time period |
| `alertThreshold` | BigDecimal | Alert threshold % |
| `isTotalBudget` | Boolean | Is total budget |
| `isOverBudget` | Boolean | Has exceeded budget |
| `isNearLimit` | Boolean | Over alert threshold |

---

## 🤖 AI INTEGRATION

### How It Works

1. **User sets budget:**
   ```
   POST /api/budgets/category
   { "categoryName": "Food", "budgetAmount": 300.00 }
   ```

2. **User asks AI:**
   ```
   "What's my Food budget status?"
   ```

3. **AI fetches dynamic budget:**
   - Calls `getBudgetStatus("Food", userId)`
   - Uses YOUR $300 budget (not default $200)
   - Returns: "You have spent $125.50 out of $300.00 on Food..."

4. **Result:** AI always uses YOUR custom budgets! 🎉

---

## 📱 FRONTEND EXAMPLES

### React - Set Budget

```javascript
const setBudget = async (category, amount) => {
  const response = await fetch('/api/budgets/category', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      categoryName: category,
      budgetAmount: amount,
      budgetPeriod: 'MONTHLY',
      alertThreshold: 80.0
    })
  });
  return await response.json();
};

// Usage
await setBudget('Food', 300.00);
```

### React - Budget Settings Component

```jsx
const BudgetSettings = () => {
  const [budgets, setBudgets] = useState([]);
  
  useEffect(() => {
    fetch('/api/budgets', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => res.json())
    .then(data => setBudgets(data.data));
  }, []);
  
  const updateBudget = async (category, newAmount) => {
    await fetch('/api/budgets/category', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        categoryName: category,
        budgetAmount: newAmount,
        budgetPeriod: 'MONTHLY'
      })
    });
    // Refresh budgets
  };
  
  return (
    <div>
      <h2>Budget Settings</h2>
      {budgets.map(budget => (
        <div key={budget.id}>
          <span>{budget.categoryIcon} {budget.categoryName}</span>
          <input 
            type="number" 
            value={budget.budgetAmount} 
            onChange={(e) => updateBudget(budget.categoryName, e.target.value)}
          />
          <progress 
            value={budget.percentageUsed} 
            max="100"
            className={budget.isOverBudget ? 'over-budget' : ''}
          />
          <span>${budget.currentSpending} / ${budget.budgetAmount}</span>
        </div>
      ))}
    </div>
  );
};
```

---

## 🧪 TESTING

### Step 1: Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

### Step 2: Set Food Budget

```bash
export TOKEN="your-jwt-token"

curl -X POST http://localhost:8081/api/budgets/category \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "categoryName": "Food",
    "budgetAmount": 300.00,
    "budgetPeriod": "MONTHLY",
    "alertThreshold": 80.0
  }'
```

### Step 3: Verify Budget

```bash
curl -X GET "http://localhost:8081/api/budgets/category/Food?period=MONTHLY" \
  -H "Authorization: Bearer $TOKEN"
```

### Step 4: Test AI Integration

```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "What is my Food budget status?",
    "userId": "1"
  }'
```

**Expected AI Response:**
```
"You have spent $125.50 out of $300.00 on Food. Remaining: $174.50."
```
(Uses YOUR $300 budget, not the default $200!)

---

## 📊 BUDGET PERIODS

| Period | Description | Use Case |
|--------|-------------|----------|
| `DAILY` | Resets every day | Daily spending limits |
| `WEEKLY` | Monday to Sunday | Weekly allowances |
| `MONTHLY` | 1st to last day of month | Most common |
| `YEARLY` | Jan 1 to Dec 31 | Annual budgets |

---

## ⚠️ VALIDATION RULES

1. **Budget Amount:** Must be > 0
2. **Category:** Must exist in database
3. **Period:** Must be DAILY/WEEKLY/MONTHLY/YEARLY
4. **Alert Threshold:** 0-100 (percentage)
5. **One Budget Per Category-Period:** Updates existing if duplicate

---

## 🎨 BUDGET UI EXAMPLES

### Progress Bar with Alert

```html
<div class="budget-item ${budget.isOverBudget ? 'over' : budget.isNearLimit ? 'warning' : 'ok'}">
  <div class="budget-header">
    <span>${budget.categoryIcon} ${budget.categoryName}</span>
    <span>$${budget.currentSpending} / $${budget.budgetAmount}</span>
  </div>
  <progress value="${budget.percentageUsed}" max="100"></progress>
  <span class="remaining">
    ${budget.isOverBudget ? 'Over by' : 'Remaining'}: $${Math.abs(budget.remaining)}
  </span>
</div>
```

### Alert Notification

```javascript
const checkBudgetAlerts = (budgets) => {
  budgets.forEach(budget => {
    if (budget.isOverBudget) {
      showNotification('danger', 
        `You've exceeded your ${budget.categoryName} budget!`);
    } else if (budget.isNearLimit) {
      showNotification('warning',
        `You've used ${budget.percentageUsed}% of your ${budget.categoryName} budget`);
    }
  });
};
```

---

## 🔄 WORKFLOW

### Setting Up Budgets

```
1. User Opens Settings
   ↓
2. Fetches Current Budgets (GET /api/budgets)
   ↓
3. Updates Food Budget to $300
   ↓
4. POST /api/budgets/category
   ↓
5. Budget Saved to Database
   ↓
6. AI Agent Now Uses $300 for Food Budget
```

### When User Adds Expense

```
1. User: "I spent $50 on groceries"
   ↓
2. AI Logs Expense (logExpense)
   ↓
3. Expense Saved to Database
   ↓
4. AI Checks Budget Status (getBudgetStatus)
   ↓
5. Fetches User's Custom Budget ($300)
   ↓
6. Calculates: Spent $175.50 / $300.00
   ↓
7. AI Responds: "Logged $50 to Food. You have $124.50 remaining."
```

---

## 💡 BEST PRACTICES

1. **Set Realistic Budgets** - Based on actual spending patterns
2. **Use Alert Thresholds** - Get notified before overspending
3. **Review Monthly** - Adjust budgets as needed
4. **Total Budget** - Set an overall limit to prevent overspending
5. **Monitor Dashboard** - Check `/api/expenses/budget/status` regularly

---

## 🗄️ DATABASE SCHEMA

```sql
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    budget_amount DECIMAL(10, 2) NOT NULL,
    budget_period VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    is_total_budget BOOLEAN NOT NULL DEFAULT FALSE,
    alert_threshold DECIMAL(5, 2) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    
    UNIQUE KEY (user_id, category_id, budget_period)
);
```

---

## 🎯 QUICK START

### 1. Set Monthly Budgets

```bash
curl -X POST http://localhost:8081/api/budgets/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {"categoryName": "Food", "budgetAmount": 300},
    {"categoryName": "Transport", "budgetAmount": 150},
    {"categoryName": "Bills", "budgetAmount": 500},
    {"categoryName": "Entertainment", "budgetAmount": 200}
  ]'
```

### 2. Set Total Budget

```bash
curl -X POST http://localhost:8081/api/budgets/total \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"budgetAmount": 2000, "alertThreshold": 90}'
```

### 3. Check Status

```bash
curl -X GET http://localhost:8081/api/budgets \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Ask AI

```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"message": "Show my budget status for all categories", "userId": "1"}'
```

---

## ✅ WHAT'S WORKING

1. ✅ Dynamic budget management per user
2. ✅ AI agent uses custom budgets automatically
3. ✅ Real-time budget calculations
4. ✅ Alert thresholds for warnings
5. ✅ Support for multiple budget periods
6. ✅ Total budget tracking
7. ✅ Batch budget updates
8. ✅ Database persistence
9. ✅ Swagger documentation

---

## 🚀 NEXT STEPS

1. ✅ Restart application to apply changes
2. ✅ Run Flyway migration (V5)
3. ✅ Test budget APIs in Swagger
4. ✅ Set your custom budgets
5. ✅ Test AI integration
6. ✅ Build frontend UI

---

**Your budgets are now dynamic and AI-powered!** 🎉💰

