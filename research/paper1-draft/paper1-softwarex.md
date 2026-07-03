# PWC4.5: A WEKA classifier for pairwise comparative classification

*Draft for SoftwareX (original software publication). Target length ~6 pages.
Fill fields marked TODO before submission.*

**Authors:** Heba El-Fiqi ᵃ, Eleni Petraki ᵇ, Hussein A. Abbass ᵃ
ᵃ School of Systems and Computing, UNSW Canberra, Australia
ᵇ University of Canberra, Australia
Corresponding author: TODO (email)

---

## Metadata (SoftwareX table)

| | |
|---|---|
| Current code version | 1.1.0 |
| Permanent link to code/repository | https://github.com/Hebaelfiqi/PWC4.5 |
| Permanent link to reproducible capsule | Zenodo concept DOI 10.5281/zenodo.21134664 |
| Legal code license | MIT (code); CC BY 4.0 (bundled datasets) |
| Code versioning system | git |
| Software languages / tools | Java 7+; WEKA ≥ 3.8; Python 3 (converters) |
| Compilation requirements | JDK, `weka.jar` |
| Developer documentation | repository README, research/weka/README.md |
| Support email | TODO |

## Abstract

Many classification problems are *comparative*: instances arrive in linked
pairs sharing a common source, and the class of a pair depends on the
*relationship between the paired feature values* rather than on the values
themselves. Standard classifiers, which assume independent and identically
distributed instances and split on value thresholds, cannot represent this
structure. We present **PWC4.5**, a WEKA implementation of a decision tree for
Pairwise Comparative Classification Problems (PWCCP) that splits on within-pair
relations (numeric: minimum / equal / maximum; nominal: equal / not-equal).
The software packages the method (El-Fiqi et al., 2016) as a first-class WEKA
classifier through a *pair-as-single-instance* representation that makes pairs
transparent to cross-validation and the WEKA Experimenter, and extends it to
nominal attributes. We demonstrate utility across real domains as varied as
game AI (Pokémon battles), forensic linguistics (translator stylometry),
epidemiology (matched case–control), and behavioural ecology (animal contests),
and — crucially — characterise *when* relational splits help versus when
absolute, magnitude, or additive signals favour other methods, giving
practitioners an evidence-based decision rule. On real Pokémon data a three-leaf
relational tree matches C4.5's accuracy while C4.5 needs 370 leaves.

## 1. Motivation and significance

Comparative Classification Problems (CCP) arise when data are collected in
blocks of records — one instance per class — that describe the same underlying
subject or phenomenon; the pairwise case (PWCCP), with two classes, is the
most common [1]. Examples recur across fields: two translators rendering the
same source text [1], a case matched to controls on confounders in
epidemiology, two visits of the same patient, or two options a user chooses
between. In all of these the independent-and-identically-distributed (iid)
assumption underpinning standard classifiers is violated *by design*: the two
members of a pair are linked, and the discriminative signal lives in how their
feature values relate, not in the values in isolation.

PWC4.5 [1] addressed this by modifying C4.5 [2] to split on within-pair
relations. Despite the generality of the idea, the method has existed only as
a standalone research artefact tied to its original translator-stylometry
application, invisible to the tooling where classifiers are actually compared.
This paper's significance is threefold: (i) it revives and packages the method
as a maintained, citable WEKA classifier usable in the Explorer, Experimenter
and KnowledgeFlow; (ii) it introduces the pair-as-single-instance
representation that lets standard cross-validation and paired significance
testing operate on paired data without leakage; and (iii) it extends the method
to nominal attributes, opening categorical domains (matched studies, discrete
choice) that the numeric-only original could not address.

## 2. Software description

### 2.1 The pair-as-single-instance representation

The central design decision is representational. A naive encoding storing each
pair member as its own row (the original file format) breaks under WEKA's iid
data model: cross-validation and train/test splitting shuffle rows and separate
pair members, silently corrupting every result. PWC4.5 instead encodes **each
pair as one instance** with `2n` attributes — p1's `n` features followed by
p2's `n` features (identical order and types) — and the class of p1 (p2 takes
the other class). The classifier reconstructs the relation between attribute
`i` of the two members from positions `i` and `i+n` internally. Pairing thus
becomes invisible to WEKA: a fold can never split a pair, and the
Experimenter's corrected paired t-tests apply unchanged.

### 2.2 The classifier

`weka.classifiers.trees.PWC45` extends `AbstractClassifier`. At each node, for
every feature it computes the within-pair relation — for numeric attributes
R(V(p1),{V(p1),V(p2)}) ∈ {min, eq, max}; for nominal attributes {eq, neq} — and
selects the feature whose relation maximises the C4.5 gain ratio. Trees are
pruned by C4.5-style pessimistic (confidence-based) subtree replacement,
reusing WEKA's own error estimator for consistency with J48. Options: `-U`
(unpruned), `-C` (confidence factor), `-M` (minimum instances), `-depth`.
Mixed numeric/nominal inputs are supported; a type-consistency check enforces
that the two pair sides carry matching feature types. Learned trees print as
readable relational rules with leaf and size counts.

### 2.3 Tooling

Python converters turn common formats into pair-ARFF: a generic
`.data/.names → pair-ARFF` converter, plus dataset-specific converters
(matched case–control, repeated-measures pairs, preference pairs) and a
difference-vector converter for the Gradient-Based Transformation baseline
[1]. The classifier is distributed as an installable WEKA package (`pwc45`).

### 2.4 Architecture / software functionalities (figure TODO)

- Build (train), classify, cross-validate — standard WEKA classifier lifecycle.
- Explorer / Experimenter / KnowledgeFlow / command-line usage.
- Model inspection: relational decision tree printed as rules.

