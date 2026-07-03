"""Run the baseline comparison on the in-repo PWCCP datasets and, optionally,
PWC4.5 itself (via the compiled JAR) for a side-by-side table.

Usage:
    python run_comparison.py translators    # 21 translator pairs
    python run_comparison.py synthetic       # 2D/5D across noise levels
"""
import sys
import subprocess
import warnings
from pathlib import Path
import numpy as np

warnings.filterwarnings("ignore")

sys.path.insert(0, str(Path(__file__).parent))
import pwccp_data
import models as M

REPO = Path(__file__).resolve().parents[2]
DATA = REPO / "data"
JAR_CP = f"{REPO}/bin:{REPO}/lib/cli.jar"

TRANSLATOR_PAIRS = [
    ("asad_daryabadi", "M3_M4_Asad_Daryabadi"),
    ("asad_maududi", "M3_M4_Asad_Maududi"),
    ("asad_pickthall", "M3_M4_Asad_Pickthall"),
    ("asad_raza", "M3_M4_Asad_Raza"),
    ("asad_sarwar", "M3_M4_Asad_Sarwar"),
    ("asad_yousifali", "M3_M4_Asad_YousifAli"),
    ("daryabadi_maududi", "M3_M4_Daryabadi_Maududi"),
    ("daryabadi_pickthall", "M3_M4_Daryabadi_Pickthall"),
    ("daryabadi_raza", "M3_M4_Daryabadi_Raza"),
    ("daryabadi_sarwar", "M3_M4_Daryabadi_Sarwar"),
    ("daryabadi_yousifali", "M3_M4_Daryabadi_YousifAli"),
    ("maududi_pickthall", "M3_M4_Maududi_Pickthall"),
    ("maududi_raza", "M3_M4_Maududi_Raza"),
    ("maududi_sarwar", "M3_M4_Maududi_Sarwar"),
    ("maududi_yousifali", "M3_M4_Maududi_YousifAli"),
    ("pickthall_raza", "M3_M4_Pickthall_Raza"),
    ("pickthall_sarwar", "M3_M4_Pickthall_Sarwar"),
    ("pickthall_yousifali", "M3_M4_Pickthall_YousifAli"),
    ("raza_sarwar", "M3_M4_Raza_Sarwar"),
    ("raza_yousifali", "M3_M4_Raza_YousifAli"),
    ("sarwar_yousifali", "M3_M4_Sarwar_YousifAli"),
]
SEEDS = [42, 61, 13, 3, 82, 27, 19, 47, 93, 2]


def pwc45_accuracy(stem_dir, filestem, seed, out_dir, ordered=False):
    """Invoke the compiled PWC4.5 and parse its printed test accuracy."""
    args = ["java", "-cp", JAR_CP, "PWC45",
            "-ip", f"{stem_dir}//", "-f", filestem, "-u"]
    if ordered:
        args += ["-op", f"{out_dir}//", "-o", "-b", "-r", str(seed)]
    r = subprocess.run(args, capture_output=True, text=True, cwd=REPO)
    tail = r.stdout.strip().split("\n")[-1].strip().rstrip("%")
    try:
        return float(tail)
    except ValueError:
        return float("nan")


def eval_baselines_on_stem(stem_path, seed):
    data = pwccp_data.load(str(stem_path), seed=seed)
    out = {}
    for name, model in M.all_models(seed=seed).items():
        try:
            model.fit(data["train"])
            out[name] = 100.0 * model.accuracy(data["test"])
        except Exception as e:  # keep the sweep going
            out[name] = float("nan")
    return out


def run_translators(out_dir):
    names = ["PWC4.5", "PairwiseSVM", "RankNet", "LambdaMART"]
    print(f"{'pair':<22}" + "".join(f"{n:>13}" for n in names))
    agg = {n: [] for n in names}
    for folder, stem in TRANSLATOR_PAIRS:
        d = DATA / "translators_data" / folder
        pwc, sv, rn, lm = [], [], [], []
        for i, seed in enumerate(SEEDS, start=1):
            fs = f"{stem}_Exp_{i}"
            pwc.append(pwc45_accuracy(d, fs, seed, out_dir, ordered=True))
            b = eval_baselines_on_stem(d / fs, seed)
            sv.append(b["PairwiseSVM"]); rn.append(b["RankNet"]); lm.append(b["LambdaMART"])
        row = [np.nanmean(pwc), np.nanmean(sv), np.nanmean(rn), np.nanmean(lm)]
        for n, v in zip(names, row):
            agg[n].append(v)
        print(f"{folder:<22}" + "".join(f"{v:>12.2f}%" for v in row))
    print("-" * (22 + 13 * len(names)))
    print(f"{'AVERAGE':<22}" + "".join(f"{np.mean(agg[n]):>12.2f}%" for n in names))


def run_synthetic(out_dir):
    names = ["PWC4.5", "PairwiseSVM", "RankNet", "LambdaMART"]
    noise = ["0.0", "0.05", "0.1", "0.15", "0.2", "0.25"]
    for dim in ("2d", "5d"):
        print(f"\n=== {dim.upper()} synthetic (mean over 10 experiments) ===")
        print(f"{'noise':<8}" + "".join(f"{n:>13}" for n in names))
        exps = [f"{k}_exp" for k in
                ["1st","2nd","3rd","4th","5th","6th","7th","8th","9th","10th"]]
        for nz in noise:
            pwc, sv, rn, lm = [], [], [], []
            for e in exps:
                d = DATA / f"{dim}_data" / e
                fs = f"{dim.upper()}_Noise_{nz}"
                pwc.append(pwc45_accuracy(d, fs, 0, out_dir, ordered=False))
                b = eval_baselines_on_stem(d / fs, seed=0)
                sv.append(b["PairwiseSVM"]); rn.append(b["RankNet"]); lm.append(b["LambdaMART"])
            row = [np.nanmean(pwc), np.nanmean(sv), np.nanmean(rn), np.nanmean(lm)]
            print(f"{nz:<8}" + "".join(f"{v:>12.2f}%" for v in row))


if __name__ == "__main__":
    which = sys.argv[1] if len(sys.argv) > 1 else "translators"
    out_dir = Path("/tmp/pwc45_cmp"); out_dir.mkdir(exist_ok=True)
    if which == "translators":
        run_translators(out_dir)
    else:
        run_synthetic(out_dir)
