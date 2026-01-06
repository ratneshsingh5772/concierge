#!/bin/bash

echo "========================================="
echo "Restarting Personal Finance Concierge"
echo "========================================="

# Set Java 21 home
export JAVA_HOME=/home/ratnesh/.jdks/corretto-21.0.9
export PATH=$JAVA_HOME/bin:$PATH

echo "Java Home: $JAVA_HOME"
echo "Java Version:"
java -version
echo ""

# Stop any running instances
echo "Stopping existing instances..."
pkill -f concierge 2>/dev/null || true
sleep 2

# Clean and rebuild
echo "Cleaning and rebuilding..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"
echo ""
echo "Starting application..."
echo "========================================="

# Run the application
./mvnw spring-boot:run

