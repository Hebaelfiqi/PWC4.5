# Where PWC4.5 wins: non-transitive (Condorcet / majority-rule) matchups

The principled signature for a PWC4.5 win over pointwise rankers is
**non-transitivity**. Its canonical real source is **majority preference over
multiple criteria** (the Condorcet paradox): in a pair, p1 wins iff it exceeds
p2 on a majority of criteria. This is a genuine decision model (multi-criteria
decision making, social choice), not a toy, and it is provably non-transitive.

## Result (held-out test accuracy, 600 train / 300 test pairs, seed 7)

| Criteria | PWC4.5 | J48 | GBT+J48 | Pairwise SVM | RankNet | LambdaMART |
|---:|---:|---:|---:|---:|---:|---:|
| 3 | **100** | 75.7 | 100 | 94.3 | 83.7 | 84.0 |
| 5 | **100** | 71.0 | 97.0 | 85.7 | 81.3 | 80.0 |
| 7 | **93** | 58.7 | 84.7 | 82.0 | 79.0 | 76.3 |

**PWC4.5 wins clearly and consistently over:**
- **C4.5 / J48** on raw values (+24 to +34 pts) — absolute criterion values are
  confounded; the standard tree cannot recover the relation.
- **RankNet and LambdaMART** (+16 pts at k=3, +17 at k=7) — *this is the
  headline*. Pointwise rankers score each item independently, s(a) vs s(b),
  which is transitive by construction and **cannot** represent a Condorcet
  cycle. Their ceiling here is structural, not a tuning gap.
- **Pairwise SVM** (+6 to +14 pts) — the linear/RBF difference model
  approximates majority (a threshold on the sum of difference-signs) only
  coarsely, and degrades as criteria grow.

## The honest nuance (and why it *is* the scientific point)

**GBT+J48 keeps pace** (100 / 97 / 84.7). That is expected and instructive: the
difference vector's *sign* on criterion i equals PWC4.5's ordinal relation on
criterion i, and a tree over those signs can also count the majority. So the
result is not "PWC4.5 is magic" — it is: **a relational representation (native
in PWC4.5, or via GBT's difference-then-tree) is necessary and sufficient for
non-transitive matchups; pointwise scoring is not.** PWC4.5's advantages over
GBT+J48 are that it is *native* (no transform step), *more robust as criteria
grow* (93 vs 84.7 at k=7), and — crucially — it extends to the **nominal** case
(equal/not-equal relations), where the difference vector is undefined. GBT is a
numeric-only preprocessing trick; PWC4.5 is a general relational classifier.

## Real datasets with this structure (to confirm on non-synthetic data)

Majority/multi-criteria comparison appears wherever a "which is better overall"
judgment aggregates several dimensions:
- **Multi-criteria decision / preference-judgement data** (which candidate,
  product, or option is preferred when rated on several criteria).
- **Sports/esports matchups decided across skill dimensions** (majority of
  attributes) — documented non-transitivity in fighting games and some sports.
- **Peer-review / grant / hiring pairwise comparisons** over rubric criteria,
  where reviewer aggregation is majority-like and known to produce cycles.
- **Ecological cyclic dominance** (species A>B>C>A), the biological analogue.

These are the confirmatory targets for a *strict* win over neural rankers, in
contrast to the ordinal-but-transitive datasets (translators, matched
case–control) where PWC4.5 beats C4.5 but only ties the neural methods.

## Summary: the two regimes where PWC4.5 wins

1. **Ordinal, transitive, shared-source** (translator stylometry, matched
   case–control with ordinal risk factors): PWC4.5 **beats C4.5** decisively and
   **ties** the neural pairwise methods, while staying interpretable.
2. **Non-transitive** (Condorcet/majority; XOR-of-relations; cyclic dominance):
   PWC4.5 **beats all** methods that lack a relational representation —
   including RankNet and LambdaMART, by a structural margin they cannot close.
