# Category Management - Copilot Instructions

## CRITICAL RULE: Categories are PREDEFINED ONLY

### ❌ Categories CANNOT be:
- Created by users
- Modified by users
- Deleted by users
- Made user-specific
- Customized per user

### ✅ Categories CAN ONLY be:
- Read by all users (GET endpoint only)
- Predefined in the database
- Managed by database administrators
- Shared globally across all users

---

## Implementation Rules

### 1. Controller Layer

**ALLOWED**:
```java
@GetMapping("/api/categories")
public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
    // READ-ONLY: Returns predefined categories
    return categoryService.getAllCategories();
}
```

**FORBIDDEN** - Never implement these:
```java
// ❌ NEVER create user-scoped category endpoints
@PostMapping("/api/users/{userId}/categories")       // ❌ FORBIDDEN
@PostMapping("/api/categories")                      // ❌ FORBIDDEN
@PutMapping("/api/categories/{id}")                  // ❌ FORBIDDEN
@PutMapping("/api/users/{userId}/categories/{id}")   // ❌ FORBIDDEN
@DeleteMapping("/api/categories/{id}")               // ❌ FORBIDDEN
@DeleteMapping("/api/users/{userId}/categories/{id}") // ❌ FORBIDDEN
```

### 2. Service Layer

**CategoryService Interface**:
```java
public interface CategoryService {
    // ✅ ONLY this method is allowed
    List<CategoryResponseDTO> getAllCategories();
    
    // ❌ NEVER add these methods:
    // CategoryResponseDTO createCategory(...)
    // CategoryResponseDTO updateCategory(...)
    // void deleteCategory(...)
    // CategoryResponseDTO createCategoryForUser(...)
}
```

### 3. Entity Layer

**Category Entity**:
```java
@Entity
@Table(name = "categories")
public class Category {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Boolean isActive;
    
    // ❌ NEVER add these fields:
    // private User user;           // No user ownership
    // private Long userId;         // No user reference
    // private Boolean isSystem;    // All categories are system
    // private Boolean isCustom;    // No custom categories
}
```

### 4. Database Schema

**Categories Table Structure**:
```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),
    color VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- ❌ NEVER add these columns:
-- user_id BIGINT              -- No user ownership
-- is_system BOOLEAN            -- All are system categories
-- is_custom BOOLEAN            -- No custom categories
```

**Default Categories**:
```sql
-- These are the ONLY categories allowed:
INSERT INTO categories (name, description, icon, color) VALUES
('Food', 'Food and beverages', '🍔', '#FF6B6B'),
('Transport', 'Transportation costs', '🚗', '#4ECDC4'),
('Bills', 'Utility bills and rent', '📄', '#95E1D3'),
('Entertainment', 'Movies, games, leisure', '🎬', '#F38181'),
('Shopping', 'Clothing, electronics', '🛍️', '#AA96DA'),
('Health', 'Healthcare and fitness', '💊', '#FCBAD3'),
('Education', 'Books, courses, tuition', '📚', '#FFFFD2'),
('Other', 'Miscellaneous expenses', '📦', '#A8D8EA');
```

---

## Code Review Checklist

When reviewing code related to categories, ensure:

- [ ] No POST endpoints for categories
- [ ] No PUT/PATCH endpoints for categories
- [ ] No DELETE endpoints for categories
- [ ] No user_id field in Category entity
- [ ] No user relationship in Category entity
- [ ] No custom category creation in service layer
- [ ] CategoryService only has getAllCategories() method
- [ ] No database migrations adding user ownership to categories
- [ ] Categories are fetched globally, not per user
- [ ] No "user-scoped category" controllers exist

---

## Why Categories Must Be Predefined

### Business Reasons:
1. **Consistency**: All users see the same categories
2. **Analytics**: Aggregated spending data across all users
3. **Simplicity**: No category management UI needed
4. **Data Quality**: Prevents category proliferation
5. **Reporting**: Standard reports across all users

