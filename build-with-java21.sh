#!/bin/bash

echo "========================================="
echo "Building with Java 21"
echo "========================================="

# Set Java 21 home
export JAVA_HOME=/home/ratnesh/.jdks/corretto-21.0.9
export PATH=$JAVA_HOME/bin:$PATH

echo "Java Home: $JAVA_HOME"
echo "Java Version:"
java -version

echo ""
echo "Building project..."
echo "========================================="

# Clean and install
./mvnw clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ Build successful!"
    echo "========================================="
    echo ""
    echo "You can now run the application with:"
    echo "./run-with-java21.sh"
else
    echo ""
    echo "========================================="
    echo "❌ Build failed!"
    echo "========================================="
    exit 1
fi

