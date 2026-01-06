# Flyway Migration Checksum Mismatch Fix

## Problem
```
Migration checksum mismatch for migration version 4
-> Applied to database : -1465488073
-> Resolved locally    : -42771185
```

This happens when you modify a migration file after it has already been executed.

## Solution: Update Flyway Schema History

Run this SQL query to update the checksum in the database:

```sql
-- Option 1: Update the checksum to match the new file
UPDATE flyway_schema_history 
SET checksum = -42771185 
WHERE version = '4';
```

OR

```sql
-- Option 2: Delete the V4 record and let it re-run
DELETE FROM flyway_schema_history WHERE version = '4';
```

## Execute the Fix

### Using MySQL Command Line:
```bash
mysql -u root -pconcierge -e "USE concierge; UPDATE flyway_schema_history SET checksum = -42771185 WHERE version = '4';"
```

### Or using MySQL client:
```bash
mysql -u root -pconcierge
```

Then run:
```sql
USE concierge;
UPDATE flyway_schema_history SET checksum = -42771185 WHERE version = '4';
SELECT * FROM flyway_schema_history WHERE version = '4';
```

## Verify the Fix

After running the update, check the record:
```sql
SELECT version, checksum, description, success 
FROM flyway_schema_history 
WHERE version = '4';
```

Expected output:
```
+---------+-------------+---------------------------+---------+
| version | checksum    | description               | success |
+---------+-------------+---------------------------+---------+
| 4       | -42771185   | Insert Default Categories | 1       |
+---------+-------------+---------------------------+---------+
```

## Then Restart the Application

After fixing the checksum, restart your Spring Boot application:

```bash
./mvnw spring-boot:run
```

Or in IntelliJ, just run the main class again.

## Alternative: Flyway Repair (Recommended for Production)

```bash
./mvnw flyway:repair -Dflyway.user=root -Dflyway.password=concierge -Dflyway.url=jdbc:mysql://localhost:3306/concierge
```

This will automatically fix the checksum mismatch.

## Quick One-Liner Fix

```bash
mysql -u root -pconcierge concierge -e "UPDATE flyway_schema_history SET checksum = -42771185 WHERE version = '4';"
```

Then restart your app.

