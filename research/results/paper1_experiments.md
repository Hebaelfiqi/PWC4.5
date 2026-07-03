# Paper 1 — experiments E1–E5 (results)

All figures reproduced on Eclipse Temurin JDK 17, WEKA 3.8.6. PWC45 = the
`pwc45` package classifier (pruned, CF=0.25). Metric = pair-classification
accuracy. Translator/synthetic numbers from the earlier baseline harness
([README.md](README.md)); applied datasets via 10-fold CV in WEKA.

## Headline table

| # | Dataset | Signal type | PWC4.5 | J48 (C4.5) | GBT+J48 | Best non-PWC baseline | PWC4.5 tree (leaves) |
|---|---------|-------------|-------:|-----------:|--------:|:----------------------|--------------------:|
| E1 | Translator stylometry (21 pairs) | relational | **78.3** | ~62 (CV) | — | RankNet 80.1 | small |
| E2a | Synthetic numeric XOR (noise 0) | relational | **100** | ~59 | ~59 | all ≈ chance | 4 |
| E2b | Synthetic categorical XOR (α≥5) | relational | **100** | ≤57 | — | all ≈ chance | 4 |
| E3 | Matched case–control (infert) | relational | **77.6** | 66.1 | — (mixed) | J48 66.1 | 5 |
| E4 | Parkinson's progression | temporal magnitude | 50.6 | 51.7 | **56.5** | GBT+J48 56.5 | 105 |
| E5 | SUSHI preference | absolute | 69.2 | **76.6** | — (mixed) | J48 76.6 | 23 |

## The finding: *when* relational splits help (this is the contribution)

The results are not "PWC4.5 always wins." They cleanly characterise the
regime where it does — which is more useful to a practitioner and more honest
to a reviewer:

- **PWC4.5 wins when the discriminative signal is the within-pair relation and
  absolute values are non-informative or confounded** — translator stylometry
  (text length varies, relations are invariant), matched case–control
  (matched covariates cancel; unmatched relations carry the signal), and the
  XOR constructions (relation-parity by design). On matched case–control it
  beats C4.5 by **+11.5 points with a 5-leaf tree** that reads medically
  (fewer spontaneous abortions within the pair → control).
- **PWC4.5 does not help when the signal is absolute** — SUSHI preference is
  largely driven by item identity/quality (some items are universally
  popular), which a value-splitting tree can read directly and a relational
  tree discards. Honest loss (−7.4 vs J48).
- **PWC4.5 does not help when the signal is temporal magnitude** — for
  Parkinson's progression the informative quantity is *how much* a biomarker
  changed, not merely its ordinal direction. Tellingly, **GBT (the
  difference vector, which preserves magnitude) is the only method above
  chance** (56.5 vs ~51). This is the sharpest possible motivation for the
  temporal relation family of Paper 2: point relations {min,eq,max} throw away
  exactly the magnitude that progression needs.

### The decision rule (paper deliverable)

> Use PWC4.5 when pairs are linked by a shared source/context so that absolute
> feature values are confounded or vary across pairs, and the class depends on
> the *ordinal within-pair relationship*. Do not use it when the class is a
> function of absolute feature values, or when it depends on the *magnitude*
> of change within the pair (use GBT / a temporal extension there).

This turns two "negative" results into a scope characterisation — the E4/E5
losses are load-bearing evidence for the rule, not weaknesses to hide.

## Reproduce

```sh
# datasets
python research/tools/convert_infert.py raw/infert.csv -o d3
python research/tools/convert_parkinsons.py raw/parkinsons_updrs.data -o d4
python research/tools/convert_sushi.py raw/sushi3-2016 -o d5
python research/tools/make_diff_arff.py d4/parkinsons_all.arff d4/parkinsons_all_diff.arff  # GBT

# evaluation (WEKA, 10-fold CV) — PWC45 from the pwc45 package, e.g.:
java --add-opens java.base/java.lang=ALL-UNNAMED -cp weka.jar:pwc45.jar \
     weka.classifiers.trees.PWC45 -x 10 -t d3/infert_all.arff
```

Splits are group-disjoint (stratum for infert, subject for Parkinson's, user
for SUSHI) so no pair member leaks across train/test.
