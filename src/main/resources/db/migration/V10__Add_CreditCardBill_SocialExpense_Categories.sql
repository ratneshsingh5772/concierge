-- Add two new predefined categories: Credit Card Bill and Social Expense
-- These categories will be available for all users to track expenses

INSERT INTO categories (name, description, icon, color, is_active, created_at, updated_at) VALUES
('Credit Card Bill', 'Payment of credit card bills and dues', '💳', '#E91E63', true, NOW(), NOW()),
('Social Expense', 'Gifts, donations, parties, and social gatherings', '🎁', '#AB47BC', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    icon = VALUES(icon),
    color = VALUES(color),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Verify new categories were inserted
SELECT
    id,
    name,
    description,
    icon,
    color,
    is_active,
    created_at
FROM categories
WHERE name IN ('Credit Card Bill', 'Social Expense')
ORDER BY name;

