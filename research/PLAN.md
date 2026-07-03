# PWC4.5 — Extension and Evaluation Plan

Forward-looking development plan for turning PWC4.5 from a standalone,
two-class, numeric-only implementation into (a) a WEKA-integrated classifier
and (b) a method benchmarked against modern pairwise/ranking baselines.

> This work lives on the `research/` branch and does **not** affect the
> released v1.0.0 artifact on `main` / Zenodo.

## Motivation

PWC4.5 addresses Pairwise Comparative Classification Problems (PWCCP), where
the discriminative signal is the *relationship between paired instances*, not
their individual values. Its distinctive value versus modern pairwise/ranking
methods (pairwise SVM, RankNet, LambdaMART, Siamese networks) is **not** raw
accuracy but **interpretability** (a readable decision tree over min/eq/max
relations) and **robustness on small paired datasets** — the regime typical of
forensics, biometric verification, and matched biomedical studies. The plan
leans into that positioning.

## Workstreams

### A. WEKA integration
Implement `weka.classifiers.AbstractClassifier` so PWC4.5 is usable in the
WEKA Explorer, Experimenter, and KnowledgeFlow, and directly comparable to
J48/RandomForest with WEKA's cross-validation and paired significance tests.

**Key design decision — pair-as-single-instance.** WEKA's data model is iid,
one row at a time; its CV/splitting would shuffle rows and break the current
"two consecutive rows = a pair" convention. The fix is representational:
encode each pair as **one** instance with `2n` attributes (p1's `n` features
followed by p2's `n` features) plus the ordered-assignment class. PWC4.5 then
computes its R(min/eq/max) relations internally from those `2n` attributes.
This makes the pairing invisible to WEKA — CV folds cannot split a pair, and
the GUI/Experimenter "just work".

### B. Scope extensions (widen applicability)
1. **Nominal attributes** — redefine the within-pair relation for nominal
   values as equal / not-equal (current min/eq/max is numeric-only). Low
   effort, meaningfully wider audience.
2. **Multi-class CCP** — generalise the relation R() from {min, eq, max} to a
   scale/range over more than two classes, as flagged in the TALLIP 2016
   Future Work. Genuine research increment; candidate for a follow-up paper.

### C. Comparative evaluation
Benchmark PWC4.5 against pairwise SVM, RankNet, and LambdaMART (plus the C4.5
and GBT baselines from the original papers) on both the in-repo PWCCP datasets
and additional public paired/comparative datasets. See
[EXPERIMENTS.md](EXPERIMENTS.md) and [DATASETS.md](DATASETS.md).

## Sequenced roadmap

| # | Step | Depends on | Effort | Status |
|---|------|-----------|--------|--------|
| 1 | Pair-as-instance representation + ARFF converter | — | S | in progress |
| 2 | Baseline harness (pairwise SVM, RankNet, LambdaMART) | 1 | M | in progress |
| 3 | Validated comparison on in-repo PWCCP datasets | 1,2 | S | in progress |
| 4 | Acquire + convert external paired datasets | 1 | M | planned |
| 5 | Full comparison sweep + significance tests | 2,4 | M | planned |
| 6 | WEKA `AbstractClassifier` (pair-as-instance) port | 1 | L | scaffolded |
| 7 | Nominal-attribute relation extension | 6 | M | planned |
| 8 | Multi-class CCP relation extension | 6 | L | planned |
| 9 | Paper: WEKA package + benchmark study | 3,5,6 | L | planned |
| 10 | Paper: multi-class CCP generalisation | 7,8 | L | planned |

Effort: S ≈ days, M ≈ 1–2 weeks, L ≈ a month+.

## Papers this enables

- **Artifact/benchmark paper** — "PWC4.5 as a WEKA classifier: interpretable
  pairwise comparative classification, benchmarked against pairwise SVM,
  RankNet and LambdaMART." Built on steps 3, 5, 6.
- **Methods paper** — "Generalising pairwise comparative classification to
  the multi-class case" (general CCP). Built on steps 7, 8.

## Honest risk assessment

- PWC4.5 will likely **trail** LambdaMART/RankNet on accuracy for larger data;
  the defensible claims are interpretability and small-sample robustness. The
  experiments must be designed to test *those* claims (learning curves, tree
  readability), not just a single accuracy leaderboard.
- If the method stays strictly two-class numeric-only, the audience is narrow
  and the WEKA effort may exceed the reuse return. The scope extensions
  (B1/B2) are what make the packaging clearly worthwhile.
