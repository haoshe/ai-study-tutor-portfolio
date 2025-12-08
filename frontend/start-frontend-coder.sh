#!/bin/bash
# Start frontend on Coder server
# The frontend will be accessible at https://hung-na-04-3000.hung-na-04.coder.scss.tcd.ie

cd "$(dirname "$0")"

echo "======================================"
echo "Starting Frontend on Coder Server"
echo "======================================"
echo ""
echo "Frontend will be available at:"
echo "https://hung-na-04-3000.hung-na-04.coder.scss.tcd.ie"
echo ""
echo "Backend API: http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Disable auto-open browser and start
BROWSER=none npm start
