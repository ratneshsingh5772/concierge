# Quick Reference - Expense Tracking

## 🚀 Quick Start (3 Steps)

### 1. Initialize Categories
```sql
SOURCE src/main/resources/db/migration/V4__Insert_Default_Categories.sql;
```

### 2. Send Test Message
```bash
curl -X POST http://localhost:8081/api/chat/message/json \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "I spent $25 on lunch", "userId": "1"}'
```

### 3. Verify Result
```sql
SELECT * FROM expenses WHERE user_id = 1 ORDER BY created_at DESC LIMIT 1;
```

---

## 📝 Message Examples

| Message | Category | Amount |
|---------|----------|--------|
| "I spent $15 on coffee" | Food | 15.00 |
| "Paid $50 for Uber" | Transport | 50.00 |
| "Movie tickets $25" | Entertainment | 25.00 |
| "Electricity bill $100" | Bills | 100.00 |

---

## 🗂️ Categories

| Category | Keywords | Icon |
|----------|----------|------|
| **Food** | coffee, lunch, dinner, restaurant | 🍔 |
| **Transport** | uber, taxi, bus, gas | 🚗 |
| **Bills** | electricity, water, internet, rent | 📄 |
| **Entertainment** | movie, concert, game | 🎬 |
| **Shopping** | clothes, shoes, amazon | 🛍️ |
| **Health** | doctor, medicine, gym | 💊 |
| **Education** | book, course, tuition | 📚 |
| **Other** | misc, other | 📦 |

---

## 🔍 Useful Queries

### View All Expenses
```sql
SELECT e.*, c.name FROM expenses e 
JOIN categories c ON e.category_id = c.id 
WHERE e.user_id = 1;
```

### Total by Category
```sql
SELECT c.name, SUM(e.amount) as total 
FROM expenses e 
JOIN categories c ON e.category_id = c.id 
WHERE e.user_id = 1 
GROUP BY c.name;
```

### This Month's Expenses
```sql
SELECT * FROM expenses 
WHERE user_id = 1 
  AND MONTH(expense_date) = MONTH(NOW()) 
  AND YEAR(expense_date) = YEAR(NOW());
```

---

## 🐛 Troubleshooting

### Expense Not Saving?
1. Check categories exist: `SELECT * FROM categories;`
2. Check logs: `tail -f app.log | grep -i expense`
3. Verify user ID: `SELECT * FROM users WHERE id = 1;`

### Wrong Category?
- Add keywords to `CategoryMappingUtil.java`
- Restart application

### AI Not Working?
- Check `GOOGLE_API_KEY` is set
- Falls back to regex automatically

---

## 📚 Documentation

- **Full Guide:** `EXPENSE_AI_PARSER_GUIDE.md`
- **Solution:** `EXPENSE_TRACKING_SOLUTION.md`
- **Testing:** `TESTING_GUIDE.md`
- **Summary:** `COMPLETE_SUMMARY.md`

---

## ✅ Verification Checklist

- [ ] Categories initialized
- [ ] Test message sent
- [ ] Expense appears in database
- [ ] Category mapped correctly
- [ ] Amount is correct
- [ ] Chat response received
- [ ] No errors in logs

---

**Need Help?** Check the full documentation files above!

