#!/bin/bash
set -e

# Set default values for Supabase if not provided
export SUPABASE_URL=${SUPABASE_URL:-""}
export SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY:-""}

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
    echo "Backend: http://localhost:9080"
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
    echo "Backend: http://localhost:9080"
    echo "Frontend: http://localhost:9081"
    echo "Database: localhost:5433"
    echo ""
    echo "💡 To configure Supabase, set environment variables:"
    echo "   export SUPABASE_URL=your_supabase_url"
    echo "   export SUPABASE_ANON_KEY=your_supabase_anon_key"
fi