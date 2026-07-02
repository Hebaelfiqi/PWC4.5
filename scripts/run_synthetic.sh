#!/usr/bin/env bash
# Run PWC4.5 on the synthetic datasets.
#
# Usage:
#   scripts/run_synthetic.sh [2d|5d] [exp_folder] [noise]
#
# Examples:
#   scripts/run_synthetic.sh                 # 2d, 1st_exp, all noise levels
#   scripts/run_synthetic.sh 5d 3rd_exp      # 5d, 3rd_exp, all noise levels
#   scripts/run_synthetic.sh 2d 1st_exp 0.1  # single noise level
#
# Output files are written next to the input data (same behaviour as the
# published commands). Accuracy on the test set is printed per run.
set -euo pipefail
cd "$(dirname "$0")/.."

DIM="${1:-2d}"
EXP="${2:-1st_exp}"
NOISE_LEVELS=("${3:-0.0}" )
if [ -z "${3:-}" ]; then
  NOISE_LEVELS=(0.0 0.01 0.025 0.05 0.1 0.15 0.2 0.25)
fi

PREFIX="$(echo "$DIM" | tr '[:lower:]' '[:upper:]')"   # 2D / 5D

[ -d bin ] || { echo "bin/ not found - run scripts/build.sh first" >&2; exit 1; }

for n in "${NOISE_LEVELS[@]}"; do
  stem="${PREFIX}_Noise_${n}"
  printf '%-20s accuracy: ' "$stem"
  java -cp bin:lib/cli.jar PWC45 -ip "data//${DIM}_data//${EXP}//" -f "$stem" -u
  echo
done
