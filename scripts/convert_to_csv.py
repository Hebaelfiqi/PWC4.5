#!/usr/bin/env python3
"""Convert a C4.5-format dataset (.data/.test + .names) to CSV.

The pairwise structure is preserved with an explicit pair_id column:
consecutive line pairs (p1, p2) share a pair_id, and a `member` column
marks p1/p2.

Usage:
    scripts/convert_to_csv.py data/2d_data/1st_exp/2D_Noise_0.0.data
    scripts/convert_to_csv.py data/translators_data/asad_daryabadi/M3_M4_Asad_Daryabadi_Exp_1.test -o out.csv
"""
import argparse
import csv
import sys
from pathlib import Path


def read_names(names_path: Path):
    classes, attributes = [], []
    for raw in names_path.read_text().splitlines():
        line = raw.strip().rstrip(".")
        if not line or line.startswith("|"):
            continue
        if not classes:
            classes = [c.strip() for c in line.split(",")]
            continue
        attributes.append(line.split(":")[0].strip())
    return classes, attributes


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("datafile", type=Path, help=".data or .test file")
    ap.add_argument("-o", "--output", type=Path, default=None,
                    help="output CSV path (default: alongside input)")
    args = ap.parse_args()

    names_path = args.datafile.with_suffix(".names")
    if not names_path.exists():
        sys.exit(f"names file not found: {names_path}")
    classes, attributes = read_names(names_path)

    out_path = args.output or args.datafile.with_suffix(
        args.datafile.suffix + ".csv")
    n_rows = 0
    with args.datafile.open() as f, out_path.open("w", newline="") as out:
        writer = csv.writer(out)
        writer.writerow(["pair_id", "member"] + attributes + ["class"])
        for i, raw in enumerate(f):
            line = raw.strip()
            if not line:
                continue
            values = [v.strip() for v in line.split(",")]
            if len(values) != len(attributes) + 1:
                sys.exit(f"line {i+1}: expected {len(attributes)+1} fields, "
                         f"got {len(values)}")
            writer.writerow([i // 2 + 1, "p1" if i % 2 == 0 else "p2"]
                            + values)
            n_rows += 1
    print(f"{out_path}: {n_rows} rows ({n_rows // 2} pairs), "
          f"{len(attributes)} attributes, classes: {', '.join(classes)}")


if __name__ == "__main__":
    main()
