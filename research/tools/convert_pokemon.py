"""Pokemon combat data (Weedle's Cave, Kaggle) -> pairwise battle pairs.

Each of ~50k rows is a real battle: (First_pokemon, Second_pokemon, Winner).
We form the pair (p1 = First, p2 = Second) with features = each Pokemon's six
base stats (HP, Attack, Defense, Sp.Atk, Sp.Def, Speed) and its two types
(nominal). Label = which of the ordered pair won. Battle outcome depends on
RELATIONS between the two Pokemon (is my Attack above your Defense, am I
faster) plus non-transitive type effectiveness — the structure PWC4.5 targets.

Split: by battle row (random), default 30% test. (Pokemon recur across battles;
this mirrors the standard Weedle's Cave train/test setup.)

Usage: python convert_pokemon.py pokemon.csv combats.csv -o outdir [--seed 0]
"""
import argparse, csv, random
from pathlib import Path

STATS = ["HP", "Attack", "Defense", "Sp. Atk", "Sp. Def", "Speed"]
TYPES = ["Bug","Dark","Dragon","Electric","Fairy","Fighting","Fire","Flying",
         "Ghost","Grass","Ground","Ice","Normal","Poison","Psychic","Rock",
         "Steel","Water","None"]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("pokemon_csv"); ap.add_argument("combats_csv")
    ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--test-frac", type=float, default=0.3)
    ap.add_argument("--max-battles", type=int, default=20000)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    pk = {}
    for r in csv.DictReader(open(args.pokemon_csv)):
        pk[r["#"]] = ([r[s] for s in STATS],
                      [r["Type 1"] or "None", r["Type 2"] or "None"])

    battles = list(csv.DictReader(open(args.combats_csv)))
    rng.shuffle(battles); battles = battles[:args.max_battles]
    n_test = int(len(battles) * args.test_frac)

    def feat(pid):
        stats, types = pk[pid]
        return stats, types

    out = {"train": [], "test": []}
    for i, b in enumerate(battles):
        f, s, w = b["First_pokemon"], b["Second_pokemon"], b["Winner"]
        if f not in pk or s not in pk:
            continue
        s1, t1 = feat(f); s2, t2 = feat(s)
        label = "P1wins" if w == f else "P2wins"
        split = "test" if i < n_test else "train"
        out[split].append((s1 + t1, s2 + t2, label))

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    tvals = ",".join(TYPES)
    def write(path, rows, name):
        with open(path, "w") as f:
            f.write(f"@relation {name}\n\n")
            for side in ("p1", "p2"):
                for s in STATS:
                    f.write(f"@attribute {side}_{s.replace(' ','').replace('.','')} numeric\n")
                f.write(f"@attribute {side}_type1 {{{tvals}}}\n")
                f.write(f"@attribute {side}_type2 {{{tvals}}}\n")
            f.write("@attribute pair_class {P1wins,P2wins}\n\n@data\n")
            for p1, p2, label in rows:
                f.write(",".join(p1 + p2) + f",{label}\n")
    write(outdir / "pokemon_train.arff", out["train"], "pokemon_battles_train")
    write(outdir / "pokemon_test.arff", out["test"], "pokemon_battles_test")
    with open(outdir / "pokemon_all.arff", "w") as f:
        f.write((outdir / "pokemon_train.arff").read_text())
        for line in (outdir / "pokemon_test.arff").read_text().splitlines():
            t = line.strip()
            if t and not t.startswith("@"):
                f.write(line + "\n")
    print(f"pokemon: {len(battles)} battles -> train {len(out['train'])}, "
          f"test {len(out['test'])} pairs (6 stats + 2 types per side)")

if __name__ == "__main__":
    main()
