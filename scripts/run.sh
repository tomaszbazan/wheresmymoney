#!/bin/bash
set -e

if [ "$1" = "backend" ]; then
    echo "🚀 Building and starting Where's My Money backend only..."
    
    echo "📦 Building backend..."
    cd backend
    ./gradlew clean build
    cd ..
    
    echo "🐳 Starting backend services with docker-compose..."
    docker-compose up --no-deps --build -d postgres backend
    
    echo "✅ Backend services started!"
    echo "Backend: http://localhost:9080"
    echo "Database: localhost:5432"
else
    echo "🚀 Building and starting Where's My Money application..."
    
    echo "📦 Building backend..."
    cd backend
    ./gradlew clean build
    cd ..
    
    echo "🐳 Starting all services with docker-compose..."
    docker-compose up --no-deps --build -d
    
    echo "✅ Application started!"
    echo "Backend: http://localhost:9080"
    echo "Frontend: http://localhost:8000"
    echo "Database: localhost:5432"
fi