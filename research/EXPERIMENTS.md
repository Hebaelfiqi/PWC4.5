# Experimental Design

Evaluation protocol for benchmarking PWC4.5 against pairwise/ranking baselines.

## Research questions

- **RQ1 (accuracy).** How does PWC4.5's pair-classification accuracy compare to
  pairwise SVM, RankNet, and LambdaMART on PWCCP tasks?
- **RQ2 (small-sample robustness).** How do the methods compare as training set
  size shrinks (learning curves)? Hypothesis: PWC4.5 degrades more gracefully
  on very small paired data.
- **RQ3 (noise robustness).** How does accuracy degrade with label/feature
  noise? (The synthetic datasets already vary noise 0.0–0.25.)
- **RQ4 (interpretability).** Qualitative: tree size / rule readability of
  PWC4.5 vs. the opacity of RankNet/LambdaMART; can the learned relations be
  read as stylistic markers?

## Task definition (common to all methods)

Every method decides the **ordered-pair assignment**: given a pair (a, b) known
to contain one instance of each class, predict whether a→ClassA, b→ClassB or
the reverse. Metric = **pair-classification accuracy**, identical to the
original papers, so numbers are directly comparable to the published PWC4.5
results.

To prevent position leakage, each pair is randomly oriented before feature
construction (the label flips with the orientation).

## Methods

| Method | Representation | Notes |
|--------|----------------|-------|
| **PWC4.5** (ours) | native paired rows | relation splits (min/eq/max), gain ratio |
| C4.5 / J48 | single rows | traditional tree baseline (from the papers) |
| GBT + C4.5 | difference vector φ(a)−φ(b) | "Gradient-Based Transformation" from TALLIP 2016 |
| **Pairwise SVM** | difference vector φ(a)−φ(b) | linear + RBF kernels |
| **RankNet** | item scores, pairwise cross-entropy | 2-layer MLP |
| **LambdaMART** | item features, group size 2 | gradient-boosted ranking trees |

The difference-vector representation φ(a)−φ(b) is the standard pairwise
transform and coincides with the paper's GBT baseline, so the SVM/RankNet/
LambdaMART comparison is methodologically continuous with the original work.

## Protocol

- **Datasets:** in-repo PWCCP datasets first (synthetic 2D/5D across 8 noise
  levels; 21 translator pairs), then external paired datasets
  ([DATASETS.md](DATASETS.md)).
- **Splits:** use each dataset's provided train/test where available; otherwise
  grouped k-fold CV with **pairs kept intact within a fold** (the pair-as-
  instance representation guarantees this).
- **Repetitions:** average over the 10 provided experiment replications
  (translator) or 10 sampled datasets (synthetic), matching the original
  protocol; report mean ± std.
- **Feature scaling:** standardize difference vectors for SVM/RankNet
  (motif-frequency magnitudes span several orders); trees (PWC4.5, LambdaMART)
  are scale-invariant.
- **Significance:** one-tailed paired t-test across replications (α = 0.05),
  matching the original papers; also report Wilcoxon signed-rank as a
  distribution-free check.
- **Hyperparameters:** small grid per baseline (SVM C/γ; RankNet hidden units,
  lr, epochs; LambdaMART leaves/lr/estimators), tuned on a validation split,
  reported for reproducibility.

## Deliverable tables

- **T1** — per-translator-pair accuracy: PWC4.5 vs. each baseline (21 rows).
- **T2** — synthetic accuracy vs. noise level (2D and 5D).
- **T3** — learning curves (accuracy vs. training-set fraction) per method.
- **T4** — interpretability summary (PWC4.5 tree size + example rules).

## Preliminary results

A first validated run of the harness on the in-repo datasets is recorded in
[results/](results/) with the exact commands to reproduce. These use the
existing 21 translator pairs and the synthetic data; the external datasets and
learning-curve / significance sweeps are the remaining planned work.
