"""SUSHI-3 preference data (Kamishima) -> preference pairs (pair-ARFF).

Each of 5000 users ranked 10 sushi items (set A). We take the top-vs-bottom,
2nd-vs-9th, and 3rd-vs-8th pairs per user; the pair is randomly oriented and
labelled {Preferred, NotPreferred} by p1's role. Item features from
sushi3.idata: style (maki/other), major group (seafood/other), minor group
(12 categories) as nominals; oiliness, eating frequency, price, sell
frequency as numerics. Task: from two items' features, which did the user
prefer (population-level preference; per-user variation is label noise).

Split: by user (disjoint), default 30% test.

Usage: python convert_sushi.py <sushi3-2016 dir> -o outdir [--seed 0]
       [--pairs-per-user 3] [--test-frac 0.3] [--max-users 2000]
"""
import argparse, random
from pathlib import Path

MINOR = [str(i) for i in range(12)]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("sushi_dir"); ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--pairs-per-user", type=int, default=3)
    ap.add_argument("--test-frac", type=float, default=0.3)
    ap.add_argument("--max-users", type=int, default=2000)
    args = ap.parse_args()
    rng = random.Random(args.seed)
    d = Path(args.sushi_dir)

    items = {}
    for line in (d / "sushi3.idata").read_text().splitlines():
        p = line.split("\t")
        if len(p) < 9: continue
        # id, name, style, major, minor, oiliness, freq, price, sell_freq
        items[p[0]] = [p[2], p[3], p[4], p[5], p[6], p[7], p[8]]

    orders = []
    for line in (d / "sushi3a.5000.10.order").read_text().splitlines()[1:]:
        p = line.split()
        if len(p) == 12:      # "0 10" prefix then 10 item ids, most->least preferred
            orders.append(p[2:])
    rng.shuffle(orders)
    orders = orders[:args.max_users]
    n_test = int(len(orders) * args.test_frac)

    def feat(iid):
        s = items[iid]
        return [s[0], s[1], s[2], s[3], s[4], s[5], s[6]]

    out = {"train": [], "test": []}
    for u, order in enumerate(orders):
        split = "test" if u < n_test else "train"
        for k in range(args.pairs_per_user):
            pref, npref = order[k], order[-(k + 1)]
            if rng.random() < 0.5:
                out[split].append((feat(pref), feat(npref), "Preferred"))
            else:
                out[split].append((feat(npref), feat(pref), "NotPreferred"))
    for split in out:
        rng.shuffle(out[split])

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    minor_vals = ",".join(MINOR)
    for split in ("train", "test"):
        with open(outdir / f"sushi_{split}.arff", "w") as f:
            f.write(f"@relation sushi_pref_{split}\n\n")
            for side in ("p1", "p2"):
                f.write(f"@attribute {side}_style {{0,1}}\n")
                f.write(f"@attribute {side}_major {{0,1}}\n")
                f.write(f"@attribute {side}_minor {{{minor_vals}}}\n")
                for a in ("oiliness", "eat_freq", "price", "sell_freq"):
                    f.write(f"@attribute {side}_{a} numeric\n")
            f.write("@attribute pair_class {Preferred,NotPreferred}\n\n@data\n")
            for p1, p2, label in out[split]:
                f.write(",".join(p1 + p2) + f",{label}\n")
    print(f"sushi: {len(orders)} users -> train {len(out['train'])} pairs, "
          f"test {len(out['test'])} pairs (user-disjoint)")

if __name__ == "__main__":
    main()
