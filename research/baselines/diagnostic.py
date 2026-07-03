"""Thorough cross-validated diagnostic of PWC4.5 and its tweaks across every
dataset collected. Objective: characterise WHEN the relational method excels,
WHEN it fails, and WHICH tweak helps — not to maximise any single number.

Variants (all with C4.5 pessimistic pruning unless noted):
  Relational      original PWC4.5 (sign min/eq/max)
  +Value+pen      + C4.5 threshold on max-of-pair aggregate, numeric penalty
  +Magnitude      + standardised dead-zone magnitude relation
  Hybrid          + value(max)+penalty + magnitude
  LogReg(diff)    logistic regression on the difference vector (linear ref)

Metric: k-fold CV pair accuracy (10-fold for n<3000, else 5-fold).
"""
import sys, warnings, numpy as np
warnings.filterwarnings("ignore")
sys.path.insert(0, "research/baselines")
import pair_arff_loader as L
import pwccp_data
from pwc45_py import PWC45Py
from sklearn.model_selection import StratifiedKFold
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler

S = sys.argv[1]

def from_arff(path, cap=None):
    d = L.load(path, path)["train"]; A, B, y = d["A"], d["B"], d["y"]
    if cap and len(y) > cap:
        rng = np.random.default_rng(0); i = rng.choice(len(y), cap, replace=False)
        A, B, y = A[i], B[i], y[i]
    return A, B, y

def from_pwccp(stem):
    d = pwccp_data.load(stem)
    A = np.vstack([d["train"]["A"], d["test"]["A"]])
    B = np.vstack([d["train"]["B"], d["test"]["B"]])
    y = np.concatenate([d["train"]["y"], d["test"]["y"]])
    return A, B, y

# name: (loader, regime)
DATASETS = [
    # NB: 2D-XOR is degenerate under random orientation — with an EVEN number of
    # features XOR(p1's relations)=XOR(p2's relations), so once the pair is
    # randomly oriented the features cannot identify the label. Only odd-feature
    # XOR (5D) is a valid PWCCP under the honest "unknown orientation" setting.
    ("5D-XOR noise0",   lambda: from_pwccp("data/5d_data/1st_exp/5D_Noise_0.0"),  "relational/non-transitive"),
    ("cat-XOR",         lambda: from_arff(f"{S}/catxor/cat_xor_train.arff"),      "nominal/non-transitive"),
    ("Condorcet k5",    lambda: from_arff(f"{S}/cond5/condorcet_all.arff"),       "non-transitive"),
    ("Translators(AD)", lambda: from_pwccp("data/translators_data/asad_daryabadi/M3_M4_Asad_Daryabadi_Exp_1"), "relational/subtle"),
    ("Pokemon",         lambda: from_arff(f"{S}/d8/pokemon_all.arff", 6000),      "relational/dominant"),
    ("Mantis shrimp",   lambda: from_arff(f"{S}/d10/shrimp_all.arff"),            "relational/small"),
    ("Twins",           lambda: from_arff(f"{S}/d11/twins_all.arff"),            "relational"),
    ("infert (CC)",     lambda: from_arff(f"{S}/d3/infert_all.arff"),            "relational"),
    ("birthwt (CC)",    lambda: from_arff(f"{S}/d7/birthwt_all.arff"),           "magnitude"),
    ("Parkinson's",     lambda: from_arff(f"{S}/d4/parkinsons_all.arff"),        "magnitude/temporal"),
    ("HC3 LLM-human",   lambda: from_arff(f"{S}/d12/hc3_all.arff"),              "magnitude/mixed"),
    ("SUSHI",           lambda: from_arff(f"{S}/d5/sushi_all.arff"),             "absolute"),
    ("TravelMode",      lambda: from_arff(f"{S}/d6/travelmode_all.arff"),        "absolute"),
    ("Dota2",           lambda: from_arff(f"{S}/d9/dota2_all.arff", 5000),       "additive"),
]

VARIANTS = {
    "Relational":  dict(prune=True),
    "+Value+pen":  dict(prune=True, value_split=True, value_aggs=(1,), numeric_penalty=True),
    "+Magnitude":  dict(prune=True, magnitude=True, mag_kind="std", deadzone=True),
    "Hybrid":      dict(prune=True, value_split=True, value_aggs=(1,), numeric_penalty=True,
                        magnitude=True, mag_kind="std", deadzone=True),
}

def run():
    cols = list(VARIANTS) + ["LogReg(diff)"]
    print(f"{'dataset':<17}{'regime':<26}" + "".join(f"{c:>13}" for c in cols))
    rows = []
    for name, loader, regime in DATASETS:
        A, B, y = loader()
        folds = 10 if len(y) < 3000 else 5
        skf = StratifiedKFold(folds, shuffle=True, random_state=1)
        accs = {c: [] for c in cols}
        for tr, te in skf.split(A, y):
            trd = {"A": A[tr], "B": B[tr], "y": y[tr]}
            for vn, kw in VARIANTS.items():
                m = PWC45Py(min_leaf=10, **kw).fit(trd)
                accs[vn].append(m.accuracy({"A": A[te], "B": B[te], "y": y[te]}))
            sc = StandardScaler().fit(A[tr] - B[tr])
            lr = LogisticRegression(max_iter=1000).fit(sc.transform(A[tr] - B[tr]), y[tr])
            accs["LogReg(diff)"].append(lr.score(sc.transform(A[te] - B[te]), y[te]))
        means = {c: 100 * np.mean(accs[c]) for c in cols}
        rows.append((name, regime, means))
        print(f"{name:<17}{regime:<26}" + "".join(f"{means[c]:>12.1f}" for c in cols))
    return rows

if __name__ == "__main__":
    run()
