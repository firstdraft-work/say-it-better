#!/bin/bash
APP_DIR=/srv/communication-optimizer/backend
PID_FILE=$APP_DIR/app.pid

if [ ! -f "$PID_FILE" ]; then
    echo "Not running (no PID file)"
    exit 0
fi

PID=$(cat "$PID_FILE")
if kill -0 "$PID" 2>/dev/null; then
    echo "Stopping PID $PID..."
    kill "$PID"
    sleep 5
    if kill -0 "$PID" 2>/dev/null; then
        echo "Force killing..."
        kill -9 "$PID"
    fi
    echo "Stopped."
else
    echo "Process $PID not found, cleaning PID file."
fi
rm -f "$PID_FILE"
