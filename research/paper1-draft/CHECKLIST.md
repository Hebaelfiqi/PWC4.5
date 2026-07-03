# Paper 1 — completion checklist

## Done (validated in-repo)
- [x] Pair-as-single-instance representation (converters + classifier).
- [x] WEKA `PWC45` classifier: numeric relations, **nominal relations**,
      C4.5-style pruning, options, GUI metadata. Compiles & runs on WEKA 3.8.6.
- [x] Installable `pwc45` package (Description.props + build_package.sh; zip
      built and structure-verified).
- [x] Nominal validation (categorical XOR): PWC45 100% vs J48 chance (α≥5).
- [x] Applied datasets converted (infert, Parkinson's, SUSHI) + GBT baseline.
- [x] E1–E5 run; "when relational splits help" decision rule established.
- [x] SoftwareX draft (paper1-softwarex.md).
- [x] WEKA submission guide (weka/SUBMISSION.md).

## Remaining before submission (needs author input or non-code effort)
- [ ] Fill metadata TODOs: corresponding-author email, maintainer email,
      support email, full dataset citations.
- [ ] Architecture figure + interpretability-tree figure (draw for camera).
- [ ] Confirm exhibit tree text (infert) against a final fixed seed/run.
- [ ] Optional stretch (P1.5): restore the French-English Verne corpus
      (Mercier 1872 / Walter 1991, both public-domain) as a sixth dataset —
      include only if the stylometric feature pipeline is cheap to stand up.
- [ ] Merge the `research/weka/` classifier into the main library as v1.1.0,
      cut a GitHub release with the package zip attached, update the Zenodo
      version DOI, and cite that DOI in the paper's metadata table.
- [ ] Internal read-through by co-authors; then submit package to WEKA team
      and paper to SoftwareX.

## Suggested submission order
1. Fill TODOs + figures.
2. Cut library v1.1.0 release (classifier + package zip) → Zenodo DOI.
3. Submit `pwc45` to WEKA (unofficial install already works).
4. Submit paper to SoftwareX referencing the release + DOI.
