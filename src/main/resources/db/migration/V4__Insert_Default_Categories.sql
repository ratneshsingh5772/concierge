-- Initialize default expense categories with budget limits
-- Run this script if categories table is empty

INSERT INTO categories (name, description, icon, color, is_active, created_at, updated_at) VALUES
('Food', 'Food, groceries, dining, and beverages', '🍔', '#FF6B6B', true, NOW(), NOW()),
('Transport', 'Transportation, fuel, parking, and travel', '🚗', '#4ECDC4', true, NOW(), NOW()),
('Bills', 'Utilities, rent, subscriptions, and recurring payments (electricity, water, internet, phone)', '📄', '#95E1D3', true, NOW(), NOW()),
('Entertainment', 'Movies, games, concerts, and leisure activities', '🎬', '#F38181', true, NOW(), NOW()),
('Shopping', 'Clothing, electronics, and retail purchases', '🛍️', '#AA96DA', true, NOW(), NOW()),
('Health', 'Healthcare, medicine, gym, and fitness', '💊', '#FCBAD3', true, NOW(), NOW()),
('Education', 'Books, courses, tuition, and learning', '📚', '#FFFFD2', true, NOW(), NOW()),
('Other', 'Miscellaneous and uncategorized expenses', '📦', '#A8D8EA', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    icon = VALUES(icon),
    color = VALUES(color),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Verify categories were inserted
SELECT
    id,
    name,
    description,
    icon,
    color,
    is_active,
    created_at
FROM categories
ORDER BY id;

-- Note: Budget limits are now:
-- Food: $200, Transport: $100, Bills: $300, Entertainment: $150
-- Shopping: $250, Health: $200, Education: $150, Other: $100

