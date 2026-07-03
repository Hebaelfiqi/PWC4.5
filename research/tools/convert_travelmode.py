"""TravelMode discrete-choice data (AER / mlogit) -> comparative-choice pairs.

Each individual faces 4 travel modes (air/train/bus/car) with mode-specific
attributes and chooses one. We pair the CHOSEN mode with each non-chosen mode;
the pair is randomly oriented and labelled {Chosen, NotChosen} by p1's role.
Unlike SUSHI (where preference tracks absolute item quality), travel-mode
choice is genuinely *comparative*: a mode is chosen because its cost/time is
low RELATIVE to the alternative in the same trip context. Features: wait,
vcost, travel, gcost (mode attributes); income/size are per-individual and
cancel within a pair, so they are excluded.

Split: by individual (disjoint), default 30% test.

Usage: python convert_travelmode.py travelmode.csv -o outdir [--seed 0]
"""
import argparse, csv, random
from pathlib import Path
from collections import defaultdict

FEATS = ["wait", "vcost", "travel", "gcost"]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("csv_path"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--test-frac", type=float, default=0.3)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    by_ind = defaultdict(list)
    with open(args.csv_path) as f:
        for r in csv.DictReader(f):
            by_ind[r["individual"]].append(r)

    inds = sorted(by_ind, key=int); rng.shuffle(inds)
    n_test = int(len(inds) * args.test_frac)
    test_set = set(inds[:n_test])

    def feat(r):
        return [r[k] for k in FEATS]

    out = {"train": [], "test": []}
    for ind, modes in by_ind.items():
        split = "test" if ind in test_set else "train"
        chosen = [m for m in modes if m["choice"] == "yes"]
        if not chosen:
            continue
        ch = chosen[0]
        for other in modes:
            if other is ch:
                continue
            if rng.random() < 0.5:
                out[split].append((feat(ch), feat(other), "Chosen"))
            else:
                out[split].append((feat(other), feat(ch), "NotChosen"))
    for split in out:
        rng.shuffle(out[split])

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    for split in ("train", "test"):
        with open(outdir / f"travelmode_{split}.arff", "w") as f:
            f.write(f"@relation travelmode_choice_{split}\n\n")
            for side in ("p1", "p2"):
                for k in FEATS:
                    f.write(f"@attribute {side}_{k} numeric\n")
            f.write("@attribute pair_class {Chosen,NotChosen}\n\n@data\n")
            for p1, p2, label in out[split]:
                f.write(",".join(p1 + p2) + f",{label}\n")
    # combined for CV
    with open(outdir / "travelmode_all.arff", "w") as f:
        f.write((outdir / "travelmode_train.arff").read_text())
        for line in (outdir / "travelmode_test.arff").read_text().splitlines():
            s = line.strip()
            if s and not s.startswith("@") and not s.startswith("%"):
                f.write(line + "\n")
    print(f"travelmode: {len(by_ind)} individuals -> train {len(out['train'])} "
          f"pairs, test {len(out['test'])} pairs (individual-disjoint)")

if __name__ == "__main__":
    main()
