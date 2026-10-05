#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BIN="$SCRIPT_DIR/build/install/upcoming-movies/bin/upcoming-movies"

if [ ! -f "$BIN" ]; then
    echo "Installing application distribution..."
    "$SCRIPT_DIR/gradlew" -q -p "$SCRIPT_DIR" installDist
fi

exec "$BIN" "$@"
