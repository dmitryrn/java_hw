#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

REQUESTS="${REQUESTS:-1000}"
WARMUP_REQUESTS="${WARMUP_REQUESTS:-100}"
BASE_URL="${BASE_URL:-http://localhost:8080/api/messages}"
RUN_ID="$(date +%Y%m%d-%H%M%S)"
RUN_DIR="$ROOT_DIR/profiling-artifacts/$RUN_ID"
mkdir -p "$RUN_DIR"

command -v jcmd >/dev/null || { echo "jcmd is required (use a JDK)" >&2; exit 1; }
command -v jfr >/dev/null || { echo "jfr is required (use a JDK)" >&2; exit 1; }
command -v curl >/dev/null || { echo "curl is required" >&2; exit 1; }

if [[ -z "${APP_PID:-}" ]]; then
    APP_PID="$(jcmd -l | awk '$2 == "org.example.java_8.Java8Application" { print $1; exit }')"
fi
if [[ -z "$APP_PID" ]]; then
    echo "Could not find Java8Application. Start it first with ./gradlew bootRun." >&2
    exit 1
fi

curl --fail --silent --show-error "$BASE_URL?page=0&size=1" >/dev/null || {
    echo "App is not responding at $BASE_URL. Start it with ./gradlew bootRun." >&2
    exit 1
}

echo "Seeding test messages..."
./gradlew --console=plain runTestDataInitializer

request_endpoint() {
    local endpoint="$1"
    local count="$2"
    for ((i = 0; i < count; i++)); do
        curl --fail --silent --show-error "$BASE_URL/$endpoint" >/dev/null
    done
}

echo "Warming up both endpoints (${WARMUP_REQUESTS} requests each)..."
request_endpoint nplus1 "$WARMUP_REQUESTS"
request_endpoint optimized "$WARMUP_REQUESTS"

record_endpoint() {
    local name="$1"
    local endpoint="$2"
    local recording="$RUN_DIR/$name.jfr"

    echo "Recording $endpoint (${REQUESTS} requests)..."
    jcmd "$APP_PID" JFR.start "name=$name" settings=profile "filename=$recording"
    request_endpoint "$endpoint" "$REQUESTS"
    jcmd "$APP_PID" JFR.stop "name=$name"
    jfr summary "$recording" > "$RUN_DIR/$name-summary.txt"
}

record_endpoint nplus1 nplus1
record_endpoint optimized optimized

python3 "$ROOT_DIR/scripts/render_jfr_report.py" \
    --nplus1 "$RUN_DIR/nplus1.jfr" \
    --optimized "$RUN_DIR/optimized.jfr" \
    --output "$ROOT_DIR/profiling-artifacts/index.html" \
    --run-id "$RUN_ID" \
    --requests "$REQUESTS"

echo "Done. Open profiling-artifacts/index.html in a browser."
echo "JFR recordings: $RUN_DIR/"
