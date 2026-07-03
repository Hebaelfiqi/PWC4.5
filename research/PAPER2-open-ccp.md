# Paper 2 — Open Comparative Classification: interpretable discovery of behavioral groupings from co-occurring agents

**Type:** methods paper, Q1 journal
**Status:** planning
**Objective (one line):** define and solve the *unsupervised* extension of
the CCP problem class — discovering behavioral groupings from agents observed
once, side by side, under partially shared perception, with no labels — via
unsupervised relational split trees, validated with a ground-truth
heterogeneous-swarm generator and demonstrated on real collective-behavior
data.

---

## 1. Context and vision

The 2016 TALLIP paper defined CCP/PWCCP for the **supervised, exact-pair**
setting: pair members linked by an identical source, class labels known.
Real multi-agent fields invert every assumption: a *group* runs the scenario
*once*; co-moving agents share only part of their perception (context overlap
< 100%); and the behavioral groupings are unknown — they are the thing to be
discovered. This paper closes that gap with a ladder:

> PWCCP (supervised, m=2, exact pairs, 2016) → CCP (supervised, m known) →
> **Open CCP (this paper): m unknown, labels absent, pairs constructed.**

The founding invariance insight carries over in graded form: within-pair
relations cancel the *shared* portion of contextual causes; the unshared
portion enters as noise proportional to (1 − overlap). Discovery of groupings
becomes possible where absolute-feature clustering is confounded by context.

**Positioning vs. deep learning:** trajectory transformers predict; their
latent styles are unidentifiable, unnameable, unauditable. Our claims are
(i) interpretable grouping criteria, (ii) validated explanation fidelity
against generative ground truth, (iii) sample efficiency in the few-trajectory
partial-observation regime, (iv) complementarity — discrete interpretable
types as conditioning inputs that recover most of the implicit-style benefit.
The transformer is a baseline and a collaborator inside the experiments, not
a rival outside them.

**Research-identity link:** the evaluation instrument is the heterogeneous
shepherding testbed (sheep agents with distinct behavior-weight vectors), and
the downstream motivation — who to influence, whose trajectory to predict —
is swarm guidance and trusted autonomy.

## 2. Venues

| Option | Fit | Notes |
|---|---|---|
| **IEEE Trans. on Cybernetics (TCYB)** — *primary recommendation* | agents/swarms/learning systems; strong identity fit; prior publication track record there (WGLAE) | Q1, IF ~10; methods+systems papers with substantial experiments are the house style. |
| **IEEE Trans. on Systems, Man, and Cybernetics: Systems (TSMC-S)** | systems-of-agents framing | Q1, IF ~8; slightly more systems-engineering flavored; good fallback within the same community. |
| IEEE TKDE | data-mining framing (clustering/trees) | A*, IF ~9; if reviewers should be data-mining people rather than cybernetics people. |
| Machine Learning / DMKD (or via ECML-PKDD journal track) | interpretable-trees lineage | Rigorous, respected; journal-track gives a conference talk too. |
| Swarm Intelligence (journal) | domain fallback | Lower tier; only if scope narrows to swarms. |

**Recommendation:** write for **TCYB**; keep TSMC-S and TKDE as re-targets
without structural rewrites (all three tolerate the same anatomy: problem
formalisation + method + theory lemma + extensive controlled/real
experiments + released artifact).

## 3. Problem formalisation (paper §3, to be drafted first)

- **Given:** one episode; agents a₁…a_N with trajectories/feature streams;
  co-context pairs constructed by spatio-temporal proximity, each with an
  estimated context-overlap ω ∈ [0,1]. No labels; number of groups K unknown.
- **Find:** a partition of agents into behavioral groups **and** a relational
  rule set (tree) that defines each group boundary — the explanation is part
  of the output, not an afterthought.
