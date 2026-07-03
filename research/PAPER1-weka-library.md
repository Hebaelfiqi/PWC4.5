# Paper 1 — PWC4.5 in WEKA: pairwise comparative classification beyond translator stylometry

**Type:** software / resource paper (deliberately low-effort, high-reuse)
**Status:** planning → execution
**Objective (one line):** bring the TALLIP 2016 method back to life as a
maintained WEKA package, extend it beyond the translator-stylometry niche
(nominal attributes + naturally pairwise datasets from the literature), and
give the method a citable, discoverable software identity.

---

## 1. Context and vision

PWC4.5 (El-Fiqi, Petraki, Abbass, ACM TALLIP 2016) defined Pairwise
Comparative Classification Problems (PWCCP) and showed that relational splits
recover within-pair structure that value-threshold classifiers cannot. The
method has lived since 2013 as a standalone Java artifact tied to one
application. This paper repositions it as general-purpose infrastructure:

- **Revival** — reference implementation released (done: GitHub + Zenodo DOI,
  v1.0.0), now integrated into WEKA where tree methods are actually compared.
- **De-niching** — the pair-as-single-instance representation makes PWCCP data
  a first-class citizen of standard ML tooling; evaluation moves from one
  corpus to naturally pairwise problems across domains.
- **First scope extension** — nominal attributes (relation ∈ {equal,
  not-equal}), opening categorical domains (matched case–control covariates,
  discrete-choice data) that the numeric-only method could never touch.

Explicit non-goals (reserved for Paper 2): temporal relations, multi-class
CCP, unsupervised/open CCP, new theory beyond a brief statement of the
representational argument.

## 2. Venues

