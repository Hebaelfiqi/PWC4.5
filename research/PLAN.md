# PWC4.5 — Research roadmap

Two papers govern all work on this branch. Full context, venues, methodology
and experiment plans live in their own documents:

| Paper | One-liner | Target venues | Doc |
|---|---|---|---|
| **Paper 1** | PWC4.5 as a WEKA package: revival + nominal attributes + naturally pairwise datasets across domains (stylometry, matched case–control, disease progression, choice) | WEKA package repo + SoftwareX (stretch: JMLR-MLOSS) | [PAPER1-weka-library.md](PAPER1-weka-library.md) |
| **Paper 2** | Open Comparative Classification: unsupervised relational split trees discovering behavioral groupings from co-occurring agents under partial context overlap; heterogeneous-swarm ground-truth validation + real collective-behavior data | IEEE TCYB (alt: TSMC-S, TKDE) | [PAPER2-open-ccp.md](PAPER2-open-ccp.md) |

**Sequencing:** Paper 1 first (low effort, ships the artifact Paper 2 cites),
Paper 2's Stage-0 infrastructure can start in parallel once Paper 1 enters
its writing phase.

**Key design decisions already made:**
- Pair-as-single-instance representation for WEKA (pairs survive CV folds) —
  implemented and validated in [weka/](weka/).
- Paper 2 treats the self-supervised path (identity-over-time labels) strictly
  as validation/de-risking for the unsupervised relational split trees — the
  unsupervised method is the headline.
- The temporal relation family ("long-term relationships between pairs") lives
  in Paper 2, not Paper 1.
- The heterogeneous shepherding generator is the measurement instrument
  (ground-truth types, overlap knob), not the use case; real collective-
  behavior data is mandatory in Paper 2.

**Evidence in hand** (see [results/](results/)): reproduction of published
results on Java 17; WEKA classifier validated (PWC45 98% vs J48 62%, 10-fold
CV, identical pair-instances); baseline comparison — synthetic relational
data: PWC4.5 ≈100% vs pairwise SVM / RankNet / LambdaMART at chance;
translators: PWC4.5 78.3% ≈ RankNet 80.1% ≫ SVM 61.9%, LambdaMART 58.8%.

Historical planning notes (superseded by the two paper docs) are retained in
git history.
