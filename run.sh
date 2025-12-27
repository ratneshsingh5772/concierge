#!/bin/bash

# Personal Finance Concierge - Run Script

echo "=========================================="
echo "  Starting Personal Finance Concierge"
echo "=========================================="
echo ""

# Check if GOOGLE_API_KEY is set
if [ -z "$GOOGLE_API_KEY" ]; then
    echo "⚠️  GOOGLE_API_KEY is not set!"
    echo ""
    # Try to load from .env if it exists
    if [ -f .env ]; then
        echo "Loading from .env file..."
        source .env
        if [ -z "$GOOGLE_API_KEY" ]; then
            echo "❌ .env file exists but GOOGLE_API_KEY is not set in it."
            exit 1
        fi
        echo "✓ API key loaded from .env"
    else
        echo "Please run ./setup.sh first or set GOOGLE_API_KEY:"
        echo "  export GOOGLE_API_KEY=\"your-api-key-here\""
        exit 1
    fi
else
    echo "✓ GOOGLE_API_KEY is set"
fi

echo ""
echo "Starting application on port 8081..."
echo "Web UI will be available at: http://localhost:8081"
echo "Press Ctrl+C to stop"
echo ""

./mvnw spring-boot:run

