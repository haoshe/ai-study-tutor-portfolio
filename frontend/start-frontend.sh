#!/bin/bash

echo "========================================"
echo "  Starting AI Study Assistant Frontend"
echo "========================================"
echo ""

# Check if .env exists and has polling enabled
if ! grep -q "CHOKIDAR_USEPOLLING=true" .env 2>/dev/null; then
    echo "⚠️  Adding CHOKIDAR_USEPOLLING=true to .env for compatibility..."
    echo "CHOKIDAR_USEPOLLING=true" >> .env
fi

echo "✓ File watching configured for low-resource environments"
echo ""
echo "Starting development server..."
echo "Note: You may see 'EMFILE' warnings - these are normal and can be ignored."
echo "The app will still work, but hot-reload may be slower."
echo ""

npm start
