# Data Visualization API Guide

## Overview

The Expense Controller provides **REST APIs** for data visualization. These endpoints return data in formats ready for charts, graphs, and dashboards.

## Base URL
```
http://localhost:8081/api/expenses
```

## Authentication
All endpoints require JWT authentication:
```
Authorization: Bearer <your-jwt-token>
```

---

## 📊 VISUALIZATION ENDPOINTS

### 1. Dashboard Statistics (All-in-One)

**Get complete dashboard data for current month**

```http
GET /api/expenses/dashboard
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalSpentThisMonth": 285.50,
    "totalSpentLastMonth": 230.00,
    "monthOverMonthChange": 24.13,
    "transactionCount": 12,
    "categoryBreakdown": [
      {
        "categoryName": "Food",
        "categoryIcon": "🍔",
        "categoryColor": "#FF6B6B",
        "totalAmount": 125.00,
        "transactionCount": 5,
        "percentage": 43.78
      }
    ],
    "dailySpending": [
      {
        "date": "2026-01-01",
        "amount": 25.00,
        "transactionCount": 2
      }
    ],
    "topExpenses": [
      {
        "id": 10,
        "amount": 100.00,
        "categoryName": "Bills",
        "description": "Electricity bill",
        "expenseDate": "2026-01-05"
      }
    ],
    "budgetStatus": [
      {
        "categoryName": "Bills",
        "categoryIcon": "📄",
        "budgetLimit": 300.00,
        "spent": 100.00,
        "remaining": 200.00,
        "percentageUsed": 33.33,
        "isOverBudget": false
      }
    ]
  },
  "message": "Dashboard statistics retrieved successfully"
}
```

**Use for:**
- ✅ Main dashboard page
- ✅ All chart types in one request
- ✅ Comprehensive overview

---

### 2. Category Breakdown (Pie Chart)

**Get spending breakdown by category**

```http
GET /api/expenses/breakdown/category
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "categoryName": "Food",
      "categoryIcon": "🍔",
      "categoryColor": "#FF6B6B",
      "totalAmount": 125.00,
      "transactionCount": 5,
      "percentage": 43.78
    },
    {
      "categoryName": "Transport",
      "categoryIcon": "🚗",
      "categoryColor": "#4ECDC4",
      "totalAmount": 70.00,
      "transactionCount": 3,
      "percentage": 24.52
    }
  ]
}
```

**Chart.js Example:**
```javascript
const pieChartData = {
  labels: response.data.map(cat => cat.categoryName),
  datasets: [{
    data: response.data.map(cat => cat.totalAmount),
    backgroundColor: response.data.map(cat => cat.categoryColor)
  }]
};
```

---

### 3. Daily Spending Trend (Line Chart)

**Get daily spending for last 30 days**

```http
GET /api/expenses/trends/daily
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "date": "2025-12-06",
      "amount": 45.50,
      "transactionCount": 3
    },
    {
      "date": "2025-12-07",
      "amount": 0.00,
      "transactionCount": 0
    }
  ]
}
```

**Chart.js Example:**
```javascript
const lineChartData = {
  labels: response.data.map(day => day.date),
  datasets: [{
    label: 'Daily Spending',
    data: response.data.map(day => day.amount),
    borderColor: '#4ECDC4',
    fill: false
  }]
};
```

---

### 4. Budget Status (Progress Bars)

**Get budget usage for all categories**

```http
GET /api/expenses/budget/status
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "categoryName": "Bills",
      "categoryIcon": "📄",
      "budgetLimit": 300.00,
      "spent": 100.00,
      "remaining": 200.00,
      "percentageUsed": 33.33,
      "isOverBudget": false
    },
    {
      "categoryName": "Food",
      "categoryIcon": "🍔",
      "budgetLimit": 200.00,
      "spent": 250.00,
      "remaining": -50.00,
      "percentageUsed": 125.00,
      "isOverBudget": true
    }
  ]
}
```

