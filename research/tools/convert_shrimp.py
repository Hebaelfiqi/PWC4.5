"""Mantis shrimp telson-sparring contests (Green & Patek, Zenodo 4995116)
-> dyadic contest pairs (pair-ARFF).

Real ecological dyadic contests: consecutive rows are the winner (W) and loser
(L) of one staged contest over a burrow. Pair = (contestant A, contestant B),
label = which won. Pre-contest traits only: residency (resident/intruder),
body length, body mass, maximum strike force. Residency is encoded NUMERICALLY
(resident=1, intruder=0) so PWC4.5's min/max relation preserves direction
(which animal is the resident) — a nominal eq/neq relation would be useless
here because every contest pairs one resident with one intruder (always 'neq').
'Number Strikes Delivered' is excluded (a contest outcome -> leakage).

Pairs are randomly oriented (the raw data lists winner first).

Usage: python convert_shrimp.py Contests.csv -o outdir [--seed 0]
"""
import argparse, csv, random
from pathlib import Path

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("csv_path"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    rows = [r for r in csv.reader(open(args.csv_path)) if r and r[0].startswith("G")]
    # columns: ID, Sex, Residency, Win/Lose, BodyLength, BodyMass, StrikeForce, NStrikes
    def feat(r):
        residency = 1 if r[2].strip().upper().startswith("R") else 0
        return [str(residency), r[4], r[5], r[6]]   # residency, length, mass, force

    contests = []
    for i in range(0, len(rows) - 1, 2):
        a, b = rows[i], rows[i + 1]
        # ensure a=winner, b=loser
        if a[3].strip().upper().startswith("L"):
            a, b = b, a
        contests.append((feat(a), feat(b)))          # a won, b lost

    oriented = []
    for wf, lf in contests:
        if rng.random() < 0.5:
            oriented.append((wf, lf, "P1wins"))
        else:
            oriented.append((lf, wf, "P2wins"))
    rng.shuffle(oriented)

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    names = ["residency", "body_length", "body_mass", "strike_force"]
    with open(outdir / "shrimp_all.arff", "w") as f:
        f.write("@relation mantis_shrimp_contests\n\n")
        for side in ("p1", "p2"):
            for n in names:
                f.write(f"@attribute {side}_{n} numeric\n")
        f.write("@attribute pair_class {P1wins,P2wins}\n\n@data\n")
        for p1, p2, label in oriented:
            f.write(",".join(p1 + p2) + f",{label}\n")
    print(f"shrimp: {len(contests)} contests -> {len(oriented)} pairs "
          f"(residency numeric, strike force retained, N-strikes excluded)")

if __name__ == "__main__":
    main()