### Technical Reasons:
1. **Performance**: No complex user-category joins
2. **Simplicity**: Single source of truth
3. **Maintenance**: Easy to manage 8 categories vs thousands
4. **Budgeting**: Budget templates work for everyone
5. **AI Training**: Consistent category classification

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Adding User Ownership
```java
// ❌ WRONG - Never add user relationship
@Entity
public class Category {
    @ManyToOne
    private User user;  // ❌ FORBIDDEN
}
```

### ❌ Mistake 2: User-Scoped Endpoints
```java
// ❌ WRONG - Never create user-scoped category endpoints
@GetMapping("/api/users/{userId}/categories")
public List<CategoryResponseDTO> getUserCategories(@PathVariable Long userId) {
    // ❌ FORBIDDEN
}
```

### ❌ Mistake 3: Category CRUD Operations
```java
// ❌ WRONG - Never allow category modification
@PostMapping("/api/categories")
public CategoryResponseDTO createCategory(@RequestBody CategoryRequestDTO dto) {
    // ❌ FORBIDDEN
}

@PutMapping("/api/categories/{id}")
public CategoryResponseDTO updateCategory(@PathVariable Long id, ...) {
    // ❌ FORBIDDEN
}

@DeleteMapping("/api/categories/{id}")
public void deleteCategory(@PathVariable Long id) {
    // ❌ FORBIDDEN
}
```

### ✅ Correct Implementation
```java
// ✅ CORRECT - Only read-only access
@GetMapping("/api/categories")
public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
    List<CategoryResponseDTO> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved"));
}
```

---

## If User Requests Category Features

### User Request: "I want to add custom categories"
**Response**: Categories are predefined system-wide. Use the existing 8 categories (Food, Transport, Bills, Entertainment, Shopping, Health, Education, Other). The "Other" category can be used for miscellaneous expenses.

### User Request: "Can I rename a category?"
**Response**: No. Categories are standard across all users. This ensures consistency in reporting and analytics.

### User Request: "I need a 'Pets' category"
**Response**: Use the "Other" category for pet-related expenses, or if this is a common request, an administrator can add it to the system for all users.

### Developer Request: "Add user-specific categories"
**Response**: This goes against the system design. Categories are intentionally global to maintain data consistency and simplify analytics. If customization is needed, consider using expense tags or descriptions instead.

---

## Alternative Solutions for Customization

If users need more granularity without custom categories:

### 1. Use Expense Descriptions
```java
// Users can add detailed descriptions
Expense expense = Expense.builder()
    .category(foodCategory)  // Predefined category
    .description("Pet food for dog")  // Custom description
    .build();
```

### 2. Use Tags (if implemented)
```java
// Add tags for additional classification
expense.setTags(Arrays.asList("pet", "recurring", "essential"));
```

### 3. Use Subcategories (if needed in future)
```sql
-- Future enhancement: Add subcategories under main categories
CREATE TABLE subcategories (
    id BIGINT PRIMARY KEY,
    category_id BIGINT REFERENCES categories(id),
    name VARCHAR(50),
    -- Still predefined, not user-created
);
```

---

## Admin-Only Category Management

If categories need to be added/modified:

### Process:
1. **Only database administrators** can modify categories
2. Use database migrations (Flyway)
3. New categories are added for **ALL users**
4. Changes require application restart

### Example Migration:
```sql
-- V9__Add_Pets_Category.sql
INSERT INTO categories (name, description, icon, color, is_active)
VALUES ('Pets', 'Pet care and supplies', '🐾', '#FFB6C1', TRUE);
```

---

## Summary

### ✅ DO:
- Keep categories predefined and global
- Provide read-only GET endpoint
- Use existing 8 categories
- Let admins manage via database migrations

### ❌ DON'T:
- Add user ownership to categories
- Create category CRUD endpoints
- Allow user-specific categories
- Add user_id field to Category entity
- Create UserCategoryController

### Remember:
**Categories = Predefined System Constants**  
**NOT user-customizable resources**

---

*This is a hard requirement. Do not implement user-specific category features.*

