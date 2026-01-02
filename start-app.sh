#!/bin/bash

echo "==================================================="
echo "Finance Concierge - Application Starter"
echo "==================================================="
echo ""

# Kill any existing processes on port 8081
echo "Step 1: Checking for existing processes on port 8081..."
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Port 8081 is in use. Killing existing process..."
    lsof -ti:8081 | xargs kill -9 2>/dev/null
    sleep 2
    echo "✅ Port 8081 freed"
else
    echo "✅ Port 8081 is available"
fi

# Set Java 21
echo ""
echo "Step 2: Setting Java 21..."
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
echo "Java version:"
java -version 2>&1 | head -1
echo ""

# Start the application
echo "Step 3: Starting Finance Concierge..."
echo "==================================================="
echo ""

cd "$(dirname "$0")"
./mvnw spring-boot:run

