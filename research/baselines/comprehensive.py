"""Comprehensive cross-validated comparison: the GATED PWC4.5 (internal-CV
split-family selection) against the fixed PWC4.5 variants and the standard
pairwise/linear baselines, on every dataset. Also reports which configuration
the gate chose per dataset — the test of whether the gate resolves the
relational-vs-absolute tension.

Metric: k-fold CV pair accuracy (10-fold n<3000 else 5-fold).
"""
import sys, warnings, numpy as np
warnings.filterwarnings("ignore")
sys.path.insert(0, "research/baselines")
import pair_arff_loader as L
import pwccp_data
from pwc45_py import PWC45Py, GatedPWC45
import models as M
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
    return (np.vstack([d["train"]["A"], d["test"]["A"]]),
            np.vstack([d["train"]["B"], d["test"]["B"]]),
            np.concatenate([d["train"]["y"], d["test"]["y"]]))

DATASETS = [
    ("5D-XOR",         lambda: from_pwccp("data/5d_data/1st_exp/5D_Noise_0.0"), "non-transitive"),
    ("categorical-XOR",lambda: from_arff(f"{S}/catxor/cat_xor_train.arff"),      "non-transitive"),
    ("Condorcet k5",   lambda: from_arff(f"{S}/cond5/condorcet_all.arff"),       "non-transitive"),
    ("Translators",    lambda: from_pwccp("data/translators_data/asad_daryabadi/M3_M4_Asad_Daryabadi_Exp_1"), "relational/subtle"),
    ("Pokemon",        lambda: from_arff(f"{S}/d8/pokemon_all.arff", 4000),      "relational/dominant"),
    ("Mantis shrimp",  lambda: from_arff(f"{S}/d10/shrimp_all.arff"),            "relational/small"),
    ("Twins",          lambda: from_arff(f"{S}/d11/twins_all.arff"),            "relational"),
    ("infert",         lambda: from_arff(f"{S}/d3/infert_all.arff"),            "relational"),
    ("birthwt",        lambda: from_arff(f"{S}/d7/birthwt_all.arff"),           "magnitude"),
    ("Parkinson's",    lambda: from_arff(f"{S}/d4/parkinsons_all.arff"),        "magnitude"),
    ("HC3",            lambda: from_arff(f"{S}/d12/hc3_all.arff"),              "magnitude/mixed"),
    ("SUSHI",          lambda: from_arff(f"{S}/d5/sushi_all.arff"),             "absolute"),
    ("TravelMode",     lambda: from_arff(f"{S}/d6/travelmode_all.arff"),        "absolute"),
    ("Dota2",          lambda: from_arff(f"{S}/d9/dota2_all.arff", 4000),       "additive"),
]

def run():
    cols = ["Relational", "Value", "GATED", "LogReg", "SVM", "LambdaMART"]
    print(f"{'dataset':<17}{'regime':<20}" + "".join(f"{c:>12}" for c in cols) + "   gate picks")
    for name, loader, regime in DATASETS:
        A, B, y = loader()
        folds = 10 if len(y) < 3000 else 5
        skf = StratifiedKFold(folds, shuffle=True, random_state=1)
        acc = {c: [] for c in cols}; picks = {}
        for tr, te in skf.split(A, y):
            trd = {"A": A[tr], "B": B[tr], "y": y[tr], "diff": A[tr] - B[tr]}
            ted = {"A": A[te], "B": B[te], "y": y[te], "diff": A[te] - B[te]}
            acc["Relational"].append(PWC45Py(prune=True, min_leaf=10).fit(trd).accuracy(ted))
            acc["Value"].append(PWC45Py(prune=True, min_leaf=10, value_split=True,
                                        value_aggs=(1,), numeric_penalty=True).fit(trd).accuracy(ted))
            g = GatedPWC45(min_leaf=10).fit(trd)
            acc["GATED"].append(g.accuracy(ted)); picks[g.chosen] = picks.get(g.chosen, 0) + 1
            d_tr, d_te = A[tr] - B[tr], A[te] - B[te]
            sc = StandardScaler().fit(d_tr)
            lr = LogisticRegression(max_iter=1000).fit(sc.transform(d_tr), y[tr])
            acc["LogReg"].append(lr.score(sc.transform(d_te), y[te]))
            for mn, key in [("PairwiseSVM", "SVM"), ("LambdaMART", "LambdaMART")]:
                mdl = M.all_models(seed=7)[mn]; mdl.fit(trd)
                acc[key].append(mdl.accuracy(ted))
        means = {c: 100 * np.mean(acc[c]) for c in cols}
        pick_str = ",".join(f"{k}:{v}" for k, v in sorted(picks.items(), key=lambda x: -x[1]))
        print(f"{name:<17}{regime:<20}" + "".join(f"{means[c]:>11.1f}" for c in cols) + f"   {pick_str}")

if __name__ == "__main__":
    run()
