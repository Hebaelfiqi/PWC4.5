"""MASS::birthwt (Hosmer-Lemeshow low birth weight study) -> CONSTRUCTED
matched case-control pairs (pair-ARFF).

birthwt is not pre-matched. We construct matched pairs the way the original
study did: each case (low birth weight, low=1) is matched to a control
(low=0) with the same race and age within +/-`age_tol` years. This makes the
matched covariates (age, race) cancel within a pair (mostly 'eq' relations),
so the discriminative signal is carried by the unmatched risk factors -
exactly the structure PWC4.5 exploits. Task: which pair member is the case.

Features: age, mother's weight (lwt), race, smoking, premature labours (ptl),
hypertension (ht), uterine irritability (ui), physician visits (ftv). The
actual birth weight (bwt) is EXCLUDED (it defines the label - leakage).

Usage: python convert_birthwt.py birthwt.csv -o outdir [--age-tol 2] [--seed 0]
"""
import argparse, csv, random
from pathlib import Path

NUM = ["age", "lwt", "ptl", "ftv"]
NOM = {"race": ["1", "2", "3"], "smoke": ["0", "1"], "ht": ["0", "1"], "ui": ["0", "1"]}
ORDER = ["age", "lwt", "race", "smoke", "ptl", "ht", "ui", "ftv"]

def feat(r):
    return [r[k] for k in ORDER]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("csv_path"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--age-tol", type=int, default=2)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--test-frac", type=float, default=0.3)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    rows = list(csv.DictReader(open(args.csv_path)))
    cases = [r for r in rows if r["low"] == "1"]
    controls = [r for r in rows if r["low"] == "0"]
    rng.shuffle(cases); rng.shuffle(controls)
    used = set()
    pairs = []
    for c in cases:
        cand = [k for k, ctl in enumerate(controls) if k not in used
                and ctl["race"] == c["race"]
                and abs(int(ctl["age"]) - int(c["age"])) <= args.age_tol]
        if not cand:
            continue
        k = rng.choice(cand); used.add(k); ctl = controls[k]
        if rng.random() < 0.5:
            pairs.append((feat(c), feat(ctl), "Case"))
        else:
            pairs.append((feat(ctl), feat(c), "Control"))
    rng.shuffle(pairs)
    n_test = int(len(pairs) * args.test_frac)
    splits = {"test": pairs[:n_test], "train": pairs[n_test:]}

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    def write(path, data, name):
        with open(path, "w") as f:
            f.write(f"@relation {name}\n\n")
            for side in ("p1", "p2"):
                for k in ORDER:
                    if k in NOM:
                        f.write(f"@attribute {side}_{k} {{{','.join(NOM[k])}}}\n")
                    else:
                        f.write(f"@attribute {side}_{k} numeric\n")
            f.write("@attribute pair_class {Case,Control}\n\n@data\n")
            for p1, p2, label in data:
                f.write(",".join(p1 + p2) + f",{label}\n")
    write(outdir / "birthwt_train.arff", splits["train"], "birthwt_matched_train")
    write(outdir / "birthwt_test.arff", splits["test"], "birthwt_matched_test")
    with open(outdir / "birthwt_all.arff", "w") as f:
        f.write((outdir / "birthwt_train.arff").read_text())
        for line in (outdir / "birthwt_test.arff").read_text().splitlines():
            s = line.strip()
            if s and not s.startswith("@") and not s.startswith("%"):
                f.write(line + "\n")
    print(f"birthwt: {len(cases)} cases, {len(controls)} controls -> "
          f"{len(pairs)} matched pairs (age+/-{args.age_tol}, same race); "
          f"train {len(splits['train'])}, test {len(splits['test'])}")

if __name__ == "__main__":
    main()
