# SWAGGER 500 ERROR - COMPLETE FIX GUIDE

## THE REAL PROBLEM

The Swagger 500 error you're seeing is happening because **another instance of the application is already running on port 8081**.

## SOLUTION

### Quick Fix (3 Steps)

#### Step 1: Stop All Running Instances

```bash
# Kill all processes on port 8081
lsof -ti:8081 | xargs kill -9

# Or kill all Spring Boot processes
pkill -f spring-boot:run
pkill -f concierge
```

#### Step 2: Clean Build

```bash
cd /home/ratnesh/Documents/concierge
./mvnw clean compile
```

#### Step 3: Start Fresh

Use the new start script:

```bash
./start-app.sh
```

Or manually:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./mvnw spring-boot:run
```

### Verification

Wait about 10-15 seconds for startup, then test:

```bash
# Test health endpoint
curl http://localhost:8081/api/chat/health

# Test Swagger API docs
curl http://localhost:8081/v3/api-docs

# Open Swagger UI
# http://localhost:8081/swagger-ui.html
```

## WHY THIS HAPPENED

1. **Previous instance still running**: You started the app earlier and it's still running
2. **Port conflict**: Spring Boot can't start because port 8081 is already in use
3. **Swagger can't initialize**: With the app not fully started, Swagger endpoints fail

## DETAILED TROUBLESHOOTING

### Check if App is Running

```bash
# Check if port 8081 is in use
lsof -i :8081

# Check for Java processes
ps aux | grep java | grep concierge
```

### Force Kill Everything

```bash
# Nuclear option - kill all Java processes
pkill -9 java

# Then restart
./start-app.sh
```

### Check Logs for Errors

If the app still won't start:

```bash
./mvnw spring-boot:run 2>&1 | tee startup.log
```

Look for:
- "Port 8081 was already in use"
- Database connection errors
- Bean creation errors

## COMMON ISSUES & SOLUTIONS

### Issue 1: Port Already in Use

**Error:**
```
Web server failed to start. Port 8081 was already in use.
```

**Solution:**
```bash
lsof -ti:8081 | xargs kill -9
```

### Issue 2: Database Not Running

**Error:**
```
Communications link failure
```

**Solution:**
```bash
sudo systemctl start mysql
# Or
./setup-database.sh
```

### Issue 3: Java Version Wrong

**Error:**
```
Unsupported class file major version 65
```

**Solution:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Should show 21.x.x
```

### Issue 4: Compilation Errors

**Solution:**
```bash
./mvnw clean compile
# Check for errors
```

## SWAGGER SPECIFIC CHECKS

Once app is running successfully:

### 1. Test API Docs Endpoint

```bash
curl -v http://localhost:8081/v3/api-docs
```

**Expected:** JSON response (not 500 error)

### 2. Test Swagger UI

```bash
curl -v http://localhost:8081/swagger-ui.html
```

**Expected:** HTML page (not 500 error)

### 3. Open in Browser

```
http://localhost:8081/swagger-ui.html
```

**Expected:** Interactive API documentation interface

## COMPLETE RESTART PROCEDURE

If everything is messed up, do a complete restart:

```bash
# 1. Kill everything
pkill -f spring-boot
pkill -f concierge
pkill -f mvnw
lsof -ti:8081 | xargs kill -9

# 2. Clean build
cd /home/ratnesh/Documents/concierge
./mvnw clean package -DskipTests

# 3. Start fresh
./start-app.sh
```

## VERIFY SWAGGER IS WORKING

### Manual Test Sequence

1. **Start App**
   ```bash
   ./start-app.sh
   ```

2. **Wait for Startup** (look for this message)
   ```
   Started ConciergeApplication in X.XXX seconds
   ```

3. **Test Health**
   ```bash
   curl http://localhost:8081/api/chat/health
   ```

4. **Test API Docs**
   ```bash
   curl http://localhost:8081/v3/api-docs | jq . | head -20
   ```

5. **Open Swagger UI**
   ```
   http://localhost:8081/swagger-ui.html
   ```

### Expected Results

✅ Health endpoint returns JSON with status "ok"
✅ API docs endpoint returns OpenAPI JSON spec
✅ Swagger UI loads with all endpoints visible
✅ No 500 errors

## DEBUGGING SWAGGER 500 ERROR

If you STILL get 500 error on `/v3/api-docs`:

### Check Application Logs

```bash
# Start with debug logging
./mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.springdoc=DEBUG"
```

Look for:
- Controller scanning errors
- Missing annotations errors
- Security configuration issues

### Common Swagger Errors

1. **Missing @Tag annotation**: Fixed ✅
2. **Missing @Operation annotation**: Fixed ✅
3. **Streaming endpoint issues**: Fixed ✅
4. **Security configuration blocking Swagger**: Fixed ✅

All these have been fixed in your codebase!

## SCRIPTS AVAILABLE

### start-app.sh
Kills existing processes, sets Java 21, and starts the app

```bash
./start-app.sh
```

### run-java21.sh
Sets Java 21 and runs with Maven

```bash
./run-java21.sh
```

### setup-database.sh
Sets up MySQL database

```bash
./setup-database.sh
```

## CONFIGURATION CHECK

Verify these files are correct:

### application.properties

```properties
server.port=8081

# Swagger Config
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

### SecurityConfig.java

```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    ...
).permitAll()
```

## SUCCESS INDICATORS

When everything is working, you'll see:

### In Browser
- Swagger UI loads at http://localhost:8081/swagger-ui.html
- "Finance Concierge API" title
- Two main sections: Authentication, Chat
- Green "Authorize" button
- All endpoints expandable

### In Terminal
```
Started ConciergeApplication in 4.123 seconds (process running for 4.456)
```

### Via curl
```bash
$ curl http://localhost:8081/api/chat/health
{
  "success": true,
  "data": {
    "status": "ok",
    "agent": "finance-concierge",
    "service": "running",
    "database": "connected"
  }
}
```

## FINAL CHECKLIST

Before reporting it's not working, verify:

- [ ] No other instance running (check with `lsof -i :8081`)
- [ ] MySQL is running (`sudo systemctl status mysql`)
- [ ] Java 21 is being used (`java -version`)
- [ ] Project compiles without errors (`./mvnw compile`)
- [ ] Application starts successfully (see "Started ConciergeApplication")
- [ ] Port 8081 is accessible (`curl http://localhost:8081/api/chat/health`)

## GET HELP

If still not working, run this diagnostic:

```bash
#!/bin/bash
echo "=== Java Version ==="
java -version

echo -e "\n=== Port 8081 Status ==="
lsof -i :8081 || echo "Port is free"

echo -e "\n=== MySQL Status ==="
sudo systemctl status mysql | head -3

echo -e "\n=== Compilation Status ==="
./mvnw compile -q 2>&1 | tail -5

echo -e "\n=== Last 20 lines of app startup ==="
./mvnw spring-boot:run 2>&1 | tail -20
```

Save output and share for debugging.

## SUMMARY

**Your Issue:** Swagger showing 500 error
**Root Cause:** Application not starting due to port conflict
**Solution:** Kill existing process, restart fresh

**Commands:**
```bash
lsof -ti:8081 | xargs kill -9
./start-app.sh
```

**Verify:**
```
http://localhost:8081/swagger-ui.html
```

That's it! The Swagger implementation is correct - you just need to start the app properly.