**HTML Example:**
```html
<div class="budget-item">
  <span>📄 Bills</span>
  <div class="progress-bar">
    <div class="progress" style="width: 33.33%"></div>
  </div>
  <span>$100 / $300</span>
</div>
```

---

### 5. Current Month Expenses (Table Data)

**Get all expenses for current month**

```http
GET /api/expenses/current-month
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 15,
      "amount": 100.00,
      "currency": "USD",
      "categoryName": "Bills",
      "categoryIcon": "📄",
      "categoryColor": "#95E1D3",
      "description": "Electricity bill",
      "expenseDate": "2026-01-05",
      "aiParsed": true,
      "originalMessage": "Electricity bill was $100",
      "createdAt": "2026-01-05T15:30:00"
    }
  ],
  "message": "Retrieved 12 expenses for current month"
}
```

---

### 6. Custom Date Range

**Get dashboard for specific date range**

```http
GET /api/expenses/dashboard/range?startDate=2026-01-01&endDate=2026-01-31
```

**Parameters:**
- `startDate`: YYYY-MM-DD format
- `endDate`: YYYY-MM-DD format

---

### 7. Category-Specific Data

**Get all expenses in a category**

```http
GET /api/expenses/category/Food
GET /api/expenses/category/Transport
GET /api/expenses/category/Bills
```

**Available categories:**
- Food, Transport, Bills, Entertainment
- Shopping, Health, Education, Other

---

### 8. Totals & Summaries

**Total spent this month:**
```http
GET /api/expenses/total/current-month
```

**Total by category (all time):**
```http
GET /api/expenses/total/category/Food
```

**Total by category (current month):**
```http
GET /api/expenses/total/category/Food/current-month
```

---

## 📱 FRONTEND INTEGRATION

### React Example

```javascript
import axios from 'axios';

const API_BASE = 'http://localhost:8081/api/expenses';

// Get dashboard data
const fetchDashboard = async () => {
  const response = await axios.get(`${API_BASE}/dashboard`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.data.data;
};

// Render pie chart
const PieChart = () => {
  const [data, setData] = useState([]);
  
  useEffect(() => {
    fetchDashboard().then(dashboard => {
      setData(dashboard.categoryBreakdown);
    });
  }, []);
  
  return (
    <Pie 
      data={{
        labels: data.map(c => c.categoryName),
        datasets: [{
          data: data.map(c => c.totalAmount),
          backgroundColor: data.map(c => c.categoryColor)
        }]
      }}
    />
  );
};
```

### Angular Example

```typescript
@Injectable()
export class DashboardService {
  private apiUrl = 'http://localhost:8081/api/expenses';
  
  getDashboard(): Observable<DashboardStats> {
    return this.http.get<ApiResponse<DashboardStats>>(
      `${this.apiUrl}/dashboard`,
      { headers: this.authHeaders }
    ).pipe(map(response => response.data));
  }
}
```

### Vue.js Example

```javascript
export default {
  data() {
    return {
      dashboard: null
    }
  },
  async mounted() {
    const response = await fetch('/api/expenses/dashboard', {
      headers: {
        'Authorization': `Bearer ${this.token}`
      }
    });
    this.dashboard = (await response.json()).data;
  }
}
```

---

## 📊 CHART LIBRARY EXAMPLES

### Chart.js (Recommended)

```javascript
// Pie Chart
const ctx = document.getElementById('pieChart');
new Chart(ctx, {
  type: 'pie',
  data: {
    labels: categoryBreakdown.map(c => c.categoryName),
    datasets: [{
      data: categoryBreakdown.map(c => c.totalAmount),
      backgroundColor: categoryBreakdown.map(c => c.categoryColor)
    }]
  }
});

// Line Chart
new Chart(ctx, {
  type: 'line',
  data: {
    labels: dailySpending.map(d => d.date),
    datasets: [{
      label: 'Daily Spending',
      data: dailySpending.map(d => d.amount)
    }]
  }
});

// Bar Chart
new Chart(ctx, {
  type: 'bar',
  data: {
    labels: budgetStatus.map(b => b.categoryName),
    datasets: [{
      label: 'Spent',
      data: budgetStatus.map(b => b.spent)
    }, {
      label: 'Budget',
      data: budgetStatus.map(b => b.budgetLimit)
    }]
  }
});
```

