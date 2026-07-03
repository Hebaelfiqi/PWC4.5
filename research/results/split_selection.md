# How the hybrid decides relational vs C4.5 value split — and its impact

**Question (H. El-Fiqi):** at each node, how does the tree choose between the
pairwise relational split and a classic value-threshold split, and what does
that choice do to performance?

## The mechanism (as implemented)

At each node, every candidate split — the relational `min/eq/max`, and (if
enabled) the pair-aggregate value thresholds and magnitude splits — is scored by
**gain ratio on the training data at that node**, and the highest-scoring split
is chosen (greedy). An optional **Occam margin** (`prefer_rel`) lets a
value/magnitude split win only if it beats the best relational gain ratio by a
fixed fraction. A **numeric penalty** option subtracts `log2(#thresholds)/N`
from a value-split's gain (C4.5's correction for multi-cutpoint attributes).

## The impact — the selection rule is the whole ballgame

We instrumented the trees to report the **% of internal nodes that chose a
value-split** alongside 5-fold CV accuracy (max-aggregate value-split):

| dataset | signal | sign-only | gr-only (acc / %val) | +Occam 30% | +numeric penalty |
|---|---|---:|---:|---:|---:|
| TravelMode | absolute | 71.1 | 94.6 / 76% | 94.8 / 76% | 95.1 / 71% |
| SUSHI | absolute | 74.2 | 76.4 / 33% | 76.4 / 28% | 76.4 / 24% |
| mantis | relational | 61.9 | 45.2 / 91% | 51.0 / 88% | **61.9 / 12%** |
| infert | relational | 77.6 | 73.3 / 88% | 74.5 / 87% | 72.1 / 82% |
| Twins | relational | 62.4 | 55.4 / 97% | 55.4 / 97% | **55.4 / 97%** |
| Pokémon | relational | 95.4 | 95.4 / 45% | 95.4 / 27% | 95.4 / 33% |

### Finding 1 — gain-ratio-only chronically over-selects value-splits
On purely-relational data the tree picks a value-split at **88–97%** of nodes
(Twins 97%, mantis 91%, infert 88%) even though the true signal is relational.
Cause: a value-threshold searches *many* cut-points while the relation has *one*
fixed 3-way partition, so a value-split can almost always find a higher
*training* gain ratio — a structural advantage unrelated to generalisation.
Result: relational datasets overfit (Twins 62→55, mantis 62→45).

### Finding 2 — the Occam margin is too weak
Even at +30%, the value-split share barely moves (Twins 97%→97%) and accuracy
does not recover. A percentage margin cannot overcome the large, structural
gain-ratio advantage of multi-cutpoint splits.

### Finding 3 — C4.5's numeric penalty fixes *small*-data over-selection only
The penalty collapses value-split usage where N is small (mantis 91%→12%) and
*fully restores* accuracy (45→62) while preserving the legitimate
absolute-signal splits (TravelMode 95, 71% value). But it **vanishes as N
grows** (`log2(16)/27 ≈ 0.15` on mantis vs `≈ 0.0025` on Twins' 1,600/fold), so
Twins stays at 97% value and overfit.

## Conclusion — the selection rule, not the splits, determines performance

The hybrid's success hinges entirely on *not* over-selecting value-splits. The
proper selection needs the full C4.5 machinery, of which the prototype has only
part:

1. **Numeric penalty** — cancels the multi-cutpoint advantage (fixes small N). ✓ tested
2. **Pessimistic pruning** — removes overfit value-split subtrees by error
   estimate, **independent of N** (would fix Twins). ✗ absent in prototype
3. (optional) **held-out / validation split selection** — choose relational vs
   value by generalisation, not training gain.

The two mechanisms that the WEKA classifier already has (numeric penalty +
pessimistic pruning) are exactly the two that target the two failure modes
(small-N and large-N over-selection). This is strong justification for
evaluating the hybrid in the WEKA classifier rather than the unpruned prototype:
the prototype's relational overfitting is a *selection-rule artifact*, not an
intrinsic flaw of the value-split idea, and the standard C4.5 machinery is
designed precisely to remove it.
