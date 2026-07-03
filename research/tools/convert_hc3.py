"""HC3 (Human ChatGPT Comparison Corpus, Guo et al. 2023) -> LLM-vs-human
attribution pairs (pair-ARFF).

Each question has a human answer and a ChatGPT answer. Pairing the two answers
to the SAME question is *ordered attribution* (like translator stylometry): the
shared prompt cancels topic/content, so the discriminative signal is the
within-pair STYLE relation, not absolute values. Task: which answer is the LLM.
This is the modern, LLM-era analogue of the translator problem — and, unlike
open-ended verification, it is relation-dominated because the pair is parallel.

Features are register-neutral stylometric measures (no content words), so the
model cannot cheat on topic: length, avg word/sentence length, punctuation
rates, uppercase rate, digit rate, type-token ratio, function-word rate,
and a few structural markers. Pairs randomly oriented.

Usage: python convert_hc3.py hc3/*.jsonl -o outdir [--seed 0] [--max 4000]
"""
import argparse, json, random, re, math
from pathlib import Path

FUNCTION = set(("the a an of to in and or but if then for with on at by from as "
    "is are was were be been being this that these those it its i you he she we "
    "they not no do does did have has had will would can could should").split())

def feats(text):
    t = text.strip()
    words = re.findall(r"[A-Za-z']+", t)
    n = max(len(words), 1)
    sents = [s for s in re.split(r"[.!?]+", t) if s.strip()]
    ns = max(len(sents), 1)
    chars = max(len(t), 1)
    uniq = len(set(w.lower() for w in words))
    def rate(pat):
        return 100.0 * len(re.findall(pat, t)) / chars
    return [
        len(words),                              # length (words)
        sum(len(w) for w in words) / n,          # avg word length
        n / ns,                                  # avg sentence length (words)
        rate(r","), rate(r"[.!?]"), rate(r"[;:]"),  # punctuation rates
        rate(r"[()\[\]]"), rate(r'["“”]'),
        100.0 * sum(1 for c in t if c.isupper()) / chars,   # uppercase rate
        100.0 * sum(1 for c in t if c.isdigit()) / chars,   # digit rate
        uniq / n,                                # type-token ratio
        100.0 * sum(1 for w in words if w.lower() in FUNCTION) / n,  # function-word %
        rate(r"\n"),                             # newline rate (structure)
    ]

NAMES = ["n_words","avg_word_len","avg_sent_len","comma_rt","stop_punct_rt",
         "semicolon_rt","bracket_rt","quote_rt","upper_rt","digit_rt","ttr",
         "func_word_pct","newline_rt"]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("files", nargs="+")
    ap.add_argument("-o", "--outdir", required=True)
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--max", type=int, default=4000)
    ap.add_argument("--test-frac", type=float, default=0.3)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    items = []
    for fp in args.files:
        for line in open(fp):
            line = line.strip()
            if not line:
                continue
            d = json.loads(line)
            h = [a for a in d.get("human_answers", []) if a and len(a.split()) >= 20]
            g = [a for a in d.get("chatgpt_answers", []) if a and len(a.split()) >= 20]
            if h and g:
                items.append((h[0], g[0]))
    rng.shuffle(items); items = items[:args.max]

    pairs = []
    for human, gpt in items:
        fh, fg = feats(human), feats(gpt)
        if rng.random() < 0.5:
            pairs.append((fh, fg, "P2_LLM"))     # p1 human, p2 LLM
        else:
            pairs.append((fg, fh, "P1_LLM"))
    n_test = int(len(pairs) * args.test_frac)
    splits = {"test": pairs[:n_test], "train": pairs[n_test:]}

    outdir = Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)
    def write(path, data, name):
        with open(path, "w") as f:
            f.write(f"@relation {name}\n\n")
            for side in ("p1", "p2"):
                for nm in NAMES:
                    f.write(f"@attribute {side}_{nm} numeric\n")
            f.write("@attribute pair_class {P1_LLM,P2_LLM}\n\n@data\n")
            for p1, p2, label in data:
                f.write(",".join(f"{v:g}" for v in p1 + p2) + f",{label}\n")
    write(outdir / "hc3_train.arff", splits["train"], "hc3_llm_vs_human_train")
    write(outdir / "hc3_test.arff", splits["test"], "hc3_llm_vs_human_test")
    with open(outdir / "hc3_all.arff", "w") as f:
        f.write((outdir / "hc3_train.arff").read_text())
        for line in (outdir / "hc3_test.arff").read_text().splitlines():
            t = line.strip()
            if t and not t.startswith("@"):
                f.write(line + "\n")
    print(f"hc3: {len(pairs)} (human,LLM) pairs -> train {len(splits['train'])}, "
          f"test {len(splits['test'])}; {len(NAMES)} stylometric features/side")

if __name__ == "__main__":
    main()
