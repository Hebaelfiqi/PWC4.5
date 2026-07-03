"""UCI Dota2 Games Results -> team-vs-team matchup pairs (pair-ARFF).

Each row is a real 5v5 match: col0 = winner (+1 team1/Radiant, -1 team2/Dire),
cols 1-3 = game metadata (dropped), cols 4.. = 113 hero columns coded +1 if the
hero is on team1, -1 if on team2, 0 if unpicked.

Pair-as-instance: p1 = team1's hero-presence vector (1/0), p2 = team2's. Hero
presence is encoded NUMERICALLY (1/0) so PWC4.5's min/max relation preserves
direction: R(hero_i)=max means "p1's team has hero i and p2's doesn't", min
means the reverse, eq means neither. This captures draft advantage and (via
combinations) counter-picks, which are non-transitive. Pairs are randomly
oriented so radiant-side bias is not a shortcut.

Usage: python convert_dota2.py dota2Train.csv -o outdir [--seed 0] [--max 20000]
"""
import argparse, csv, random
from pathlib import Path

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("csv_path"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--max", type=int, default=20000)
    ap.add_argument("--test-frac", type=float, default=0.3)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    rows = []
    with open(args.csv_path) as f:
        for r in csv.reader(f):
            if not r:
                continue
            win = int(r[0])
            heroes = [int(x) for x in r[4:]]          # 113 hero columns
            t1 = [1 if h == 1 else 0 for h in heroes]
            t2 = [1 if h == -1 else 0 for h in heroes]
            rows.append((t1, t2, win))
    rng.shuffle(rows); rows = rows[:args.max]
    n_hero = len(rows[0][0])

    out = {"train": [], "test": []}
    n_test = int(len(rows) * args.test_frac)
    for i, (t1, t2, win) in enumerate(rows):
        # random orientation
        if rng.random() < 0.5:
            p1, p2, t1won = t1, t2, (win == 1)
        else:
            p1, p2, t1won = t2, t1, (win == -1)
        label = "P1wins" if t1won else "P2wins"
        out["test" if i < n_test else "train"].append((p1, p2, label))

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    def write(path, data, name):
        with open(path, "w") as f:
            f.write(f"@relation {name}\n\n")
            for side in ("p1", "p2"):
                for i in range(n_hero):
                    f.write(f"@attribute {side}_hero{i+1} numeric\n")
            f.write("@attribute pair_class {P1wins,P2wins}\n\n@data\n")
            for p1, p2, label in data:
                f.write(",".join(str(v) for v in p1 + p2) + f",{label}\n")
    write(outdir / "dota2_train.arff", out["train"], "dota2_train")
    write(outdir / "dota2_test.arff", out["test"], "dota2_test")
    with open(outdir / "dota2_all.arff", "w") as f:
        f.write((outdir / "dota2_train.arff").read_text())
        for line in (outdir / "dota2_test.arff").read_text().splitlines():
            t = line.strip()
            if t and not t.startswith("@"):
                f.write(line + "\n")
    print(f"dota2: {len(rows)} matches, {n_hero} heroes -> train {len(out['train'])}, "
          f"test {len(out['test'])} pairs")

if __name__ == "__main__":
    main()
