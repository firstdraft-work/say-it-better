#!/bin/bash
APP_DIR=/srv/communication-optimizer/backend
JAR_NAME=communication-optimizer-backend-0.0.1-SNAPSHOT.jar
PID_FILE=$APP_DIR/app.pid
LOG_FILE=$APP_DIR/app.log

cd "$APP_DIR" || exit 1

# Stop existing instance if running
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "Stopping existing process (PID: $OLD_PID)..."
        kill "$OLD_PID"
        sleep 3
        if kill -0 "$OLD_PID" 2>/dev/null; then
            kill -9 "$OLD_PID"
        fi
    fi
    rm -f "$PID_FILE"
fi

# Load env
set -a
source "$APP_DIR/.env"
set +a

# Start
echo "Starting $JAR_NAME..."
nohup /usr/bin/java -Xms256m -Xmx512m \
    -jar "$APP_DIR/$JAR_NAME" \
    --spring.profiles.active=prod \
    >> "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
echo "Started. PID: $(cat "$PID_FILE"), Log: $LOG_FILE"
