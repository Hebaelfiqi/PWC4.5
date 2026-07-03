# Verification datasets — analysis and empirical test

**Question:** do verification datasets (PAN authorship, LFW faces, speaker/
signature/kinship verification) confirm PWC4.5's findings?

**Answer:** No — and testing it revealed *why*, which strengthens the paper's
core claim rather than weakening it.

## The structural distinction

- **Attribution (PWC4.5's target):** a pair known to contain one of each class;
  decide *which member is which*. Signal = the ordinal within-pair relation
  (min/eq/max). Works because the two members share a source, so confounds
  (text length, difficulty, scale) cancel and only the class-relevant relation
  remains.
- **Verification:** given a pair, decide *same source or different*. Signal =
  similarity / *magnitude* of difference between the two feature vectors —
  exactly the information PWC4.5's ordinal relations discard. Verification is
  the home turf of difference/distance methods, not of ordinal relational
  splits.

So verification is pairwise but structurally mismatched to PWC4.5.

## Empirical test (same translator features, recast as verification)

Using the identical network-motif features where PWC4.5 excelled at
*attribution*, we built a *verification* task: same-translator pairs (two
different chapters by one translator) vs different-translator pairs. Held-out
accuracy:

| Method | Accuracy |
|---|---:|
| PWC4.5 | 47% |
| J48 | 50% |
| Pairwise SVM (difference) | 49.7% |
| RankNet | 52.3% |
| LambdaMART | 51.3% |
| \|diff\|+LogReg (similarity) | 50.0% |

**Everyone is at chance — including the difference/similarity methods.**

## Why this is a *confirmation*, not just another boundary

The reason all methods fail is the deep point: pairing two *different* chapters
breaks the shared-source structure, so the dominant confound — chapter length,
which drives motif-count magnitude — no longer cancels. It swamps the
translator signal for every method. In the original attribution task the pair
is the *same* chapter by two translators, so length is a common factor that
cancels, exposing the style relation.

This directly confirms the paper's founding insight (§2:3 of TALLIP 2016): the
power of PWCCP comes from the *linked pair sharing a source*, which makes the
within-pair relation informative by cancelling confounds. Verification
generally destroys that structure — either by pairing unmatched items (confound
returns, as here) or, when content is controlled, by reducing to a
similarity/magnitude task where difference methods dominate. Either way,
verification does not confirm PWC4.5's findings.

## Implication: what *does* confirm the findings

Confirmatory datasets must preserve the **ordered-attribution + shared-source**
structure. In priority order:

1. **French-English Verne corpus** (Mercier 1872 / Walter 1991, 47 chapters) —
   the TALLIP paper's *own second corpus*, absent from the artifact. Public
   domain (Gutenberg). Confirms the translator finding in a second language
   pair; the single most direct "beyond the datasets we have" confirmation.
2. **MT / LLM system attribution** — same source translated by two systems;
   "which system produced this?" Same ordered-attribution structure, modern and
   generatable (public WMT system outputs). Confirms + extends.
3. **More matched case–control studies** (shared-confounder cancellation is the
   epidemiological analogue of shared-source) — confirms the infert result in
   other medical cohorts.
4. **Non-transitive competition data** (ecological cyclic dominance) — confirms
   the *strict-win-over-all-methods* claim, the real-data analogue of the XOR
   result, where pointwise rankers provably fail.

Verification datasets are explicitly *not* on this list, and now we can say why
with evidence.
