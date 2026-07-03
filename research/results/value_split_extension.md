# Hybrid splits: pairwise relation + pair-aggregate value threshold

**The idea (proposed by H. El-Fiqi):** keep PWC4.5's `min/eq/max` relational
split, but *also* let the tree do a C4.5-style **absolute value threshold** —
with the catch that a naive threshold on one member sends p1 and p2 to different
branches and destroys the pair. **Fix:** threshold a *symmetric function of the
whole pair* — min, max, or mean of the two members' values — so the pair moves
together and orientation is irrelevant. This adds the absolute-value information
the pure relation discards, while keeping pairs intact.

This is distinct from (and better-motivated than) magnitude: it targets the
**absolute-signal** losses, not the magnitude ones.

## Result — it fixes the absolute-signal losses (5-fold CV)

| Dataset | signal | sign (original) | + value-split | Δ |
|---|---|---:|---:|---:|
| **TravelMode** | absolute | 71.1 | **93–95** | **+22 to +24** |
| **SUSHI** | absolute | 74.2 | **76.4** | +2.2 |
| Parkinson's | magnitude | 50.0 | 51.2 | +1.2 |
| Pokémon | relational | 95.4 | 95.2 | −0.2 (preserved) |
| HC3 | mixed | 91.5 | 89.8 | −1.7 |
| Twins | relational | 62.4 | 50.9 | −11.5 |
| infert | relational | 77.6 | 69.7 | −7.9 |
| mantis shrimp | relational | 61.9 | 59.0 | −2.9 |

**TravelMode goes from a clear loss (71%) to near the best method (95%)** — the
`min(wait) ≤ θ` aggregate captures "one mode has near-zero wait time" (absolute,
class-defining) while keeping the pair together. SUSHI (absolute item quality)
also improves. This is exactly the regime pure PWC4.5 could not handle.

## The catch — and why it is fixable

On purely-relational datasets (twins, infert, mantis shrimp) the value-split
**overfits** in this prototype and hurts. An Occam margin (use a value-split
only if its gain ratio beats the best relational split by >10%) did **not** fix
it, because the overfit value-splits genuinely have higher *training* gain
ratio — gain ratio cannot distinguish signal from overfitting.

The missing ingredient is **pruning**. This reference Python prototype has no
error-based pruning; the production WEKA classifier does (C4.5 pessimistic
subtree replacement). A value-split that fits training noise would be pruned
back, while a value-split that captures real absolute structure (TravelMode)
would survive. So the definitive evaluation requires porting the hybrid split
into the WEKA classifier and letting pruning arbitrate.

## Why this is the strongest methodological direction so far

It turns the boundary map from "PWC4.5 wins here, loses there" into a **unified
method**: at each node the tree chooses among
- **pairwise relation** `min/eq/max` (original) — for relational/ordinal signal,
- **pair-aggregate value threshold** `f(min,max,mean) ≤ θ` (new) — for
  absolute signal, keeping the pair together,
- (optionally) the **magnitude dead-zone** relation — for magnitude signal,

selecting whichever maximises gain ratio, with pruning preventing overfitting.
This directly addresses the **absolute-signal** failure regime (SUSHI,
TravelMode) that neither magnitude relations nor the original method could,
and it does so with the interpretability and pair-integrity that define the
method. It is a genuine, publishable methodological contribution — the increment
that lifts the work from "use-case catalogue" to "method + evaluation".

## Next step

Port the pair-aggregate value-threshold split (and, secondarily, the dead-zone
magnitude relation) into `weka.classifiers.trees.PWC45`, gated by the existing
pessimistic pruning, and re-run the full CV. Expected: keeps the relational wins
(pruning removes the overfit value-splits) *and* the absolute-signal gains
(TravelMode, SUSHI), yielding a single method that covers relational + absolute
paired classification.
