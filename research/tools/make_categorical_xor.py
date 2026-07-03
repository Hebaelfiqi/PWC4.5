"""Generate the categorical analogue of the synthetic XOR PWCCP data, directly
as pair-as-instance ARFF (train + test).

Design: k nominal attributes C1..Ck with alphabet {a, b, c, ...}. Values are
drawn uniformly at random for each pair member, so absolute values carry no
class signal. The pair label is the XOR (parity) of the within-pair equality
predicates over the first two attributes:

    match_i = [ Ci(p1) == Ci(p2) ]
    label   = '+' if (match_1 XOR match_2) else '-'

A relational classifier with the nominal relation {eq, neq} can represent this
exactly; a classifier over raw attribute values (which can only compare an
attribute to a constant, never to another attribute) has no signal and should
sit at chance.

Usage:
    python make_categorical_xor.py -o outdir [--pairs-train 200] [--pairs-test 100]
                                   [--attrs 2] [--alphabet 3] [--noise 0.0] [--seed 0]
"""
import argparse
from pathlib import Path
import random

def gen(n_pairs, k, alphabet, noise, rng):
    """Constructively class-balanced: half '+' (exactly one of the first two
    attributes matches within the pair), half '-' (both match or neither
    matches, evenly split so the task remains true parity, not mere
    match-detection). Without this, large alphabets make matches rare, the
    class prior collapses toward '-', and a majority-class baseline looks
    deceptively strong."""
    def diff_of(v):
        w = rng.choice(alphabet)
        while w == v:
            w = rng.choice(alphabet)
        return w

    rows = []
    for i in range(n_pairs):
        p1 = [rng.choice(alphabet) for _ in range(k)]
        p2 = [rng.choice(alphabet) for _ in range(k)]  # attrs 3..k stay random
        if i % 2 == 0:                       # '+': exactly one match
            which = rng.random() < 0.5
            p2[0] = p1[0] if which else diff_of(p1[0])
            p2[1] = diff_of(p1[1]) if which else p1[1]
            label = "+"
        else:                                # '-': both match or neither
            if rng.random() < 0.5:
                p2[0], p2[1] = p1[0], p1[1]
            else:
                p2[0], p2[1] = diff_of(p1[0]), diff_of(p1[1])
            label = "-"
        if noise > 0 and rng.random() < noise:
            label = "+" if label == "-" else "-"
        rows.append((p1, p2, label))
    rng.shuffle(rows)
    return rows

def write_arff(path, rows, k, alphabet, name):
    vals = ",".join(alphabet)
    with open(path, "w") as f:
        f.write(f"@relation {name}\n\n")
        for side in ("p1", "p2"):
            for i in range(k):
                f.write(f"@attribute {side}_C{i+1} {{{vals}}}\n")
        f.write("@attribute pair_class {+,-}\n\n@data\n")
        for p1, p2, label in rows:
            f.write(",".join(p1 + p2) + f",{label}\n")

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--pairs-train", type=int, default=200)
    ap.add_argument("--pairs-test", type=int, default=100)
    ap.add_argument("--attrs", type=int, default=2)
    ap.add_argument("--alphabet", type=int, default=3)
    ap.add_argument("--noise", type=float, default=0.0)
    ap.add_argument("--seed", type=int, default=0)
    args = ap.parse_args()

    assert args.attrs >= 2, "need at least 2 attributes for the XOR rule"
    alphabet = [chr(ord("a") + i) for i in range(args.alphabet)]
    rng = random.Random(args.seed)
    out = Path(args.outdir); out.mkdir(parents=True, exist_ok=True)

    tr = gen(args.pairs_train, args.attrs, alphabet, args.noise, rng)
    te = gen(args.pairs_test, args.attrs, alphabet, 0.0, rng)  # test noise-free
    write_arff(out / "cat_xor_train.arff", tr, args.attrs, alphabet, "cat_xor_train")
    write_arff(out / "cat_xor_test.arff", te, args.attrs, alphabet, "cat_xor_test")
    pos = sum(1 for r in tr if r[2] == "+")
    print(f"{out}: train {len(tr)} pairs ({pos} '+'), test {len(te)} pairs, "
          f"{args.attrs} nominal attrs, alphabet {len(alphabet)}, noise {args.noise}")

if __name__ == "__main__":
    main()
