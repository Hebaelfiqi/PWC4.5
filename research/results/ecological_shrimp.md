# Real ecological data: mantis shrimp contests

**Dataset:** Green & Patek, "Contests with deadly weapons: telson sparring in
mantis shrimp (*Neogonodactylus bredini*)", Zenodo record 4995116 (open access).
34 staged dyadic contests over burrow ownership; each contestant has
pre-contest morphology (body length, body mass, maximum strike force) and a
residency status (resident/intruder). Real behavioural-ecology data.

**Task:** given two contestants' trait vectors, predict the winner. Residency
is encoded numerically (resident=1, intruder=0) so relations preserve
direction; "number of strikes delivered" is excluded as a contest outcome
(leakage). Pairs randomly oriented.

## Result (10-fold CV, 34 contests)

| Method | Accuracy | Model size |
|---|---:|---:|
| **PWC4.5** | **61.8%** | 3 leaves |
| RankNet | 60.0% | — |
| LambdaMART | 56.7% | — |
| J48 (C4.5) | 55.9% | 2 leaves |
| Pairwise SVM | 53.3% | — |

PWC4.5 is the top method and beats C4.5 and the difference/pointwise methods by
6–8 points, with a compact relational tree:

```
R(body_mass) = max : P2 wins
R(body_mass) = min : P1 wins    (the relative-mass comparison predicts the winner)
```

## Honest caveats

- **Small sample (34 contests).** The margin over RankNet (61.8 vs 60.0) is one
  contest — within noise. The robust statement is PWC4.5 ≥ all methods and
  clearly > J48/SVM; it is *suggestive*, not definitive.
- **Modest absolute accuracy.** Mantis shrimp use *mutual assessment* (the
  contest is settled by relative, not absolute, fighting ability), so outcomes
  are inherently hard to predict from morphology alone — which is itself why a
  *relational* method fits: the biology is explicitly about the within-pair
  comparison.
- **Relation-dominated but not cyclic.** This is an assessment/residency
  contest (roughly transitive), so it demonstrates the "beats C4.5 + tops the
  neural methods on relational data" regime, not the non-transitive regime
  (that remains the synthetic Condorcet/XOR result; a real *cyclic* dataset —
  e.g. side-blotched lizard morphs — is still the open target).

## Significance

Even with 34 contests, a real behavioural-ecology dataset lands PWC4.5 on top,
and the mechanism is exactly the paper's thesis: mantis shrimp contests are
decided by *relative* fighting ability (mutual assessment), so the within-pair
relation is the right representation. This adds a third real domain — after
game (Pokémon) and forensic linguistics (translators) — to the evidence that
PWC4.5 wins when outcomes are relation-dominated.

## Reproduce

```sh
# open download from Zenodo record 4995116
python research/tools/convert_shrimp.py Green_Patek_SuppData_Contests.csv -o d10
java --add-opens java.base/java.lang=ALL-UNNAMED -cp weka.jar:pwc45.jar \
     weka.classifiers.trees.PWC45 -x 10 -t d10/shrimp_all.arff
```
