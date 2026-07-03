"""Uniform 10-fold CV of the Python baselines on the applied pair-ARFF datasets,
matching the protocol used for the WEKA methods (plain stratified 10-fold on
the combined pair-instances). Prints accuracy per method so the full
six-method matrix can be assembled on one protocol.

Usage: python cv_applied.py <all.arff> [--folds 10] [--seed 1]
"""
import sys, argparse, warnings
from pathlib import Path
import numpy as np
warnings.filterwarnings("ignore")
sys.path.insert(0, str(Path(__file__).parent))
import pair_arff_loader as L
import models as M
from sklearn.model_selection import StratifiedKFold


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("arff")
    ap.add_argument("--folds", type=int, default=10)
    ap.add_argument("--seed", type=int, default=1)
    args = ap.parse_args()

    d = L.load(args.arff, args.arff)["train"]   # load all pairs
    A, B, diff, y = d["A"], d["B"], d["diff"], d["y"]
    skf = StratifiedKFold(n_splits=args.folds, shuffle=True, random_state=args.seed)

    names = ["PairwiseSVM", "RankNet", "LambdaMART"]
    accs = {n: [] for n in names}
    for tr, te in skf.split(A, y):
        sp_tr = {"A": A[tr], "B": B[tr], "diff": diff[tr], "y": y[tr]}
        sp_te = {"A": A[te], "B": B[te], "diff": diff[te], "y": y[te]}
        for n, mdl in M.all_models(seed=args.seed).items():
            try:
                mdl.fit(sp_tr); accs[n].append(mdl.accuracy(sp_te))
            except Exception:
                accs[n].append(float("nan"))
    stem = Path(args.arff).stem
    print(f"{stem:<24}" + "".join(f"{n}={100*np.nanmean(accs[n]):.1f}  " for n in names))


if __name__ == "__main__":
    main()
