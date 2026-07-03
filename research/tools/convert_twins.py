"""Twins dataset (Louizos et al. CEVAE; US twin births 1989-91) -> discordant
mortality pairs (pair-ARFF). A real, non-competition, paired dataset.

Each row is a twin PAIR sharing all pregnancy/parent covariates (which cancel
as within-pair 'eq' relations) and differing in birth weight. We keep the
DISCORDANT pairs (exactly one twin died in year 1) and ask: which twin died?
The signal is the within-pair birth-weight relation (the lighter twin has
higher mortality) with shared confounds cancelled by the pairing — the PWCCP
structure in a medical/demographic domain rather than a competition.

Per-twin features: birth weight (differs) + a few shared covariates
(gestation, mother age, race — identical within a pair, included for realism;
they yield 'eq' relations and carry no signal). Pairs randomly oriented.

Usage: python convert_twins.py X.csv T.csv Y.csv -o outdir [--seed 0]
"""
import argparse, csv, random
from pathlib import Path

SHARED = ["gestat10", "mager8", "mrace", "meduc6", "cardiac", "lung", "diabetes"]

def load(path):
    rows = list(csv.DictReader(open(path)))
    return rows

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("x_csv"); ap.add_argument("t_csv"); ap.add_argument("y_csv")
    ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    X = load(args.x_csv); T = load(args.t_csv); Y = load(args.y_csv)
    n = min(len(X), len(T), len(Y))

    def sharedfeat(xr):
        out = []
        for c in SHARED:
            v = xr.get(c, "")
            out.append(v if v not in ("", "nan") else "0")
        return out

    pairs = []
    for i in range(n):
        try:
            w0 = float(T[i]["dbirwt_0"]); w1 = float(T[i]["dbirwt_1"])
            m0 = float(Y[i]["mort_0"]); m1 = float(Y[i]["mort_1"])
        except (ValueError, KeyError):
            continue
        if m0 == m1:
            continue                    # keep discordant-mortality pairs only
        sf = sharedfeat(X[i])
        f0 = [f"{w0:g}"] + sf           # twin 0 features
        f1 = [f"{w1:g}"] + sf           # twin 1 features
        died0 = (m0 == 1.0)
        # winner-of-interest = the twin that died
        a, b, label_a_died = (f0, f1, died0)
        if rng.random() < 0.5:
            pairs.append((a, b, "P1died" if label_a_died else "P2died"))
        else:
            pairs.append((b, a, "P2died" if label_a_died else "P1died"))
    rng.shuffle(pairs)

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    names = ["birthweight"] + SHARED
    with open(outdir / "twins_all.arff", "w") as f:
        f.write("@relation twins_discordant_mortality\n\n")
        for side in ("p1", "p2"):
            for nm in names:
                f.write(f"@attribute {side}_{nm} numeric\n")
        f.write("@attribute pair_class {P1died,P2died}\n\n@data\n")
        for p1, p2, label in pairs:
            f.write(",".join(p1 + p2) + f",{label}\n")
    n_p1 = sum(1 for _, _, l in pairs if l == "P1died")
    print(f"twins: {len(pairs)} discordant-mortality pairs "
          f"({n_p1} P1died / {len(pairs)-n_p1} P2died); feature that differs "
          f"within a pair = birth weight")

if __name__ == "__main__":
    main()
