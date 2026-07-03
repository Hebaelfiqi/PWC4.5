# Datasets for PWCCP evaluation

PWCCP requires **paired** data: instances grouped in pairs (or blocks) where
each member belongs to a different class and the discriminative signal is the
within-pair relationship. Below: the in-repo datasets, then external public
datasets suitable for demonstrating the comparison.

## In-repo (already available)

| Dataset | Pairs | Features | Task |
|---------|------:|----------|------|
| Synthetic 2D | 200 tr / 100 te ×10 | 2 numeric | XOR-relation recovery, 8 noise levels |
| Synthetic 5D | 500 tr / 200 te ×10 | 5 numeric | as above, larger space |
| Translator stylometry | 50 tr / 24 te ×10, 21 pairs | 212 motif freqs | which translator |

These are immediately usable and give a direct tie-back to the published
results. **Start here** (already wired into the harness).

## External candidates (paired / comparative / matched)

Selected for genuine pair structure and public availability. Each needs a
converter into the pair-as-instance representation.

### Authorship / stylometry (closest domain)
- **PAN authorship-verification corpora** (pan.webis.de) — "same author?"
  given a pair of documents. Naturally pairwise; large, standard benchmark.
  Strong fit for RQ1/RQ4. *License: research use per PAN.*
- **Federalist Papers** (disputed-authorship, classic) — small, interpretable;
  good for the small-sample and readability story (RQ2/RQ4).
- **CCAT/Reuters author sets, Spooky Authorship (Kaggle)** — convertible to
  same/different-author pairs.

### Biometric / forensic verification (native pairwise)
- **LFW pairs** (Labeled Faces in the Wild) — the canonical same/different
  face-pair benchmark; use provided embeddings to keep it numeric. Tests the
  method outside text. *License: research use.*
- **Speaker verification trial pairs** (e.g., VoxCeleb verification lists) —
  same/different-speaker pairs over embeddings.
- **Signature/handwriting verification** (e.g., CEDAR, GPDS) — genuine vs.
  forged pairs.

### Matched / repeated-measures (biomedical, the paper's motivating analogy)
- **Paired-organ / bilateral measurement datasets** (e.g., paired kidney or
  eye measurements) — the "matched pair" setting cited in TALLIP 2016.
- **Twin/sibling matched-pair studies** where available with numeric features.

### Preference / competition
- **Chess/sports head-to-head** (player A vs B feature vectors → winner) —
  abundant, purely numeric, easy to frame as ordered-pair classification.
- **Learning-to-rank benchmarks** (MSLR-WEB10K/30K, LETOR) — restricted to
  pairs within a query; connects directly to the LambdaMART/RankNet lineage
  and lets us compare on their home turf.

## Recommended demonstration set (minimal but convincing)

To make the case without boiling the ocean, three external datasets spanning
the regimes:

1. **PAN authorship verification** — text, same domain, standard benchmark.
2. **LFW pairs (embeddings)** — biometric verification, out-of-text generality.
3. **MSLR-WEB10K (pairs within query)** — ranking home turf, where beating or
   matching LambdaMART/RankNet would be the strongest possible result, and
   trailing-but-interpretable is still a defensible finding.

Together these test the three claims: domain relevance (PAN), generality
(LFW), and honest comparison against ranking incumbents (MSLR).

## Acquisition status

External datasets are **not** downloaded here (size/licensing/manual access).
`research/tools/` will hold per-dataset converters into the pair-as-instance
format as each is obtained. The in-repo datasets are fully wired and used for
the preliminary validated comparison.
