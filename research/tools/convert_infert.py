"""Matched case-control (secondary infertility; Trichopoulos et al., via R's
`infert`) -> pair-as-instance ARFF.

Each matched stratum holds 1 case and 2 controls (matched on age, parity,
education). We form the two (case, control) pairs per stratum; the pair is
randomly oriented and labelled by p1's status {Case, Control}. Features:
education (nominal), age, parity, induced, spontaneous. Matched covariates
mostly yield 'eq' relations within a pair by design - exactly the structure
the method exploits (unmatched covariates carry the signal).

Split: by stratum (train/test disjoint strata) so no case appears in both.

Usage: python convert_infert.py <infert.csv> -o outdir [--seed 0] [--test-frac 0.3]
"""
import argparse, csv, random
from pathlib import Path
from collections import defaultdict

ED_VALS = ["0-5yrs", "6-11yrs", "12+ yrs"]

def write_arff(path, rows, name):
    ed = ",".join(f"'{v}'" for v in ED_VALS)
    with open(path, "w") as f:
        f.write(f"@relation {name}\n\n")
        for side in ("p1", "p2"):
            f.write(f"@attribute {side}_education {{{ed}}}\n")
            for a in ("age", "parity", "induced", "spontaneous"):
                f.write(f"@attribute {side}_{a} numeric\n")
        f.write("@attribute pair_class {Case,Control}\n\n@data\n")
        for p1, p2, label in rows:
            f.write(",".join(p1 + p2) + f",{label}\n")

def feat(r):
    return [f"'{r['education']}'", r["age"], r["parity"], r["induced"], r["spontaneous"]]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("csv_path"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--test-frac", type=float, default=0.3)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    strata = defaultdict(lambda: {"case": [], "ctrl": []})
    with open(args.csv_path) as f:
        for r in csv.DictReader(f):
            strata[r["stratum"]]["case" if r["case"] == "1" else "ctrl"].append(r)

    sids = sorted(strata, key=int); rng.shuffle(sids)
    n_test = int(len(sids) * args.test_frac)
    test_ids = set(sids[:n_test])

    out = {"train": [], "test": []}
    for sid, grp in strata.items():
        split = "test" if sid in test_ids else "train"
        for case in grp["case"]:
            for ctrl in grp["ctrl"]:
                a, b, la = (case, ctrl, "Case") if rng.random() < 0.5 else (ctrl, case, "Control")
                out[split].append((feat(a), feat(b), la))
    for split in out:
        rng.shuffle(out[split])

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    write_arff(outdir / "infert_train.arff", out["train"], "infert_pairs_train")
    write_arff(outdir / "infert_test.arff", out["test"], "infert_pairs_test")
    print(f"infert: {len(strata)} strata -> train {len(out['train'])} pairs, "
          f"test {len(out['test'])} pairs (stratum-disjoint)")

if __name__ == "__main__":
    main()
