"""UCI Parkinson's Telemonitoring -> disease-progression pairs (pair-ARFF).

Each of 42 subjects has ~150 voice recordings over ~6 months (test_time in
days). A pair = two visits of the SAME subject at least `--gap` days apart;
the pair is randomly oriented and labelled {Earlier, Later} by p1's position.
Features: the 16 voice measures only (test_time and UPDRS scores are excluded
as leakage; age/sex are constant within a subject and excluded). The task:
detect the direction of progression from voice biomarkers alone - the
within-pair relational structure is the signal, since absolute voice measures
vary across subjects far more than within.

Split: subject-disjoint (default 12 of 42 subjects in test).

Usage: python convert_parkinsons.py <parkinsons_updrs.data> -o outdir
       [--gap 90] [--pairs-per-subject 30] [--test-subjects 12] [--seed 0]
"""
import argparse, csv, random
from pathlib import Path
from collections import defaultdict

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("data_path"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--gap", type=float, default=90.0)
    ap.add_argument("--pairs-per-subject", type=int, default=30)
    ap.add_argument("--test-subjects", type=int, default=12)
    ap.add_argument("--seed", type=int, default=0)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    with open(args.data_path) as f:
        reader = csv.reader(f)
        header = next(reader)
        rows = [r for r in reader if r]
    i_subj = header.index("subject#"); i_time = header.index("test_time")
    feat_idx = [i for i, h in enumerate(header)
                if h not in ("subject#", "age", "sex", "test_time",
                             "motor_UPDRS", "total_UPDRS")]
    feat_names = [header[i].replace("(", "_").replace(")", "").replace(":", "_")
                  .replace("%", "pct") for i in feat_idx]

    by_subj = defaultdict(list)
    for r in rows:
        by_subj[r[i_subj]].append((float(r[i_time]), [r[i] for i in feat_idx]))
    for s in by_subj:
        by_subj[s].sort()

    subjects = sorted(by_subj, key=int); rng.shuffle(subjects)
    test_set = set(subjects[:args.test_subjects])

    out = {"train": [], "test": []}
    for s, visits in by_subj.items():
        split = "test" if s in test_set else "train"
        cand = [(i, j) for i in range(len(visits)) for j in range(i + 1, len(visits))
                if visits[j][0] - visits[i][0] >= args.gap]
        rng.shuffle(cand)
        for i, j in cand[:args.pairs_per_subject]:
            early, late = visits[i][1], visits[j][1]
            if rng.random() < 0.5:
                out[split].append((early, late, "Earlier"))
            else:
                out[split].append((late, early, "Later"))
    for split in out:
        rng.shuffle(out[split])

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    for split in ("train", "test"):
        with open(outdir / f"parkinsons_{split}.arff", "w") as f:
            f.write(f"@relation parkinsons_progression_{split}\n\n")
            for side in ("p1", "p2"):
                for n in feat_names:
                    f.write(f"@attribute {side}_{n} numeric\n")
            f.write("@attribute pair_class {Earlier,Later}\n\n@data\n")
            for p1, p2, label in out[split]:
                f.write(",".join(p1 + p2) + f",{label}\n")
    print(f"parkinsons: {len(by_subj)} subjects, gap>={args.gap}d -> "
          f"train {len(out['train'])} pairs, test {len(out['test'])} pairs "
          f"(subject-disjoint, {len(test_set)} test subjects)")

if __name__ == "__main__":
    main()
