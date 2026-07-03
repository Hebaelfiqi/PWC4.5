#!/usr/bin/env bash
# Build and smoke-test the PWC45 WEKA classifier.
# Requires: a JDK and a Weka jar. Set WEKA_JAR to its path.
#
#   WEKA_JAR=/path/to/weka.jar research/weka/build_and_run.sh train.arff test.arff
set -euo pipefail
cd "$(dirname "$0")"

: "${WEKA_JAR:?set WEKA_JAR to the path of weka.jar (e.g. from a Weka 3.8 install)}"
TRAIN="${1:-train.arff}"
TEST="${2:-}"
OPENS="--add-opens java.base/java.lang=ALL-UNNAMED"   # needed on Java 9+

mkdir -p out
javac -cp "$WEKA_JAR" -d out PWC45.java
echo "compiled PWC45 -> out/"

if [ -n "$TEST" ]; then
  java $OPENS -cp "$WEKA_JAR:out" weka.classifiers.trees.PWC45 -t "$TRAIN" -T "$TEST"
else
  java $OPENS -cp "$WEKA_JAR:out" weka.classifiers.trees.PWC45 -x 10 -t "$TRAIN"
fi
