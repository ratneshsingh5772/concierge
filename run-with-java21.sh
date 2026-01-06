#!/bin/bash

echo "========================================="
echo "Running with Java 21"
echo "========================================="

# Set Java 21 home
export JAVA_HOME=/home/ratnesh/.jdks/corretto-21.0.9
export PATH=$JAVA_HOME/bin:$PATH

echo "Java Home: $JAVA_HOME"
echo "Java Version:"
java -version

echo ""
echo "Starting Spring Boot application..."
echo "========================================="

# Run Spring Boot
./mvnw spring-boot:run

