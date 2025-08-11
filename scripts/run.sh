#!/bin/bash
set -e

echo "🚀 Building and starting Where's My Money application..."

echo "📦 Building backend..."
cd backend
./gradlew clean build
cd ..

echo "🐳 Starting all services with docker-compose..."
docker-compose up --build

echo "✅ Application started!"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:3000"