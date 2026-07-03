# Four designs of the hybrid value-split "move-together" rule

Experiment (H. El-Fiqi): when a value-threshold split routes a whole pair to
one branch, which single number represents the pair? Four designs — always
**min**, always **mean**, always **max**, or **fit any** of the three per node
by gain ratio. 5-fold CV, min_leaf=15, no Occam bias (raw effect of each).

| dataset | signal | sign | D1 min | D2 mean | D3 max | D4 any |
|---|---|---:|---:|---:|---:|---:|
| TravelMode | absolute | 71.1 | 75.6 | 78.9 | **94.6** | 93.2 |
| SUSHI | absolute | 74.2 | 76.4 | 76.4 | 76.4 | 76.4 |
| Twins | relational | 62.4 | 57.5 | 51.0 | 55.4 | 50.9 |
| infert | relational | 77.6 | 68.5 | 69.1 | 73.3 | 69.1 |
| mantis shrimp | relational | 61.9 | 59.0 | 50.5 | 45.2 | 59.0 |
| Pokémon | relational | 95.4 | 95.1 | 95.1 | 95.4 | 95.2 |
| HC3 | magnitude | 91.5 | 91.1 | 91.2 | 90.8 | 89.8 |
| Parkinson's | magnitude | 50.0 | 51.1 | 51.6 | 51.5 | 51.2 |

(All results include the pairwise `min/eq/max` relational split too; the
columns vary only the value-split aggregate.)

## Findings

1. **MAX is the best move-together rule for absolute-signal tasks.** On
   TravelMode, thresholding the *larger* member of the pair reaches 94.6% —
   vs 78.9 (mean), 75.6 (min), 71.1 (relation only). SUSHI improves equally
   under all aggregates (+2). So for the absolute regime the answer is: route
   the pair by max(pair) ≤ θ.

2. **"Fit any" (D4) is NOT the winner — it overfits.** Free per-node choice of
   all three aggregates is *worse* than the best fixed aggregate (TravelMode
   94.6 max vs 93.2 any). The extra capacity fits training noise; committing to
   one aggregate generalises better. More flexibility hurt here.

3. **The best aggregate is regime-dependent and can be extreme.** MAX is best on
   TravelMode (94.6) but *worst* on small relational data (mantis 45.2 vs
   relation's 61.9). No single fixed aggregate is universally safe.

4. **All value-splits still overfit small relational datasets** (twins, infert,
   mantis) in this unpruned prototype — consistent with earlier runs; the fix
   is C4.5's numeric-split penalty + pessimistic pruning, absent here.

## Design implication

- The move-together rule that matters for the target (absolute signal) is
  **max**, not min/mean, and not free per-node choice.
- Per-node "fit any" is the wrong knob — it adds capacity that overfits. Better
  options to carry into the WEKA port: (a) make the aggregate a **global
  hyperparameter** (default max) rather than a per-node choice, or (b) keep the
  per-node choice but pay for it with C4.5's numeric-attribute penalty +
  pruning, which this prototype lacks.
- Relational datasets need the relation to dominate; the value-split must be
  gated (Occam margin / pruning) so it is used only where absolute signal is
  genuinely present (TravelMode, SUSHI), not on relational data.

## Next

Port to the WEKA classifier with (i) the **max** aggregate as the default
value-split, (ii) full midpoint search + C4.5 numeric penalty, (iii)
pessimistic pruning, then re-run. Expected: TravelMode/SUSHI gains retained,
relational wins preserved (pruning removes the overfit value-splits).
