# Testing Guide - Expense Tracking Integration

## Prerequisites

1. ✅ Application is running on port 8081
2. ✅ Database is connected (MySQL)
3. ✅ Categories are initialized (run V4 migration)
4. ✅ User is registered and logged in
5. ✅ JWT token is obtained

## Step 1: Initialize Categories

Run the migration or execute manually:

```sql
-- Check if categories exist
SELECT * FROM categories;

-- If empty, run:
INSERT INTO categories (name, description, icon, color, is_active, created_at, updated_at) VALUES
('Food', 'Food and dining', '🍔', '#FF6B6B', true, NOW(), NOW()),
('Transport', 'Transportation', '🚗', '#4ECDC4', true, NOW(), NOW()),
('Bills', 'Bills and utilities', '📄', '#95E1D3', true, NOW(), NOW()),
('Entertainment', 'Entertainment', '🎬', '#F38181', true, NOW(), NOW()),
('Shopping', 'Shopping', '🛍️', '#AA96DA', true, NOW(), NOW()),
('Health', 'Health and fitness', '💊', '#FCBAD3', true, NOW(), NOW()),
('Education', 'Education', '📚', '#FFFFD2', true, NOW(), NOW()),
('Other', 'Other expenses', '📦', '#A8D8EA', true, NOW(), NOW());
```

## Step 2: Register/Login to Get Token

### Register (if needed):
```bash
curl -X 'POST' \
  'http://localhost:8081/api/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "test@example.com",
  "password": "password123"
}'
```

### Login:
```bash
curl -X 'POST' \
  'http://localhost:8081/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "test@example.com",
  "password": "password123"
}'
```

Save the `accessToken` from the response.

## Step 3: Test Expense Messages

### Test 1: Food Expense

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "I spent $15 on coffee at Starbucks",
  "userId": "1"
}'
```

**Expected:**
- ✅ Response: AI message about the expense
- ✅ Database: Expense record with category "Food", amount 15.00
- ✅ Logs: "Expense saved successfully"

**Verify:**
```sql
SELECT e.*, c.name as category 
FROM expenses e 
JOIN categories c ON e.category_id = c.id 
WHERE e.user_id = 1 
ORDER BY e.created_at DESC 
LIMIT 1;
```

### Test 2: Transport Expense

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Paid $50 for Uber ride home",
  "userId": "1"
}'
```

**Expected:**
- ✅ Category: Transport
- ✅ Amount: 50.00
- ✅ original_message: "Paid $50 for Uber ride home"
- ✅ ai_parsed: true

### Test 3: Bills Expense

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Electricity bill was $100",
  "userId": "1"
}'
```

**Expected:**
- ✅ Category: Bills
- ✅ Amount: 100.00

### Test 4: Entertainment Expense

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Movie tickets cost $25",
  "userId": "1"
}'
```

**Expected:**
- ✅ Category: Entertainment
- ✅ Amount: 25.00

### Test 5: Multiple Expenses

```bash
# First expense
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Bought lunch for $12.50",
  "userId": "1"
}'

# Second expense
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Gas cost $40",
  "userId": "1"
}'

# Third expense
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Gym membership $60",
  "userId": "1"
}'
```

## Step 4: Verify in Database

### Check All Expenses

```sql
SELECT 
    e.id,
    u.email as user,
    c.name as category,
    e.amount,
    e.currency,
    e.description,
    e.original_message,
    e.expense_date,
    e.ai_parsed,
    e.ai_confidence,
    e.created_at
FROM expenses e
JOIN users u ON e.user_id = u.id
JOIN categories c ON e.category_id = c.id
WHERE u.id = 1
ORDER BY e.created_at DESC;
```

### Check Totals by Category

```sql
SELECT 
    c.name as category,
    c.icon,
    COUNT(e.id) as transaction_count,
    SUM(e.amount) as total_spent,
    AVG(e.amount) as average_amount,
    MIN(e.amount) as min_amount,
    MAX(e.amount) as max_amount
FROM expenses e
JOIN categories c ON e.category_id = c.id
WHERE e.user_id = 1
GROUP BY c.id, c.name, c.icon
ORDER BY total_spent DESC;
```

### Check This Month's Expenses

```sql
SELECT 
    c.name as category,
    SUM(e.amount) as total_spent
FROM expenses e
JOIN categories c ON e.category_id = c.id
WHERE e.user_id = 1
  AND MONTH(e.expense_date) = MONTH(CURRENT_DATE())
  AND YEAR(e.expense_date) = YEAR(CURRENT_DATE())
GROUP BY c.name
ORDER BY total_spent DESC;
```

### Check AI-Parsed Expenses