- **Assumptions (stated, testable):** type persistence within an episode
  (an agent's group is stable over the observation window); overlap
  estimable from observables; groups differ in *response weights* (relational
  signatures under shared stimuli), not merely absolute offsets.
- **Theory targets:**
  - *Graded-invariance lemma* — probability that a within-pair relation flips
    due to unshared context, as a function of overlap ω and effect size;
    formalises why relational features beat absolute features for ω < 1.
  - *Representational proposition* — difference-vector and pointwise-scoring
    methods cannot express XOR-of-relations (empirical half already in
    research/results/; formal statement + proof sketch).

## 4. Method — staged, with Stage 1 strictly in service of Stage 2

**Stage 0 — infrastructure.**
(a) Heterogeneous paired-episode generator on the Shepherding Library: K
sheep types as behavior-weight vectors designed to differ *relationally*
(weight ratios/combinations, not absolute scalars); knobs = type separation,
perception overlap ω, noise, N, K; ground truth recorded.
(b) Co-context pairing module: candidate pairs by proximity windows; ω
estimated per pair (view/field overlap proxy).
(c) Relational feature library over paired windows: point relations
{min, eq, max}, nominal {eq, neq}, **temporal relations** {persistent-min,
diverging, converging, crossing-count, lagged-response}.

**Stage 1 — self-supervised validation path (kept minimal; not a headline).**
Identity-over-time gives free labels: same agent in two windows = same-type
pair; distinct agents = weak different-type candidates (debiased weighting for
same-type contamination). Train the *supervised* relational tree on this
same/different verification task. Purpose, strictly: (i) verify that
co-context pairing + relational features carry signal at realistic ω;
(ii) provide pseudo-labels/initialisation for Stage 2; (iii) serve as the
ablation baseline that justifies Stage 2's added machinery. Deliverable is a
go/no-go signal and a baseline row in every table — not a paper section of
its own.

**Stage 2 — the core: unsupervised relational split trees.**
Replace label-dependent gain ratio with an unsupervised split objective over
relations. Candidate criteria (to be selected by ablation on the generator):
1. relation-consistency partitioning — choose the relation whose induced
   grouping maximises within-group relation consistency vs between-group
   inconsistency across co-context pairs;
2. tree-constrained graph partitioning — build the same/different affinity
   graph (from Stage-1 scores or raw relation agreement) and grow a tree whose
   each cut is a *relational predicate* best approximating the spectral cut
   (interpretable surrogate splits);
3. MDL-based relational clustering trees.
Guards against degenerate solutions (single-agent leaves, all-one-cluster)
via minimum-support and stability constraints. K selected by stability /
MDL, reported with sensitivity analysis.

**Stage 3 — temporal relation family** integrated as first-class split
predicates (this is where the "long-term relationships between pairs" idea
lives: persistence and gap-trend over the episode, not snapshot relations).

**Stage 4 — theory** (lemma + proposition above), written against Stage 0's
knobs so every assumption has a matching experiment.

## 5. Planned experiments

| ID | Question | Design |
|---|---|---|
| X1 | Discovery quality | ARI/NMI vs ground-truth types across type-separation × overlap ω × K × N grid; baselines: k-means/spectral on absolute features, trajectory-embedding clustering (transformer or AE encoder + k-means), Stage-1-only pseudo-label clustering, GBT-difference clustering |
| X2 | **Explanation fidelity (flagship)** | alignment of learned relational rules with the generative weight dimensions that actually differ (rule–factor matching score); embedding baselines scored by post-hoc probe for contrast |
| X3 | Graded invariance | empirical relation-flip rate vs ω against the lemma's prediction |
| X4 | Sample efficiency | discovery quality vs #episodes/#agents; crossover point vs transformer-embedding baseline |
| X5 | Temporal ablation | point-only vs +temporal relations on the same data; the temporal family must earn its complexity |
| X6 | Downstream value | type-conditioned trajectory prediction (transformer with/without discovered type token; vs its own implicit latent); influence attribution demo (who is shepherding whom) |
| X7 | Real data | collective-behavior datasets with exogenous validation — sheep–dog GPS (herding literature), pigeon flock GPS (leadership hierarchies), baboon troop GPS (Strandburg-Peshkin), fish schooling data; groupings checked against known roles/species/age metadata and cross-day stability |
| X8 | Failure boundary | characterise where discovery collapses (separation too small, ω too low, windows too short) — reported as a result, not hidden |

## 6. Work plan

| Phase | Content | Estimate |
|---|---|---|
| M1 | Formal problem statement + lemma sketch (paper §3 draft) | 2 wks |
| M2 | Stage 0 generator + pairing + feature library | 4–6 wks |
| M3 | Stage 1 validation run (go/no-go at realistic ω) | 2 wks |
| M4 | Stage 2 criterion selection + implementation | 6–8 wks |
| M5 | Stage 3 temporal integration + X5 | 3 wks |
| M6 | X1–X6 full sweeps | 4 wks |
| M7 | X7 real-data acquisition + case studies | 4 wks (parallelisable) |
| M8 | Theory write-up, paper assembly, TCYB submission | 4 wks |

Realistic wall-clock: 6–9 months part-time. Dependency on Paper 1: the WEKA
artifact + nominal relations ship first and are cited as the software base;
Stage-2/3 code joins the library as v2 after acceptance.

## 7. Risks and mitigations
- **Degenerate unsupervised splits** — the central technical risk; mitigated
  by the three-candidate criterion bake-off on ground truth before any real
  data, and by Stage 1 as fallback headline if Stage 2 underdelivers
  (paper degrades gracefully to "self-supervised relational discovery").
- **Type-persistence violations** (role switching, fatigue) — detectable as
  within-agent inconsistency; reported, and flagged as future work.
- **"Your generator, your rules"** — generator released with seeds/splits as
  an open benchmark; X7's external datasets are mandatory, not decorative.
- **TCYB review latency** — plan for one major-revision cycle; TSMC-S/TKDE
  re-targets prepared by keeping the framing community-neutral.
