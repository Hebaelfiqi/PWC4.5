#!/usr/bin/env bash
# Build the installable WEKA package zip for PWC45.
#   WEKA_JAR=/path/to/weka.jar research/weka/build_package.sh [outdir]
# Produces: <outdir>/pwc45-<version>.zip  (install via WEKA Package Manager
# "Unofficial - File/URL", or submit to the WEKA team for official listing).
set -euo pipefail
cd "$(dirname "$0")"
: "${WEKA_JAR:?set WEKA_JAR to the path of weka.jar}"
OUT="${1:-dist}"
VERSION=$(grep '^Version=' Description.props | cut -d= -f2)

rm -rf build "$OUT"; mkdir -p build/classes "$OUT/pwc45"
javac -cp "$WEKA_JAR" -d build/classes PWC45.java
jar cf "$OUT/pwc45/pwc45.jar" -C build/classes .
cp Description.props "$OUT/pwc45/Description.props"
mkdir -p "$OUT/pwc45/doc"
cp README.md "$OUT/pwc45/doc/README.md"
(cd "$OUT/pwc45" && zip -qr "../pwc45-$VERSION.zip" .)
echo "built $OUT/pwc45-$VERSION.zip"
unzip -l "$OUT/pwc45-$VERSION.zip"
