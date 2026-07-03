# Matched case–control — replication attempt

**Question:** does the infert result (PWC4.5 beats C4.5 on matched
case–control) replicate on other matched cohorts?

**Answer:** partially, and the *non*-replication is informative — it refines
the scope from "matched case–control" to "matched case–control **with
ordinally-acting risk factors**."

## Availability note

Genuine *individually-matched* case–control datasets are textbook-rare in
public repositories. `infert` (secondary infertility, 1:2 matched) is the
canonical one. The other classic — the Breslow–Day endometrial cancer 1:4
matched study (`bdendo`, mostly categorical exposures) — lives in R's `Epi`
package and is not readily available as CSV without R. So we tested a
*constructed*-matching cohort instead.

## Two matched case–control datasets, 10-fold CV (pair accuracy %)

| Dataset | Matching | Pairs | PWC4.5 | J48 | Pairwise SVM | RankNet | LambdaMART |
|---|---|---:|---:|---:|---:|---:|---:|
| infert (secondary infertility) | designed 1:2 | 165 | **77.6** | 66.1 | 76.9 | 78.8 | 78.2 |
| birthwt (low birth weight) | constructed (age±2, same race) | 58 | 60.3 | 60.3 | **74.7** | 65.0 | 56.7 |

On `infert`, PWC4.5 beats C4.5 by 11.5 points and matches the neural methods.
On `birthwt` it ties C4.5 and *loses to pairwise SVM by 14 points*.

## Why they differ (the refined boundary)

`birthwt`'s discriminative risk factors act partly by **magnitude** — mother's
weight (lwt) and physician visits differ *by how much*, not merely in ordinal
direction — and the pairwise SVM, operating on the difference vector, captures
that magnitude. PWC4.5's ordinal relations discard it, so it cannot exploit the
strongest signal. `infert`'s signal (spontaneous/induced abortion counts within
matched strata) is closer to ordinal, which is why PWC4.5 is competitive there.
The small matched sample (58 pairs after constructed matching) adds noise.

**Refined scope claim:** matching (shared-confounder cancellation) is
*necessary* for PWC4.5's advantage but not *sufficient*. The method wins when
the residual, unmatched signal is carried by the **ordinal within-pair
relation**; it does not when that signal is carried by the **magnitude** of the
difference (then difference-vector methods win, as on Parkinson's and
TravelMode).

## Honest recommendation

Matched case–control is a valid secondary domain but a noisy one for
confirmation: genuine matched datasets are scarce, and the ordinal-vs-magnitude
character of the risk factors varies by cohort. The most robust confirmations of
PWC4.5's findings remain the **ordered-attribution + shared-source** datasets
(French-English Verne corpus; MT/LLM system attribution), where the structure
guarantees the signal is relational. `infert` stays as the representative
matched-case–control example in Paper 1; `birthwt` is worth reporting as the
honest boundary that sharpens the "ordinal, not magnitude" caveat.
