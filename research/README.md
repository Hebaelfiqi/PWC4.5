# PWC4.5 — research & extension (work in progress)

This directory holds forward-looking development that is **not** part of the
released v1.0.0 artifact: a WEKA integration, scope extensions, and a
comparison against modern pairwise/ranking baselines. It lives on the
`research/` branch and does not affect `main` or the Zenodo archive.

## Contents

- [PLAN.md](PLAN.md) — roadmap, WEKA design, scope extensions, sequenced steps.
- [EXPERIMENTS.md](EXPERIMENTS.md) — research questions, methods, protocol, tables.
- [DATASETS.md](DATASETS.md) — in-repo and external candidate datasets.
- [weka/](weka/) — `PWC45` as a `weka.classifiers.AbstractClassifier`
  (compiles & runs against Weka 3.8.6; validated).
- [tools/](tools/) — `make_pair_arff.py`: PWCCP → pair-as-instance ARFF.
- [baselines/](baselines/) — pairwise SVM, RankNet (numpy), LambdaMART (LightGBM),
  the shared data loader, and the comparison runner.
- [results/](results/) — validated preliminary comparison and how to reproduce.

## Reproduce (Python baselines)

```sh
python3 -m venv venv && . venv/bin/activate
pip install numpy scipy scikit-learn lightgbm
python baselines/run_comparison.py translators
python baselines/run_comparison.py synthetic
```

## Headline preliminary findings

- **Synthetic (relational XOR structure):** PWC4.5 ≈100% while pairwise SVM,
  RankNet, and LambdaMART sit at chance (~55–62%) — the difference-vector /
  item-scoring representations cannot express an XOR-of-relations. Sharpest
  demonstration of when the method is necessary.
- **Translator stylometry (real, 21 pairs):** PWC4.5 78.3% is competitive with
  RankNet 80.1% and well ahead of pairwise SVM 61.9% and LambdaMART 58.8%,
  while staying interpretable.
- **In WEKA:** on identical pair-instances, 10-fold CV gives PWC45 98% vs
  J48 (C4.5) 62% — the paper's core claim, reproduced through WEKA.

See [results/README.md](results/README.md) for tables and honest caveats
(light baseline tuning; external datasets pending).
