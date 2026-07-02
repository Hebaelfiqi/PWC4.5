#!/usr/bin/env bash
# Reproduce a translator-stylometry experiment: 10 runs with the published
# random seeds, writing each run's output to run_1/ .. run_10/ (created
# automatically) under the translator-pair folder.
#
# Usage:
#   scripts/run_translators.sh [pair_folder] [exp_number]
#
# Examples:
#   scripts/run_translators.sh                      # asad_daryabadi, Exp_1
#   scripts/run_translators.sh asad_pickthall 3     # asad_pickthall, Exp_3
set -euo pipefail
cd "$(dirname "$0")/.."

PAIR="${1:-asad_daryabadi}"
EXP="${2:-1}"

# Published seeds for run_1 .. run_10 (see docs/03-using-pwc45.md)
SEEDS=(42 61 13 3 82 27 19 47 93 2)

PAIR_DIR="data/translators_data/${PAIR}"
[ -d "$PAIR_DIR" ] || { echo "Unknown pair folder: $PAIR_DIR" >&2; exit 1; }
[ -d bin ] || { echo "bin/ not found - run scripts/build.sh first" >&2; exit 1; }

# Filestem, e.g. M3_M4_Asad_Daryabadi_Exp_1 (capitalise each name part)
CAP_PAIR=$(echo "$PAIR" | awk -F_ '{for(i=1;i<=NF;i++){$i=toupper(substr($i,1,1)) substr($i,2)}; print $1"_"$2}')
STEM="M3_M4_${CAP_PAIR}_Exp_${EXP}"

for i in $(seq 1 10); do
  seed="${SEEDS[$((i-1))]}"
  outdir="${PAIR_DIR}/run_${i}"
  mkdir -p "$outdir"
  printf 'run_%-2s (seed %-2s) accuracy: ' "$i" "$seed"
  java -cp bin:lib/cli.jar PWC45 -ip "${PAIR_DIR}//" -op "${outdir}//" -f "$STEM" -u -o -b -r "$seed"
  echo
done
