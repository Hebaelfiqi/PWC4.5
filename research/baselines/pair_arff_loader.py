"""Load a pair-as-instance ARFF (train + test) into the numeric pairwise
representation used by the Python baselines, one-hot encoding nominal
attributes so SVM / RankNet / LambdaMART can consume mixed data.

Returns the same dict shape as pwccp_data.load: A, B, diff, y per split.
Pairs are already oriented in the ARFF (class = class of p1); y = 1 if p1's
class is the first declared class value.
"""
import re
import numpy as np
from pathlib import Path


def _parse(path):
    attrs = []            # list of (name, values_or_None)
    data = []
    in_data = False
    for line in Path(path).read_text().splitlines():
        s = line.strip()
        if not s or s.startswith("%"):
            continue
        low = s.lower()
        if low.startswith("@attribute"):
            m = re.match(r"@attribute\s+(\S+)\s+(.+)", s, re.I)
            name, spec = m.group(1), m.group(2).strip()
            if spec.startswith("{"):
                vals = [v.strip().strip("'") for v in spec.strip("{}").split(",")]
                attrs.append((name, vals))
            else:
                attrs.append((name, None))
        elif low.startswith("@data"):
            in_data = True
        elif in_data:
            data.append([c.strip().strip("'") for c in s.split(",")])
    return attrs, data


def load(train_path, test_path):
    attrs, tr_rows = _parse(train_path)
    _, te_rows = _parse(test_path)
    n = (len(attrs) - 1) // 2
    class_vals = attrs[-1][1]
    p1_specs = attrs[:n]

    # build per-feature encoders from the p1 attribute declarations
    def encode_side(row, base):
        out = []
        for i, (name, vals) in enumerate(p1_specs):
            v = row[base + i]
            if vals is None:
                out.append(float(v))
            else:
                out.extend(1.0 if v == cat else 0.0 for cat in vals)
        return out

    def build(rows):
        A, B, y = [], [], []
        for r in rows:
            A.append(encode_side(r, 0))
            B.append(encode_side(r, n))
            y.append(1 if r[-1] == class_vals[0] else 0)
        A, B = np.array(A), np.array(B)
        return {"A": A, "B": B, "diff": A - B, "y": np.array(y)}

    return {"classes": class_vals, "train": build(tr_rows), "test": build(te_rows)}