### Recharts (React)

```jsx
import { PieChart, Pie, Cell } from 'recharts';

<PieChart width={400} height={400}>
  <Pie 
    data={categoryBreakdown} 
    dataKey="totalAmount"
    nameKey="categoryName"
    cx="50%" 
    cy="50%"
  >
    {categoryBreakdown.map((entry, index) => (
      <Cell key={`cell-${index}`} fill={entry.categoryColor} />
    ))}
  </Pie>
</PieChart>
```

### ApexCharts

```javascript
const options = {
  series: categoryBreakdown.map(c => c.totalAmount),
  labels: categoryBreakdown.map(c => c.categoryName),
  colors: categoryBreakdown.map(c => c.categoryColor)
};

const chart = new ApexCharts(document.querySelector("#chart"), options);
chart.render();
```

---

## 🎨 DASHBOARD LAYOUTS

### Recommended Charts

1. **Pie Chart** → Category Breakdown
   - Use: `/api/expenses/breakdown/category`
   - Shows: % of spending per category

2. **Line Chart** → Daily Trend
   - Use: `/api/expenses/trends/daily`
   - Shows: Spending over time

3. **Progress Bars** → Budget Status
   - Use: `/api/expenses/budget/status`
   - Shows: How much budget is used

4. **Bar Chart** → Month Comparison
   - Use: `/api/expenses/dashboard` (totalSpentThisMonth vs totalSpentLastMonth)
   - Shows: Current vs previous month

5. **Table** → Recent Transactions
   - Use: `/api/expenses/current-month`
   - Shows: List of all expenses

---

## 🧪 TESTING

### Using cURL

```bash
# Get dashboard
curl -X GET "http://localhost:8081/api/expenses/dashboard" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get category breakdown
curl -X GET "http://localhost:8081/api/expenses/breakdown/category" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get date range
curl -X GET "http://localhost:8081/api/expenses/dashboard/range?startDate=2026-01-01&endDate=2026-01-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Using Postman

1. Create new request
2. Method: GET
3. URL: `http://localhost:8081/api/expenses/dashboard`
4. Headers: `Authorization: Bearer <token>`
5. Send

---

## 🎯 QUICK START

### Step 1: Get Token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### Step 2: Fetch Dashboard
```bash
export TOKEN="your-jwt-token-here"

curl -X GET http://localhost:8081/api/expenses/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

### Step 3: Build Your Charts
Use the response data with your favorite chart library!

---

## 📖 API Summary

| Endpoint | Purpose | Chart Type |
|----------|---------|------------|
| `/dashboard` | All stats | Dashboard |
| `/breakdown/category` | Category totals | Pie Chart |
| `/trends/daily` | Daily spending | Line Chart |
| `/budget/status` | Budget usage | Progress Bars |
| `/current-month` | Transaction list | Table |
| `/category/{name}` | Filtered expenses | Table |
| `/total/current-month` | Monthly total | Number Widget |

---

## 🚀 Production Tips

1. **Cache dashboard data** (5-15 minutes)
2. **Use WebSocket** for real-time updates
3. **Lazy load** historical data
4. **Paginate** expense lists
5. **Compress** large responses (gzip)
6. **Rate limit** API calls

---

## 📚 Next Steps

1. ✅ Test endpoints with Swagger UI: `http://localhost:8081/swagger-ui.html`
2. ✅ Integrate with your frontend
3. ✅ Add charts using Chart.js/Recharts
4. ✅ Customize colors and layouts
5. ✅ Add filters and date pickers

Happy Visualizing! 📊🎨

