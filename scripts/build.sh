#!/usr/bin/env bash
# Compile PWC4.5 into bin/ (requires a JDK, tested with Java 17).
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p bin
javac -d bin -cp lib/cli.jar src/*.java
echo "Compiled to bin/"
