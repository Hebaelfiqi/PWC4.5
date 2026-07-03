# Can PWC4.5 be improved to consider magnitude? Design + empirical answer

**Short answer:** yes, cleanly and cheaply — and it *works when the signal is
single-feature magnitude* — but it does **not** close the losses on the real
magnitude datasets (HC3, Parkinson's), because those losses are not about
single-feature magnitude at all; they need a **joint linear combination** of
many feature-differences, which is the tree-vs-linear divide, orthogonal to the
relational idea. This is a sharper, more honest boundary result than "we patched
it."

## The design (implemented in `research/baselines/pwc45_py.py`)

The numeric relation `min/eq/max` is the *sign* of d = V(p1)−V(p2). To add
magnitude, at each node and per attribute we also evaluate a **threshold split
on the difference**, chosen by gain ratio (like C4.5 chooses value thresholds):

- **relative (scale-invariant):** δ = (a−b)/(|a|+|b|+ε) ∈ [−1,1]; `min/eq/max` =
  sign(δ). Preserves scale-invariance; adds "much vs slightly greater".
- **raw:** threshold on (a−b). Captures absolute magnitude but sacrifices
  scale-invariance.

The node keeps whichever of {sign 3-way, δ-threshold} maximises gain ratio, so
magnitude is used **only when it helps** ("if necessary" is automatic).

## Control — the mechanism is correct

Synthetic task where the label *is* single-feature magnitude
(class 1 iff |a₀−b₀| > 0.5; sign is uninformative):

| | sign-only | + magnitude |
|---|---:|---:|
| accuracy | 74.5% | **99.7%** |

The magnitude relation recovers the signal perfectly. Implementation validated.

## But it does not fix the real magnitude losses

HC3 (LLM-vs-human), held-out, across regularisation strengths:

| min_leaf | sign-only | + raw-magnitude |
|---:|---:|---:|
| 10 | 92.0 | 91.1 |
| 30 | 91.5 | 91.2 |
| 60 | 91.7 | 91.2 |
| 120 | 91.2 | 91.2 |
| **Logistic on the FULL difference vector (joint)** | | **96.8** |

Magnitude splits neither help nor (with regularisation) hurt — they plateau at
~91–92%. The method that actually wins HC3, **logistic regression on the whole
difference vector (96.8%)**, wins by **jointly, linearly combining all feature
differences** — not by any single-feature magnitude threshold. The same held on
Parkinson's and twins (magnitude splits overfit small data; sign-only is best).

## The real diagnosis (and why it matters)

The magnitude losses are not "PWC4.5 ignores magnitude". They are the classic
**greedy axis-parallel tree vs. joint linear model** gap: HC3's human-vs-LLM
signal is spread across many stylometric dimensions, each contributing a little,
best aggregated by a weighted sum. Greedy relational *or* magnitude splits pick
one attribute at a time and cannot represent that sum. Adding magnitude
relations does not change this, because the missing ingredient is *joint
combination*, not *per-feature magnitude*.

## Two extension paths — and the recommendation

1. **Magnitude-aware relations (this doc).** Small, clean, gain-ratio-gated,
   never hurts when regularised, and genuinely helps the *single-feature-
   magnitude* regime (control above). Worth shipping as an **optional relation
   family** — it strictly enlarges coverage from {ordinal} to {ordinal +
   single-feature magnitude} at near-zero cost. A legitimate, modest
   methodological increment.
2. **Joint / oblique / model-tree extension** (e.g. linear leaf models on
   difference vectors). This is what would close the HC3 gap — but it abandons
   the interpretable, axis-parallel relational tree that is the method's entire
   value proposition, and reinvents difference-vector linear methods that
   already exist. **Not recommended.**

**Strategic conclusion.** Add magnitude relations as an *option* (path 1), but
do **not** try to turn PWC4.5 into a magnitude/additive method. Position it as
owning the **relational / ordinal / non-transitive** regime — where it uniquely
beats even neural rankers (Condorcet/XOR) and matches them interpretably
(Pokémon, twins) — and cede the **joint-magnitude / additive** regime to
difference-vector linear methods. The boundary is principled, and saying so
plainly is a stronger scientific position than an over-reaching patch.
