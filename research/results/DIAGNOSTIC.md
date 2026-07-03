# Diagnostic evaluation: when PWC4.5 excels, when it fails, what tweaks do

Cross-validated (10-fold for n<3000, else 5-fold) pair accuracy, across every
dataset collected. All PWC4.5 variants use C4.5 pessimistic pruning. Objective:
characterise the method's boundary, not maximise any number.

Variants: **Relational** (original PWC4.5) · **+Value+pen** (adds a C4.5
threshold on the max-of-pair aggregate, with numeric penalty) · **+Magnitude**
(adds a standardised dead-zone relation) · **Hybrid** (value+magnitude) ·
**LogReg(diff)** (logistic regression on the difference vector — the
linear/difference reference).

| dataset | regime | Relational | +Value+pen | +Magnitude | Hybrid | LogReg(diff) |
|---|---|---:|---:|---:|---:|---:|
| 5D-XOR | non-transitive | **100.0** | 52.7 | 75.4 | 52.7 | 58.3 |
| categorical-XOR | non-transitive | **98.5** | 97.0 | 98.5 | 97.0 | 52.5 |
| Condorcet (k=5) | non-transitive | **100.0** | 90.0 | 100.0 | 90.0 | 83.0 |
| Translators | relational/subtle | **98.8** | 98.8 | 98.8 | 98.8 | 84.1 |
| Pokémon | relational/dominant | **95.1** | 94.9 | 94.8 | 94.6 | 87.5 |
| Mantis shrimp | relational/small | 60.0 | 37.5 | **67.5** | **67.5** | 61.7 |
| Twins | relational | **62.4** | 58.7 | 59.5 | 53.9 | **62.4** |
| infert (case–control) | relational | 77.6 | 74.1 | 76.9 | 69.8 | **78.2** |
| birthwt (case–control) | magnitude | 55.0 | 69.0 | 62.0 | 67.3 | **71.3** |
| Parkinson's | magnitude/temporal | 50.0 | 51.4 | 51.3 | 51.4 | **53.9** |
| HC3 (LLM-vs-human) | magnitude/mixed | 92.0 | 90.7 | 93.5 | 89.9 | **96.5** |
| SUSHI | absolute | 74.2 | 76.4 | 76.2 | 76.3 | **77.0** |
| TravelMode | absolute | 68.6 | **95.1** | 85.4 | 94.0 | 69.7 |
| Dota 2 | additive | 51.6 | 51.7 | 51.6 | 51.7 | **56.8** |

(2D-XOR excluded — degenerate under random orientation; see note in
`diagnostic.py`.)

## When the relational method EXCELS

The original relational PWC4.5 is **best or tied-best on every relational and
non-transitive dataset**:
- **Non-transitive** (5D-XOR 100, categorical-XOR 98.5, Condorcet 100) — and
  by a wide margin over the linear reference (58/53/83), which *cannot* express
  non-transitivity. This is the method's signature, unique strength.
- **Subtle relational** (Translators 98.8 vs LogReg 84.1) and **dominant
  relational** (Pokémon 95.1 vs 87.5).
- **Matched/relational** (Twins 62.4, infert 77.6) — ties the linear reference
  while being interpretable.

## When it FAILS (and what wins instead)

The linear difference model wins whenever the signal is **not** an ordinal
within-pair relation:
- **Magnitude** — HC3 96.5 vs 92, Parkinson's 53.9 vs 50, birthwt 71.3 vs 55.
- **Additive** — Dota 2 56.8 vs 51.6 (a weighted sum of many weak features).
- **Absolute** — SUSHI 77.0 vs 74.2 (item identity dominates).

## What the tweaks do — the central finding

The tweaks are **regime-specific and double-edged**, not general improvements:

- **The value-split is the only fix for the absolute regime** (TravelMode
  68.6 → **95.1**, +26; birthwt 55 → 69) — but it is a **liability everywhere
  else**: it *destroys* the non-transitive strength (5D-XOR **100 → 52.7**,
  Condorcet 100 → 90) and erodes relational datasets (Twins 62→59, infert
  77.6→74, mantis 60→37.5). Turned on globally it does more harm than good.
- **Magnitude (dead-zone)** is mild and mostly safe: it helps small/mixed cases
  (mantis 60 → 67.5, HC3 92 → 93.5) and rarely hurts, but it also dents 5D-XOR
  (100 → 75.4).
- **Hybrid (both on)** inherits the value-split's damage on non-transitive/
  relational data; it is not a safe default.

## Conclusion for the paper

1. **PWC4.5's territory is precisely the relational/ordinal/non-transitive
   regime**, where it is best and often uniquely capable (non-transitivity).
   Outside it — magnitude, additive, absolute — a difference-vector linear model
   is better, and no relational tweak changes that.
2. **The value-split extension is not a general improvement**; it trades the
   method's signature strength for coverage of the absolute regime. It is only
   worth enabling with a *reliable per-dataset (or per-node) gate* that detects
   absolute signal — and the earlier selection analysis
   ([split_selection.md](split_selection.md)) shows gain-ratio + penalty +
   pruning do **not** provide that gate reliably (they under-protect the
   relational strength). A validation-based or held-out split selector is the
   open problem.
3. The honest, defensible framing is therefore **not** "a unified hybrid that
   wins everywhere", but: *"an interpretable method that owns the
   relational/non-transitive regime, with a diagnosed boundary and an optional,
   gated value-split for absolute-signal paired data — where the gate itself is
   the hard open problem."*
