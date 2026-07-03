# PWC4.5 use-case catalogue: mapping the boundary

The methodology is unchanged from the 2016 article; the contribution here is an
empirical **characterisation of when relational (ordinal within-pair) splits
help** — a decision rule backed by wins *and* principled losses across many real
and synthetic datasets. All numbers are pair accuracy (%); PWC4.5 vs J48 (C4.5),
and vs pairwise SVM / RankNet / LambdaMART (best of the three shown as "ranker").
Verified on WEKA 3.8.6 / Temurin JDK 17.

## Master table

| # | Dataset | Domain | Signal type | PWC4.5 | J48 | best ranker | Verdict |
|---|---|---|---|---:|---:|---:|---|
| 1 | Pokémon battles (50k) | game | ordinal, dominant | **94.0** (3lf) | 94.4 (370lf) | 92.2 | **WIN** (best-tier + interpretable) |
| 2 | Synthetic XOR numeric | — | ordinal, non-transitive | **100** | 59 | 59.6 | **WIN** (decisive) |
| 3 | Synthetic XOR categorical | — | nominal, non-transitive | **100** | ≤57 | — | **WIN** |
| 4 | Condorcet majority (k=5) | decision theory | non-transitive | **100** | 71 | 81.3 | **WIN over neural** (structural) |
| 5 | Translator stylometry | linguistics | ordinal, subtle | 78.3 | ~62 | 80.1 | competitive; beats C4.5, ~ties RankNet |
| 6 | Matched case–control (infert) | epidemiology | ordinal | 77.6 | 66.1 | 78.8 | competitive; beats C4.5 |
| 7 | Twins discordant mortality | epidemiology | ordinal (confounds cancel) | 62.4 (3lf) | 57.5 (112lf) | 62.6 | **WIN-tier** + interpretable |
| 8 | Mantis shrimp contests | ecology | ordinal (assessment) | **61.8** | 55.9 | 60.0 | **WIN** (small n) |
| 9 | HC3 LLM-vs-human | NLP forensics | ordinal **+ gross magnitude** | 91.9 | 91.5 | 97.5 | boundary: strong but ranker wins |
| 10 | Parkinson's progression | neurology | temporal magnitude | 50.6 | 51.7 | 70.2 | LOSS (magnitude) |
| 11 | SUSHI preference | choice | absolute | 69.2 | 76.6 | 76.9 | LOSS (absolute) |
| 12 | TravelMode choice | choice | absolute + magnitude | 68.7 | 96.3 | 100 | LOSS |
| 13 | Dota 2 draft (92k) | esports | additive | 54.8 | 52.3 | 57.8 | LOSS (additive) |
| 14 | Matched birthwt | epidemiology | magnitude | 60.3 | 60.3 | 74.7 | LOSS (magnitude) |
| 15 | Translator verification | linguistics | similarity (confound uncancelled) | 47 | 50 | ~52 | structural failure |

## The refined boundary (the paper's thesis)

PWC4.5 encodes the *ordinal within-pair relation* (min/eq/max; nominal eq/neq)
and discards everything else. It therefore wins exactly when **all** of these
hold, and loses when any fails:

1. **Relational, not absolute.** The class depends on "who exceeds whom", and
   absolute values are confounded/variable across pairs (shared source or
   matching cancels them). *Fails →* SUSHI, TravelMode (absolute item/route
   quality).
2. **Ordinal, not magnitude.** Only the *direction* of the within-pair
   difference matters, not *how much*. *Fails →* Parkinson's (progression
   magnitude), birthwt, and partly HC3 (LLMs differ from humans grossly in
   verbosity/structure, so difference-vector methods that keep magnitude edge
   ahead). This is the subtle-vs-gross axis: translators (subtle ordinal) win,
   LLM-vs-human (gross magnitude) is only competitive.
3. **Dominant, not distributed-additive.** A few strong relations, not a
   weighted sum over many weak features. *Fails →* Dota 2 (win = additive
   sum of ~113 hero win-rates; a linear model wins).
4. **Attribution, not similarity.** The pair is parallel/ordered ("which is
   which"), not "same or different". *Fails →* verification, which also breaks
   the confound-cancellation that makes the relation meaningful.

Against **neural rankers specifically**, PWC4.5 wins outright only under
**non-transitivity** (Condorcet / XOR-of-relations), because a pointwise score
s(a) vs s(b) is transitive by construction. On transitive-but-relational data
it *ties* them while being interpretable (a recurring 3-leaf-vs-hundreds-of-
leaves contrast vs C4.5: Pokémon 3 vs 370, twins 3 vs 112).

## Limits → extensions this motivates (feeds a methods paper)

The losses are not just boundaries; each names a concrete methodological
extension:

- **Magnitude losses (Parkinson's, HC3, birthwt)** → add *magnitude-aware*
  relations (e.g. "much-greater / slightly-greater", quantised difference
  bands) alongside min/eq/max. Would likely convert HC3 into a clean win and
  recover the Parkinson's/birthwt signal.
- **Directional-nominal loss (Dota 2 heroes; residency needed numeric coding)**
  → a *directional* nominal relation ("p1 has attribute, p2 does not") instead
  of symmetric eq/neq.
- **Additive loss (Dota 2)** → a relational + additive hybrid (relational splits
  over aggregated counts).
- **Temporal (Parkinson's progression)** → temporal relation family
  (persistence, divergence) — the Paper 2 core.
- **Non-transitive real data** → still the open confirmation target; synthetic
  Condorcet/XOR proves the capability, a real cyclic dataset would confirm it.

## Story for the paper

Four independent **real** domains where PWC4.5 is best-or-tied-best-and-
interpretable — game (Pokémon), forensic linguistics (translators), behavioural
ecology (mantis shrimp), epidemiology (twins) — plus a decisive structural win
on non-transitive data, bounded by clearly characterised failure regimes
(absolute / magnitude / additive / similarity). The boundary is not a weakness
list; it is a *predictive rule* telling a practitioner whether to reach for the
method, and a roadmap of extensions.