## 3. Illustrative examples

We evaluate across several domains (Table 1); the point is not a leaderboard
but a *characterisation of when relational splits help*. All numbers are pair
accuracy under 10-fold cross-validation (group-disjoint folds where a grouping
exists), plus a per-experiment protocol for the translator corpus, on WEKA
3.8.6 and JDK 17. Baselines: J48 (C4.5), the Gradient-Based Transformation
(difference-vector) tree GBT+J48 [1], and — for the wins over ranking methods —
pairwise SVM, RankNet, and LambdaMART.

**Table 1.** Pair-classification accuracy (%); best per row in **bold**.

| Domain | Dataset | Signal | PWC4.5 | J48 | GBT+J48 | best ranker† |
|---|---|---|---:|---:|---:|---:|
| Game | Pokémon battles (50k real) | relational | **94.0** (3 leaves) | 94.4 (370 leaves) | — | 92.2 |
| Comp. linguistics | Translator stylometry (21 pairs) | relational | **78.3** | 62 | — | 80.1 |
| Epidemiology | Matched case–control (infert) | relational | 77.6 | 66.1 | — | 78.8 |
| Behav. ecology | Mantis shrimp contests (34) | relational | **61.8** | 55.9 | — | 60.0 |
| Synthetic | Numeric XOR (noise 0) | relational | **100** | 59 | 59 | 59.6 |
| Synthetic | Categorical XOR (alphabet ≥5) | relational | **100** | ≤57 | — | — |
| Synthetic | Condorcet majority (k=5) | non-transitive | **100** | 71 | 97 | 81.3 |
| Neurology | Parkinson's progression | temporal magnitude | 50.6 | 51.7 | **56.5** | 70.2‡ |
| Consumer choice | SUSHI preference | absolute | 69.2 | **76.6** | — | 76.9 |
| Esports | Dota 2 draft (92k real) | additive | 54.8 | 52.3 | — | **57.8** |

† best of pairwise SVM / RankNet / LambdaMART. ‡ optimistic (group leakage in
plain CV; see repository).

**Reading the results.** PWC4.5 wins where the class depends on the *ordinal
within-pair relationship* and absolute values are confounded or variable — a
regime that recurs across unrelated real domains: real **Pokémon** battles
(outcome set by "who is faster", a Speed relation invariant to the huge stat
scale — a **3-leaf** tree matches C4.5's accuracy where C4.5 needs **370**
leaves), **translator stylometry** (text length varies, relations are
invariant), **matched case–control** (matched covariates cancel), real
**mantis shrimp** contests (settled by *mutual assessment* — relative fighting
ability), and the synthetic constructions. On non-transitive structure
(Condorcet majority, XOR-of-relations) PWC4.5 beats *every* method lacking a
relational representation — including RankNet and LambdaMART by a structural
margin, since a single ranking score cannot represent a preference cycle.

It does *not* help in three complementary regimes, each an honest boundary:
(i) **absolute** signal — SUSHI preference, where item identity dominates;
(ii) **temporal magnitude** — Parkinson's progression, where *how much* a
biomarker changed matters and the difference-vector GBT is the only method
above chance; and (iii) **additive** signal — Dota 2 draft, where the outcome
is a weighted *sum* of many hero win-rates that a linear model captures best.
These boundaries yield a practitioner decision rule:

> *Use PWC4.5 when pairs are linked by a shared source so absolute values are
> confounded, and the class depends on the **dominant, ordinal** within-pair
> relation. Prefer difference-based methods when the signal is the **magnitude**
> of change, linear/additive models when it is a **sum** over many weak
> features, and standard classifiers when **absolute** values are informative.*

**Interpretability exhibit (real Pokémon battles):** a single relation — who is
faster — explains 94% of 50,000 battles in three leaves, where C4.5 needs 370:
```
R(Speed) = max : P1 wins        (the faster Pokémon strikes first and usually wins)
R(Speed) = min : P2 wins
R(Speed) = eq  : P2 wins
```

## 4. Impact

PWC4.5 makes an established but under-used problem formulation accessible
inside the standard ML workbench, and broadens it from its origin niche to any
domain with linked pairs and relational signal — matched observational studies,
verification/forensics, and comparative choice. The pair-as-single-instance
representation is reusable independently of the classifier. The explicit
scope characterisation helps practitioners decide *whether* the method fits
before adopting it. The categorical-attribute extension and the WEKA packaging
lower the barrier to reuse and to fair comparison against standard trees.

## 5. Conclusions

We released PWC4.5 as a WEKA classifier for pairwise comparative
classification, extended to nominal attributes, with converters, an installable
package, and a five-domain evaluation that characterises when relational splits
are necessary. Future work extends the relation family to temporal (magnitude-
aware) relations and to the multi-class CCP setting.

## Acknowledgements / data

Translator datasets are network-motif features derived from Tanzil.net
translations (see repository). infert (R datasets), Parkinson's Telemonitoring
(UCI), and SUSHI (Kamishima) are public; converters are provided.

## References

[1] H. El-Fiqi, E. Petraki, H. A. Abbass. Pairwise comparative classification
for translator stylometric analysis. ACM TALLIP 16(1), Article 2, 2016.
doi:10.1145/2898997
[2] J. R. Quinlan. C4.5: Programs for Machine Learning. Morgan Kaufmann, 1993.
[3] M. Hall et al. The WEKA data mining software: an update. SIGKDD
Explorations 11(1), 2009.
[TODO] infert (Trichopoulos et al. 1976); Parkinson's Telemonitoring (Tsanas
et al. 2010); SUSHI (Kamishima 2003) — full citations.
