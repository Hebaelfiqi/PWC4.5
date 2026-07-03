"""Convert a PWCCP dataset (.names/.data or .test) into a WEKA ARFF using the
pair-as-single-instance representation:

    2n attributes  = p1's n features followed by p2's n features
    class          = the class of p1 (the first block); p2 is the other class

Pairs are randomly oriented (seeded) so a classifier cannot exploit position.
This is the representation the WEKA PWC45 classifier expects, and it makes the
pairing invisible to WEKA's cross-validation (a fold can never split a pair).

Usage:
    python make_pair_arff.py <stem-without-extension> <.data|.test> [-o out.arff] [--seed N]
"""
import argparse
import sys
from pathlib import Path
import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "baselines"))
import pwccp_data


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("stem", help="dataset stem, e.g. .../M3_M4_Asad_Daryabadi_Exp_1")
    ap.add_argument("split", choices=[".data", ".test"], nargs="?", default=".data")
    ap.add_argument("-o", "--output")
    ap.add_argument("--seed", type=int, default=0)
    args = ap.parse_args()

    stem = Path(args.stem)
    classes, attrs = pwccp_data.read_names(stem)
    d = pwccp_data.load(str(stem), seed=args.seed)
    split = d["train"] if args.split == ".data" else d["test"]
    A, B, y = split["A"], split["B"], split["y"]

    out = Path(args.output) if args.output else Path(str(stem) + args.split + ".pair.arff")
    with out.open("w") as f:
        f.write(f"@relation pwccp_{stem.name}\n\n")
        for side in ("p1", "p2"):
            for a in attrs:
                f.write(f"@attribute {side}_{a} numeric\n")
        f.write(f"@attribute pair_class {{{classes[0]},{classes[1]}}}\n\n")
        f.write("@data\n")
        for i in range(len(y)):
            row = list(A[i]) + list(B[i])
            label = classes[0] if y[i] == 1 else classes[1]
            f.write(",".join(f"{v:g}" for v in row) + f",{label}\n")
    print(f"{out}: {len(y)} pair-instances, {2*len(attrs)} attributes, "
          f"classes {classes}")


if __name__ == "__main__":
    main()
