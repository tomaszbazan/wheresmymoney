#!/bin/bash
set -e

# Check if backend parameter is provided
if [ "$1" = "backend" ]; then
    echo "🚀 Building and starting Where's My Money backend only..."
    
    echo "📦 Building backend..."
    cd backend
    ./gradlew clean build
    cd ..
    
    echo "🐳 Starting backend services with docker-compose..."
    docker-compose up --no-deps --build -d postgres backend
    
    echo "✅ Backend services started!"
    echo "Backend: http://localhost:8080"
    echo "Database: localhost:5433"
else
    echo "🚀 Building and starting Where's My Money application..."
    
    echo "📦 Building backend..."
    cd backend
    ./gradlew clean build
    cd ..
    
    echo "🐳 Starting all services with docker-compose..."
    docker-compose up --no-deps --build -d
    
    echo "✅ Application started!"
    echo "Backend: http://localhost:8080"
    echo "Frontend: http://localhost:8100"
    echo "Database: localhost:5433"
fi