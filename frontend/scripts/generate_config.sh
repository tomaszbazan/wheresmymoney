#!/bin/bash

# Generate Supabase configuration from environment variables
# This script is used in CI/CD environments like GitHub Actions

set -e

if [ -z "$SUPABASE_URL" ] || [ -z "$SUPABASE_ANON_KEY" ]; then
    echo "❌ Error: SUPABASE_URL and SUPABASE_ANON_KEY environment variables must be set"
    exit 1
fi

echo "🔧 Generating Supabase configuration..."

# Navigate to the Flutter frontend directory if not already there
if [ ! -f "pubspec.yaml" ]; then
    if [ -f "frontend/pubspec.yaml" ]; then
        cd frontend
    else
        echo "❌ Error: Not in Flutter project directory"
        exit 1
    fi
fi

# Create config directory if it doesn't exist
mkdir -p assets/config

# Generate JSON configuration file
cat > assets/config/supabase.json << EOF
{
  "url": "$SUPABASE_URL",
  "anonKey": "$SUPABASE_ANON_KEY"
}
EOF

echo "✅ Supabase configuration file generated successfully"