```sql
SELECT 
    e.*,
    c.name as category
FROM expenses e
JOIN categories c ON e.category_id = c.id
WHERE e.user_id = 1
  AND e.ai_parsed = true
ORDER BY e.ai_confidence DESC;
```

## Step 5: Test Error Handling

### Test 1: Non-Expense Message

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "What is my budget?",
  "userId": "1"
}'
```

**Expected:**
- ✅ AI response returned
- ✅ NO expense created (message doesn't contain expense)
- ✅ Log: "Message doesn't appear to be an expense"

### Test 2: Ambiguous Message

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "I bought something",
  "userId": "1"
}'
```

**Expected:**
- ✅ AI response returned
- ✅ Parsing might fail (no amount)
- ✅ Chat continues normally

### Test 3: Invalid Amount

```bash
curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "I spent lots of money on food",
  "userId": "1"
}'
```

**Expected:**
- ✅ Parsing fails gracefully
- ✅ Chat response still works

## Step 6: Check Application Logs

### Successful Expense Creation

```
INFO - Processing message (JSON mode): Paid $50 for Uber
INFO - Detected expense in message, saving: ParsedExpenseDTO(amount=50.00, category=Transport, ...)
INFO - Creating expense for user: 1 with data: ParsedExpenseDTO(...)
INFO - Expense created successfully with ID: 5
INFO - Expense saved successfully with ID: 5 - Amount: 50.00 Category: Transport
INFO - Chat history saved for user: 1
```

### Non-Expense Message

```
INFO - Processing message (JSON mode): What's my budget?
DEBUG - Message doesn't appear to be an expense: What's my budget?
INFO - Chat history saved for user: 1
```

### Parsing Failure

```
WARN - Failed to parse expense message: I bought something
DEBUG - Could not parse expense from message: Missing amount
INFO - Chat history saved for user: 1
```

## Step 7: Performance Check

### Check Response Time

```bash
time curl -X 'POST' \
  'http://localhost:8081/api/chat/message/json' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Content-Type: application/json' \
  -d '{
  "message": "Spent $20 on dinner",
  "userId": "1"
}'
```

**Expected:**
- ✅ Response time: < 3 seconds
- ✅ No timeout errors

### Check Database Performance

```sql
-- Check slow queries
SELECT 
    event_name,
    count_star,
    avg_timer_wait/1000000000000 as avg_time_seconds
FROM performance_schema.events_statements_summary_by_digest
WHERE schema_name = 'concierge'
ORDER BY avg_timer_wait DESC
LIMIT 10;
```

## Troubleshooting

### Issue: Expenses Not Saving

**Check:**
1. Categories exist in database
   ```sql
   SELECT COUNT(*) FROM categories WHERE is_active = true;
   ```
2. User ID is valid
   ```sql
   SELECT * FROM users WHERE id = 1;
   ```
3. Application logs show errors
   ```bash
   tail -f app.log | grep -i "expense"
   ```

### Issue: Wrong Category Assigned

**Check:**
1. Category mapping in `CategoryMappingUtil.java`
2. Message contains clear keywords
3. AI response in logs

**Fix:**
```java
// Update CategoryMappingUtil.java to add more keywords
KEYWORD_CATEGORY_MAP.put("your_keyword", "Correct_Category");
```

### Issue: Parsing Always Fails

**Check:**
1. Google API key is configured
   ```bash
   echo $GOOGLE_API_KEY
   ```
2. Regex fallback is working
3. Message format is correct

## Success Metrics

After testing, verify:

- [ ] ✅ At least 5 different expenses created
- [ ] ✅ All 4 main categories used (Food, Transport, Bills, Entertainment)
- [ ] ✅ Expenses have correct amounts
- [ ] ✅ Categories are mapped correctly
- [ ] ✅ original_message is stored
- [ ] ✅ ai_parsed = true
- [ ] ✅ Chat history also saved
- [ ] ✅ No application errors
- [ ] ✅ Response time < 3 seconds
- [ ] ✅ Database queries are efficient

## Next Steps

1. ✅ Test with real users
2. ✅ Monitor for edge cases
3. ✅ Collect feedback on category mapping
4. ✅ Add more categories if needed
5. ✅ Implement expense query endpoints
6. ✅ Add data visualization
7. ✅ Set up budget limits and alerts

## Support

If you encounter issues:

1. Check logs: `tail -f app.log`
2. Verify database: Run SQL queries above
3. Test parser directly: Unit test `ExpenseAIParserService`
4. Review documentation: `EXPENSE_AI_PARSER_GUIDE.md`
5. Check solution summary: `EXPENSE_TRACKING_SOLUTION.md`

