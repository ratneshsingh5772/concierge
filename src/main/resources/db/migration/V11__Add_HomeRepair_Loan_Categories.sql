-- Add two new predefined categories: Home Repair and Loan
-- These categories will be available for all users to track expenses

INSERT INTO categories (name, description, icon, color, is_active, created_at, updated_at) VALUES
('Home Repair', 'Maintenance, repairs, renovations, and household fixing', '🔧', '#795548', true, NOW(), NOW()),
('Loan', 'Personal loan, home loan, car loan repayments and EMI', '🏦', '#607D8B', true, NOW(), NOW())
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
WHERE name IN ('Home Repair', 'Loan')
ORDER BY name;

