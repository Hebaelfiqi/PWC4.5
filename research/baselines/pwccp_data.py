"""Load PWCCP datasets (C4.5 .names/.data/.test) into a pairwise representation.

Every two consecutive rows in a .data/.test file form one pair (p1, p2). We
build, per pair, a randomly oriented ordered pair so no method can exploit
row position:

    orient with prob 0.5 -> (a, b) = (p1, p2) else (p2, p1)
    label y = 1 if `a` belongs to the first class named in .names else 0

Two views are returned:
  - difference vectors  d = phi(a) - phi(b)   (for SVM / RankNet)
  - item pairs (phi(a), phi(b))               (for LambdaMART ranking)
"""
import numpy as np
from pathlib import Path


def _ext(stem: Path, ext: str) -> Path:
    # NB: cannot use Path.with_suffix — filestems like "2D_Noise_0.0" contain a
    # dot that with_suffix would misread as the existing extension.
    return Path(str(stem) + ext)


def read_names(stem: Path):
    classes, attrs = [], []
    for raw in _ext(stem, ".names").read_text().splitlines():
        line = raw.strip().rstrip(".")
        if not line or line.startswith("|"):
            continue
        if not classes:
            classes = [c.strip() for c in line.split(",")]
        else:
            attrs.append(line.split(":")[0].strip())
    return classes, attrs


def _read_pairs(path: Path, n_attr: int):
    rows = []
    for raw in path.read_text().splitlines():
        line = raw.strip()
        if not line:
            continue
        parts = [p.strip() for p in line.split(",")]
        x = np.array([float(v) for v in parts[:n_attr]], dtype=float)
        rows.append((x, parts[-1]))
    # consecutive rows form pairs
    pairs = [(rows[i], rows[i + 1]) for i in range(0, len(rows) - 1, 2)]
    return pairs


def load(stem: str, seed: int = 0):
    """Return dict with oriented difference vectors and item pairs for the
    train (.data) and test (.test) files sharing `stem`."""
    stem = Path(stem)
    classes, attrs = read_names(stem)
    class_a = classes[0]
    n = len(attrs)
    rng = np.random.default_rng(seed)

    def build(pairs):
        A, B, D, Y = [], [], [], []
        for (x1, c1), (x2, c2) in pairs:
            if rng.random() < 0.5:
                (a, ca), (b, cb) = (x1, c1), (x2, c2)
            else:
                (a, ca), (b, cb) = (x2, c2), (x1, c1)
            A.append(a); B.append(b)
            D.append(a - b)
            Y.append(1 if ca == class_a else 0)
        return (np.array(A), np.array(B), np.array(D), np.array(Y))

    tr = build(_read_pairs(_ext(stem, ".data"), n))
    te = build(_read_pairs(_ext(stem, ".test"), n))
    return {"classes": classes, "n_attr": n,
            "train": {"A": tr[0], "B": tr[1], "diff": tr[2], "y": tr[3]},
            "test": {"A": te[0], "B": te[1], "diff": te[2], "y": te[3]}}
