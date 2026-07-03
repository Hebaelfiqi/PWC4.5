# Full comparison: PWC4.5 vs all baselines

Every method on every dataset. Pair-classification accuracy (%). Higher is
better; **bold** = best per row; chance = 50 for the binary pair task.

## Matrix

| Dataset | Signal | PWC4.5 | J48 (C4.5) | GBT+J48 | Pairwise SVM | RankNet | LambdaMART |
|---|---|---:|---:|---:|---:|---:|---:|
| Translator stylometry (21 pairs) † | relational | 78.3 | ~62 | — | 61.9 | **80.1** | 58.8 |
| Synthetic numeric XOR (noise 0) † | relational | **100** | 59 | 59 | 57.7 | 59.0 | 59.6 |
| Synthetic categorical XOR (α=10) † | relational | **100** | 46 | — | — | — | — |
| Matched case–control (infert) | relational | 77.6 | 66.1 | — | 76.9 | **78.8** | 78.2 |
| Parkinson's progression | temporal magnitude | 50.6 | 51.7 | 56.5 | 52.3 | 54.7 | **70.2** ‡ |
| SUSHI preference | absolute | 69.2 | 76.6 | — | 76.5 | 74.8 | **76.9** |
| TravelMode choice | absolute + magnitude | 68.7 | 96.3 | 84.0 | 77.5 | 70.5 | **100** ‡ |

Applied datasets (infert, Parkinson's, SUSHI, TravelMode): 10-fold CV.
† Translator/synthetic use the original per-experiment train/test protocol
(averaged), not CV — not strictly comparable cell-to-cell with the applied
rows, but the qualitative ranking holds.
‡ **Caveat (group leakage):** plain 10-fold CV on the combined pair-instances
lets pairs from the same subject/user fall in both train and test, which
inflates memorisation-friendly methods (notably LambdaMART) on Parkinson's and
TravelMode. Group-disjoint held-out evaluation (in the converters' provided
splits) narrows these. The paper should report group-disjoint results.

## What the matrix actually says

**Against C4.5 (the method's natural comparison — it *is* a tree):** PWC4.5
wins wherever the signal is relational — translators (78.3 vs ~62), matched
case–control (77.6 vs 66.1), both XOR constructions (100 vs ~50). This is the
core, robust claim and it is not in doubt.

**Against black-box pairwise/ranking methods (SVM, RankNet, LambdaMART):**
PWC4.5 is *competitive but not dominant* on real mixed-signal data, and wins
outright **only** on genuinely relational structure:
- On the two XOR datasets it wins by ~40 points because the difference-vector
  (SVM) and pointwise-scoring (RankNet, LambdaMART) representations provably
  cannot express an XOR-of-relations — a representational guarantee, not a
  tuning gap.
- On matched case–control and translators it *ties* the best neural method
  (77.6 vs 78.8; 78.3 vs 80.1) while remaining a small readable tree. Here the
  black boxes are universal approximators over the pair representation, so they
  match the structural encoding given enough data — PWC4.5's edge is
  interpretability and small-sample behaviour, not accuracy.
- On absolute-signal data (SUSHI, TravelMode) and temporal-magnitude data
  (Parkinson's) it loses, because it discards exactly the information
  (absolute value, magnitude of difference) those tasks need.

## The reframing test — reported honestly

Hypothesis: the SUSHI loss was task framing (preference = absolute item
quality), and a *comparative* choice task would favour PWC4.5. **Tested and
refuted.** TravelMode choice, though comparative in spirit, is dominated by
absolute attributes (car's wait time is always 0) and cost *magnitude*; PWC4.5
(68.7) lost badly to J48 (96.3) and LambdaMART (100). Consumer-choice data in
general encodes absolute utility, so this domain does not host a relational
PWC4.5 win. We did not cherry-pick until a win appeared; we report the
negative.

## Where PWC4.5 genuinely wins over *all* methods — and how to find it

The unique wins (beating even neural rankers) require the property that broke
the baselines on XOR: **non-transitivity / XOR-of-relations**, which pointwise
rankers cannot represent because s(a) − s(b) is antisymmetric by construction
(no rock–paper–scissors). Real domains that carry it:
- **Non-transitive competition** (ecology: cyclic species dominance A>B>C>A —
  well documented and data exists), **style-matchup sports/games** (cyclic
  head-to-head advantage), RPS-like game logs.
- **Matched designs with interaction/non-monotone risk factors**, where the
  effect of a covariate flips sign depending on another — the biomedical
  analogue of XOR.

These, not "more choice/medical datasets," are the honest path to strict wins
over the neural baselines. For the *library paper* (Paper 1) the C4.5-relative
wins plus the interpretability and the representational guarantee are already a
complete and defensible story; chasing black-box-beating wins on real data is
Paper 2 territory (and is what the temporal + non-transitive relation families
are for).
