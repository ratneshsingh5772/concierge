# Frontend Implementation Prompt: Category Management

## Objective
Implement a "Categories" management page in the frontend application where users can view, add, edit, and delete expense categories.

## Context
The backend has provided REST APIs for managing categories. Categories define how user expenses are grouped (e.g., Food, Transport, Bills).

## API Endpoints
Base URL: `/api/categories`

### 1. Fetch All Categories
- **Method**: `GET /api/categories`
- **Response**:
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Food",
      "description": "Dining out and groceries",
      "icon": "fast-food",
      "color": "#FF5733"
    }
  ]
}
```

### 2. Create Category
- **Method**: `POST /api/categories`
- **Body**:
```json
{
  "name": "Entertainment",
  "description": "Movies and Fun",
  "icon": "movie",
  "color": "#33FF57"
}
```
- **Response**: `201 Created` with the created object in `data`.

### 3. Update Category
- **Method**: `PUT /api/categories/{id}`
- **Body**: Same structure as Create.
- **Response**: `200 OK` with updated object.

### 4. Delete Category
- **Method**: `DELETE /api/categories/{id}`
- **Response**: `200 OK`

## Functional Requirements

### 1. Category List View
- Display a list of categories (Table or Card Grid).
- Columns/Fields: Name, Description, Icon (if possible), Color (show a color swatch).
- Actions per row: "Edit", "Delete".

### 2. Add New Category
- Provide a button "Add Category" that opens a Modal or navigates to a form.
- **Form Fields**:
  - **Name** (Required): Text input.
  - **Description**: Textarea/Input.
  - **Icon**: Text input (or an icon picker if a library is used).
  - **Color**: Color picker input (HTML5 `<input type="color">` or a UI component).
- Validate that Name is not empty.
- On success, refresh the list or append the new item.

### 3. Edit Category
- Clicking "Edit" should open the form pre-filled with the category data.
- Submitting sends a `PUT` request.
- On success, update the item in the list.

### 4. Delete Category
- Clicking "Delete" should show a confirmation dialog ("Are you sure?").
- On confirmation, send `DELETE` request.
- On success, remove the item from the list.

## Technical Considerations
- Use the existing Axios/Fetch instance with the Authorization header.
- Handle loading states (skeletons or spinners).
- Handle error states (show toast notifications/alerts).
- Clean and responsive UI (React/Bootstrap/Tailwind as per project).

