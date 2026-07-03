"""Condorcet / majority-rule matchup data — a principled non-transitive PWCCP
where PWC4.5 should beat all pointwise/difference baselines.

Each item has k numeric criteria (uniform random, so absolute values carry no
class signal). In a pair, p1 wins iff it has the larger value on a MAJORITY of
criteria (>= ceil(k/2)). Majority preference over >=3 criteria is the classic
Condorcet paradox: it is genuinely NON-TRANSITIVE (A beats B beats C beats A is
possible). This matters because:
  - PWC4.5 can represent it: split on R(criterion_i) = min/max for each i, the
    tree counts how many favour p1 (majority) -> exact.
  - Pointwise rankers (RankNet, LambdaMART) score each item independently as
    s(a) vs s(b); a single score is transitive by construction and cannot
    reproduce Condorcet cycles.
  - Difference/linear methods approximate majority only crudely (it is a
    threshold on the SUM OF SIGNS of the differences, non-linear in the diff).

Majority preference over criteria is a real decision model (multi-criteria
decision making, voting theory), so this is not a toy rule but the canonical
non-transitive structure.

Usage: python make_condorcet.py -o outdir [--criteria 3] [--pairs-train 400]
       [--pairs-test 200] [--noise 0.0] [--seed 0]
"""
import argparse, random, math
from pathlib import Path

def gen(n, k, noise, rng):
    rows = []
    for _ in range(n):
        p1 = [rng.random() for _ in range(k)]
        p2 = [rng.random() for _ in range(k)]
        wins = sum(1 for i in range(k) if p1[i] > p2[i])
        label = "P1wins" if wins >= math.ceil(k / 2 + 1e-9) else "P2wins"
        if wins * 2 == k:  # even k tie on count -> break by first criterion
            label = "P1wins" if p1[0] > p2[0] else "P2wins"
        if noise > 0 and rng.random() < noise:
            label = "P2wins" if label == "P1wins" else "P1wins"
        rows.append((p1, p2, label))
    return rows

def write(path, rows, k, name):
    with open(path, "w") as f:
        f.write(f"@relation {name}\n\n")
        for side in ("p1", "p2"):
            for i in range(k):
                f.write(f"@attribute {side}_crit{i+1} numeric\n")
        f.write("@attribute pair_class {P1wins,P2wins}\n\n@data\n")
        for p1, p2, label in rows:
            f.write(",".join(f"{v:.5f}" for v in p1 + p2) + f",{label}\n")

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--criteria", type=int, default=3)
    ap.add_argument("--pairs-train", type=int, default=400)
    ap.add_argument("--pairs-test", type=int, default=200)
    ap.add_argument("--noise", type=float, default=0.0)
    ap.add_argument("--seed", type=int, default=0)
    args = ap.parse_args()
    rng = random.Random(args.seed)
    out = Path(args.outdir); out.mkdir(parents=True, exist_ok=True)
    tr = gen(args.pairs_train, args.criteria, args.noise, rng)
    te = gen(args.pairs_test, args.criteria, 0.0, rng)
    write(out / "condorcet_train.arff", tr, args.criteria, "condorcet_train")
    write(out / "condorcet_test.arff", te, args.criteria, "condorcet_test")
    with open(out / "condorcet_all.arff", "w") as f:
        f.write((out / "condorcet_train.arff").read_text())
        for line in (out / "condorcet_test.arff").read_text().splitlines():
            s = line.strip()
            if s and not s.startswith("@"):
                f.write(line + "\n")
    print(f"condorcet: {args.criteria} criteria, train {len(tr)} / test {len(te)} pairs, noise {args.noise}")

if __name__ == "__main__":
    main()
