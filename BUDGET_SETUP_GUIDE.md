# Budget Management Setup Guide

## 🎯 Overview
The Budget Management System allows users to set custom budgets for categories and get AI-powered alerts when approaching limits.

## 📋 Database Structure

### Budgets Table
- **user_id**: Reference to the user
- **category_id**: Reference to category (NULL for total budget)
- **budget_amount**: The budget limit
- **budget_period**: DAILY, WEEKLY, MONTHLY, or YEARLY
- **is_total_budget**: TRUE for overall spending limit
- **alert_threshold**: Percentage (e.g., 80.0 = 80%)
- **is_active**: Whether the budget is currently active

## 🚀 Quick Start

### 1. Start the Application
```bash
cd /home/ratnesh/Documents/concierge
./restart-app.sh
```

### 2. Login to Get JWT Token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ratnesh.sheatvns",
    "password": "your-password"
  }'
```

Save the token from the response.

### 3. Set Your First Budget
```bash
# Set Food budget to $300/month with 80% alert
curl -X POST http://localhost:8081/api/budgets/category \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "categoryName": "Food",
    "budgetAmount": 300.00,
    "budgetPeriod": "MONTHLY",
    "alertThreshold": 80.0
  }'
```

## 📊 Available Budget APIs

### Set Category Budget
```http
POST /api/budgets/category
```

**Request Body:**
```json
{
  "categoryName": "Food",
  "budgetAmount": 300.00,
  "budgetPeriod": "MONTHLY",
  "alertThreshold": 80.0
}
```

### Set Total Budget
```http
POST /api/budgets/total
```

**Request Body:**
```json
{
  "budgetAmount": 2000.00,
  "budgetPeriod": "MONTHLY",
  "alertThreshold": 90.0
}
```

### Get All Budgets
```http
GET /api/budgets
```

### Get Budget Status
```http
GET /api/budgets/status
```

Returns current spending vs budgets with alerts.

### Update Budget
```http
PUT /api/budgets/{budgetId}
```

### Delete Budget
```http
DELETE /api/budgets/{budgetId}
```

### Set Multiple Budgets at Once
```http
POST /api/budgets/bulk
```

**Request Body:**
```json
[
  {
    "categoryName": "Food",
    "budgetAmount": 300.00,
    "budgetPeriod": "MONTHLY"
  },
  {
    "categoryName": "Transport",
    "budgetAmount": 150.00,
    "budgetPeriod": "MONTHLY"
  }
]
```

## 🤖 AI Integration

The AI agent automatically uses your custom budgets:

```bash
# Ask AI about your budget
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "What is my Food budget status?",
    "userId": "1"
  }'
```

**AI Response Example:**
```
You have spent $200.00 out of $300.00 on Food this month. 
Remaining: $100.00 (33.33% remaining).
⚠️ You are approaching your budget limit (80% threshold).
```

## 📈 Budget Periods

- **DAILY**: Resets every day
- **WEEKLY**: Resets every week (Monday)
- **MONTHLY**: Resets every month (1st day)
- **YEARLY**: Resets every year (January 1st)

## 🎨 Default Categories

1. **Food** - Groceries, restaurants, coffee
2. **Transport** - Uber, gas, public transit
3. **Bills** - Utilities, rent, phone
4. **Entertainment** - Movies, games, subscriptions
5. **Shopping** - Clothes, electronics
6. **Health** - Gym, medicine, doctor
7. **Education** - Courses, books
8. **Other** - Miscellaneous expenses

## 💡 Tips

### 1. Start with Monthly Budgets
Most people find monthly budgets easiest to manage:
```json
{
  "budgetPeriod": "MONTHLY"
}
```

### 2. Set Alert Thresholds
Get warnings before you overspend:
```json
{
  "alertThreshold": 80.0  // Alert at 80% spent
}
```

### 3. Use Total Budget for Overall Control
Set a total spending limit across all categories:
```json
{
  "isTotalBudget": true,
  "budgetAmount": 2000.00
}
```

### 4. Adjust Budgets Anytime
Update your budgets as your needs change:
```http
PUT /api/budgets/{budgetId}
```

## 🔍 Checking Budget Status

### Option 1: Use the API
```bash
curl -X GET http://localhost:8081/api/budgets/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Option 2: Ask the AI
```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "Show me all my budget statuses",
    "userId": "1"
  }'
```

### Option 3: Use Swagger UI
Visit: http://localhost:8081/swagger-ui.html

## 🛠️ Troubleshooting

### Issue: "Category not found"
**Solution:** Use exact category names: Food, Transport, Bills, Entertainment, Shopping, Health, Education, Other

### Issue: "Budget already exists"
**Solution:** Use PUT to update existing budget instead of POST

### Issue: "AI not using my budgets"
**Solution:** Restart the application after setting budgets:
```bash
./restart-app.sh
```

## 📝 Example Workflow

### Complete Setup for New User

1. **Login**
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ratnesh.sheatvns","password":"pass123"}' \
  | jq -r '.token')
```

2. **Set Multiple Budgets**
```bash
curl -X POST http://localhost:8081/api/budgets/bulk \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {"categoryName": "Food", "budgetAmount": 300.00, "budgetPeriod": "MONTHLY", "alertThreshold": 80.0},
    {"categoryName": "Transport", "budgetAmount": 150.00, "budgetPeriod": "MONTHLY", "alertThreshold": 80.0},
    {"categoryName": "Bills", "budgetAmount": 500.00, "budgetPeriod": "MONTHLY", "alertThreshold": 90.0},
    {"categoryName": "Entertainment", "budgetAmount": 100.00, "budgetPeriod": "MONTHLY", "alertThreshold": 75.0}
  ]'
```

3. **Set Total Budget**
```bash
curl -X POST http://localhost:8081/api/budgets/total \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "budgetAmount": 2000.00,
    "budgetPeriod": "MONTHLY",
    "alertThreshold": 85.0
  }'
```

4. **Check Status**
```bash
curl -X GET http://localhost:8081/api/budgets/status \
  -H "Authorization: Bearer $TOKEN" | jq
```

5. **Log an Expense via AI**
```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "I spent $45 on groceries",
    "userId": "1"
  }'
```

6. **Ask AI for Budget Summary**
```bash
curl -X POST http://localhost:8081/api/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "How am I doing with my budgets this month?",
    "userId": "1"
  }'
```

## 🎉 Success!

You're now using AI-powered budget management! The system will:
- ✅ Track your spending against your custom budgets
- ✅ Send alerts when approaching limits
- ✅ Provide intelligent insights via AI
- ✅ Help you stay on top of your finances

For more details, see:
- [BUDGET_API_GUIDE.md](./BUDGET_API_GUIDE.md) - Complete API documentation
- [Swagger UI](http://localhost:8081/swagger-ui.html) - Interactive API testing

