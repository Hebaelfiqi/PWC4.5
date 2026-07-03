# Preliminary comparison results

Validated first-pass comparison of PWC4.5 against pairwise SVM, RankNet, and
LambdaMART on the in-repo PWCCP datasets. Reproduced on Eclipse Temurin JDK 17
with the baseline harness in [`../baselines/`](../baselines/).

**Reproduce:**
```sh
python research/baselines/run_comparison.py translators   # -> translators_comparison.txt
python research/baselines/run_comparison.py synthetic      # -> synthetic_comparison.txt
```
(PWC4.5 numbers come from the compiled JAR; baselines from the Python harness.
All methods solve the identical ordered-pair task; metric = pair accuracy.)

## Synthetic data (relational / XOR structure)

Mean accuracy over 10 experiments, by noise level:

**2D**

| Noise | PWC4.5 | PairwiseSVM | RankNet | LambdaMART |
|-------|-------:|------------:|--------:|-----------:|
| 0.00  | 100.00 | 57.70 | 59.00 | 59.60 |
| 0.05  | 100.00 | 56.50 | 56.10 | 58.80 |
| 0.10  | 100.00 | 55.80 | 54.80 | 59.10 |
| 0.15  | 100.00 | 56.30 | 54.10 | 59.70 |
| 0.20  |  98.20 | 56.50 | 57.30 | 59.90 |
| 0.25  |  82.90 | 57.40 | 56.50 | 59.30 |

**5D**

| Noise | PWC4.5 | PairwiseSVM | RankNet | LambdaMART |
|-------|-------:|------------:|--------:|-----------:|
| 0.00  | 100.00 | 60.35 | 60.80 | 53.90 |
| 0.10  |  99.20 | 62.25 | 61.00 | 53.25 |
| 0.25  |  63.65 | 58.90 | 60.55 | 52.05 |

**Finding.** On data whose signal is a pairwise XOR-of-relations, PWC4.5
recovers it (≈100% at low noise) while all three baselines sit near chance
(~55–62%). This is **representational, not a tuning artefact**: the
difference-vector (SVM) and independent item-scoring (RankNet, LambdaMART)
representations cannot express an XOR over within-pair min/max relations. This
is the sharpest demonstration of when PWC4.5's relational splits are necessary.

## Translator stylometry (real data, 21 pairs)

Mean accuracy over 10 experiments per pair (network-motif features):

| Method | Average accuracy |
|--------|-----------------:|
| **RankNet** | **80.14** |
| **PWC4.5** | **78.27** |
| PairwiseSVM | 61.89 |
| LambdaMART | 58.76 |

Per-pair numbers in [translators_comparison.txt](translators_comparison.txt).

**Finding.** On real data PWC4.5 is competitive with a neural pairwise ranker
(RankNet, +1.9 pts) and clearly ahead of pairwise SVM (+16.4) and LambdaMART
(+19.5) — while remaining a readable decision tree. LambdaMART underperforms
here as expected for gradient-boosted ranking on ~50 training pairs: it is the
most data-hungry method, which supports the small-sample-robustness hypothesis
(RQ2).

## Honest caveats

- **Baseline tuning is light** (near-default hyperparameters). The synthetic
  finding is representational and tuning-invariant, but the *translator*
  margins could shift with a full hyperparameter sweep — planned (EXPERIMENTS
  RQ1). RankNet/SVM/LambdaMART results should be read as competent-default, not
  best-possible.
- **A fair stronger baseline** would be a Siamese network with explicit
  interaction features (able, in principle, to represent relations); adding it
  is planned so the comparison cannot be dismissed as strawman.
- **External datasets pending** — these results are on the in-repo datasets
  only. Generalisation to PAN / LFW / MSLR ([../DATASETS.md](../DATASETS.md))
  is the next step before any publication claim.
- Baselines run on a randomly oriented ordered-pair task (seeded); PWC4.5 runs
  via its own `-o` randomisation. Both measure the same pair-accuracy metric.
