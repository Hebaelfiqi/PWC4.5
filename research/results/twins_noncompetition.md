# Real non-competition paired data: discordant-twin mortality

**Dataset:** Twins (Louizos et al., CEVAE; US twin births 1989–91,
github.com/AMLab-Amsterdam/CEVAE). ~12k same-sex twin pairs; twins share every
pregnancy/parent covariate and differ in birth weight; outcome = first-year
mortality of each twin. Widely used in causal-inference ML. **Not a
competition** — a matched-pair medical/demographic dataset.

**Task:** among the 2,056 *discordant* pairs (exactly one twin died), predict
which twin died. The pairing cancels shared genetic/environmental confounds, so
the residual signal is the within-pair birth-weight relation (the lighter twin
has higher mortality). Shared covariates are included but yield 'eq' relations.

## Result (10-fold CV, 2,056 discordant pairs)

| Method | Accuracy | Model size |
|---|---:|---:|
| Pairwise SVM | 62.6% | — |
| **PWC4.5** | **62.4%** | **3 leaves** |
| RankNet | 62.4% | — |
| LambdaMART | 58.3% | — |
| J48 (C4.5) | 57.5% | **112 leaves** |

PWC4.5 ties the best methods and beats C4.5 by ~5 points **with a 3-leaf tree
where C4.5 needs 112** — and the tree is medically correct:

```
R(birthweight) = min : this twin died     (the lighter twin has higher mortality)
R(birthweight) = max : the other twin died
```

## Why it matters

This is the "shared-source cancellation" mechanism realised **beyond
competition**: twins are the ultimate matched pair (shared genes + environment),
so the pairing removes confounds and exposes the within-pair relation — exactly
the PWCCP thesis, in epidemiology rather than a matchup. As on the other
relational datasets, PWC4.5 matches the black-box pairwise methods on accuracy
while being an interpretable 3-leaf rule, and it dominates C4.5 on both accuracy
and model size (3 vs 112 leaves).

Modest absolute accuracy is expected: the birth-weight → mortality effect is
weak (population ATE ≈ 2.5%), so predicting which discordant twin died is
inherently hard for *every* method. The contribution is the interpretable,
confounder-free relational rule that recovers the known epidemiology.

## Reproduce

```sh
python research/tools/convert_twins.py \
  twin_pairs_X_3years_samesex.csv twin_pairs_T_3years_samesex.csv \
  twin_pairs_Y_3years_samesex.csv -o d11
java --add-opens java.base/java.lang=ALL-UNNAMED -cp weka.jar:pwc45.jar \
     weka.classifiers.trees.PWC45 -x 10 -t d11/twins_all.arff
```
