-- Add three new predefined categories: Grocery, Investment, and Insurance
-- These categories will be available for all users to track expenses

INSERT INTO categories (name, description, icon, color, is_active, created_at, updated_at) VALUES
('Grocery', 'Grocery shopping, supermarket, and household items', '🛒', '#66BB6A', true, NOW(), NOW()),
('Investment', 'Stocks, mutual funds, SIP, and investment contributions', '💰', '#FFA726', true, NOW(), NOW()),
('Insurance', 'Life insurance, health insurance, vehicle insurance premiums', '🛡️', '#42A5F5', true, NOW(), NOW())
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
WHERE name IN ('Grocery', 'Investment', 'Insurance')
ORDER BY name;