| Option | Effort | Recognition | Notes |
|---|---|---|---|
| **WEKA package repository** (distribution, not a paper) | S | community-facing | Submit package zip (`Description.props` + jar + docs) to the WEKA team for listing as an official/unofficial package. This is the "publish in WEKA" step and should happen regardless of the paper venue. |
| **SoftwareX** (Elsevier) — *primary recommendation* | S | modest but legitimate (software journal, indexed, DOI'd artifact) | 4–6 page template (motivation, architecture, illustrative example, impact). Matches the low-effort constraint best. |
| **JOSS** | S | respected in open-source research software circles | ~2-page paper + open code review. Fast, citable; can coexist with SoftwareX? No — pick one of the two. |
| **JMLR (MLOSS track)** — *stretch* | M | high (JMLR brand) | 4 pages but a high code-quality/adoption bar and slow review. Only if we decide the extra polish is worth it after the package is live. |

**Recommendation:** WEKA package repo + SoftwareX. Revisit JMLR-MLOSS after
the package accumulates users.

## 3. Approach and methodology

### 3.1 Software deliverables
1. **WEKA package** `pwc45` — the `weka.classifiers.trees.PWC45`
   AbstractClassifier (research/weka/, validated) hardened:
   - error-based pruning to match the reference implementation;
   - **nominal relation** {equal, not-equal} alongside numeric {min, eq, max};
   - Explorer/Experimenter/KnowledgeFlow compatibility, options, javadoc;
   - packaged per WEKA spec (Description.props, jar, example ARFFs), unit
     tests, CI job.
2. **Converters** — `make_pair_arff.py` generalised (nominal support, CSV
   input) so any paired dataset becomes WEKA-ready in one command.
3. **Datasets release** — all evaluation datasets in pair-ARFF with
   documentation, added to the repository (CC BY 4.0 where ours; converters +
   fetch scripts where third-party licensing requires).

### 3.2 The pair-as-single-instance representation (the enabling idea)
Each pair is one instance with 2n attributes (p1's features then p2's) and
the ordered-assignment class. Pairing becomes invisible to WEKA: CV folds
cannot split a pair, and the Experimenter's paired significance tests apply
unchanged. This representation is itself a small reusable contribution and
gets its own subsection.

### 3.3 Evaluation datasets — naturally pairwise problems from the literature
Chosen for public availability + minimal preparation + domain spread:

| # | Dataset | Domain | Features | What it demonstrates |
|---|---|---|---|---|
| D1 | Translator stylometry (21 pairs, in-repo) | comp. linguistics | numeric (motifs) | continuity with TALLIP/PLOS ONE; reproduces published results inside WEKA |
| D2 | Synthetic 2D/5D XOR (in-repo) + **categorical analogue (new)** | synthetic | numeric + nominal | representational necessity; validates the nominal relation |
| D3 | **Matched case–control**: secondary-infertility study (R `infert`); Breslow & Day endometrial-cancer 1:1 matched set | biomedical | mixed, largely **nominal** | the matched-pairs setting TALLIP cited but could not run; nominal extension in a real domain; baseline: conditional logistic regression (the domain standard) |
| D4 | **Disease progression as paired visits**: UCI Parkinson's Telemonitoring (~42 patients, ~6 months of repeated voice measurements) | biomedical / repeated measurements | numeric | pair = (visit t1, visit t2) of the same patient, class = which is later (progression direction); interpretable progression markers from the tree; repeated-measurements story without Paper-2 machinery |
| D5 | SUSHI preference dataset (or comparable public conjoint/choice set) | consumer choice | **nominal**-heavy | discrete choice as native PWCCP; second nominal domain |
| D6 | French-English corpus restoration (Verne, Mercier 1872 vs Walter 1991 — both on Project Gutenberg) | comp. linguistics | numeric | restores the TALLIP second corpus to the artifact — **optional / stretch**, include if the feature pipeline is cheap |

(Candidates PAN / LFW / MSLR are deliberately deferred — access friction and
scale belong to Paper 2's evaluation budget, not a low-effort software paper.)

### 3.4 Experiments
All inside the WEKA Experimenter (10× CV or provided splits, corrected paired
t-tests), which is itself part of the demonstration:

- **E1 (reproduction):** PWC45 vs J48 vs GBT+J48 on D1 — matches published
  results (already validated: PWC45 98% vs J48 62% CV on Asad–Daryabadi).
- **E2 (necessity + nominal validation):** D2 numeric and categorical XOR —
  PWC45 recovers relational structure; J48/GBT/SVM at chance. The categorical
  analogue is the acceptance test for the nominal implementation.
- **E3 (matched biomedical):** D3 — PWC45 vs J48, GBT, conditional logistic
  regression; emphasis on the readable rules over matched covariates.
- **E4 (progression):** D4 — direction-of-progression accuracy + the tree as
  a progression-marker summary.
- **E5 (choice):** D5 — nominal relations on preference pairs.
- Each experiment reports accuracy ± std, significance, tree size, and one
  exhibit tree (interpretability is a claimed feature; show it every time).

### 3.5 Paper skeleton (SoftwareX shape)
Motivation & significance (PWCCP recap, 1 page) → Software description
(architecture, representation, relations incl. nominal) → Illustrative
examples (E1–E5 condensed) → Impact (who reuses this: forensics, matched
studies, choice modelling) → Conclusions. Target 6 pages.

## 4. Work plan and effort

| Step | Deliverable | Effort |
|---|---|---|
| P1.1 | Nominal relation in WEKA classifier + reference converter; categorical-XOR generator + validation (E2) | days |
| P1.2 | Pruning parity + options/javadoc/tests; package as WEKA `pwc45` zip; CI | ~1 wk |
| P1.3 | D3–D5 acquisition + conversion + baselines | ~1 wk |
| P1.4 | Experimenter runs E1–E5, tables, exhibit trees | days |
| P1.5 | (stretch) D6 French-English pipeline | ~1 wk, optional |
| P1.6 | Draft + WEKA package submission + SoftwareX submission | ~1 wk |

Total: ≈ 4–6 part-time weeks without D6.

## 5. Risks
- *WEKA package acceptance* is a courtesy process with the WEKA maintainers —
  unofficial listing is the fallback and is sufficient for the paper.
- *Conditional logistic regression baseline* lives in R; run it there and
  report, rather than reimplementing.
- Keep scope discipline: any temptation to add temporal/unsupervised material
  is Paper 2 leakage.
