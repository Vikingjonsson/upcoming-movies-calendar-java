#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/target/upcoming-movies.jar"

if [ ! -f "$JAR" ]; then
    echo "Building application with Maven..."
    "$SCRIPT_DIR/mvnw" -q -f "$SCRIPT_DIR/pom.xml" package -DskipTests
fi

exec java -jar "$JAR" "$@"
