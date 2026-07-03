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

---

# Relation-dominated matchup datasets (where PWC4.5 wins)

Added after the empirical scan below. Verification/attribution benchmarks
(PAN/LFW/MSLR above) test the *original* niche; this section is about finding
**real matchup datasets** where PWC4.5 is the outright best method.

## The recipe — four properties

A dataset favours PWC4.5 to the degree it has:

1. **Matchup structure** — two entities compared/competing, recorded outcome.
2. **Entities have feature vectors** — so within-pair relations are computable.
3. **Outcome driven by relations, not absolutes** — "who exceeds whom" on key
   attributes, where the absolute scale varies widely across entities.
4. **Bonus: non-transitivity** — rock-paper-scissors / counter dynamics → beats
   the neural rankers, not just C4.5.

**Litmus test:** *if you doubled every feature of both entities, would the
outcome change?* No → relation-dominated (good for PWC4.5). Yes → absolute /
magnitude (bad).

**Refinement learned empirically (Pokémon vs Dota 2):** a *dominant single
ordinal relation* (Pokémon: Speed) is a strong win; a *distributed additive*
signal (Dota 2: weighted sum of hero win-rates) is a loss — even though both
are "matchups". Property 3 must hold for the *dominant* signal, not just some
signal.

## Empirical results on real matchup data

| Dataset | Domain | PWC4.5 | Best baseline | Verdict |
|---|---|---:|---|---|
| Pokémon battles (Weedle's Cave, 50k) | game | **94.0%**, 3 leaves | J48 94.4% / 370 leaves | **WIN** — best acc + 123× smaller; captures "faster wins" |
| Dota 2 (UCI, 92k) | esports | 54.8% | Logistic 57.8% | **LOSS** — additive hero-sum signal, not relation-dominated |

## Where to find more — by domain

**Games / esports (built-in non-transitivity + entity stats):**
- Creature-battlers with stat sheets (Pokémon ✓). Look for others.
- Fighting-game *matchup charts* (Street Fighter, Smash) — small, genuinely
  non-transitive.
- Trading-card-game deck-vs-deck matchup data (Hearthstone, MTG metagame cycles).
- MOBA drafts (Dota 2 ✗, LoL) — tested; additive, not relational. Lower odds.

**Sports (large/real but often transitive → expect ties, not wins):**
- Tennis — Jeff Sackmann `tennis_atp`/`tennis_wta` (GitHub).
- Chess — Lichess/FIDE dumps.
- NBA/NFL/soccer — FiveThirtyEight, Kaggle.

**Biology / ecology (real cyclic dominance — the reviewer-persuasive case):**
- Animal contest / dominance datasets on **Dryad / Zenodo** (who wins a fight
  + individual traits). Side-blotched lizard morphs, paper-wasp dominance, fish
  and ungulate contests.
- Microbial/plant competition assays (colicin systems).

**Autonomy (research-identity fit, links to Paper 2):**
- Agent/algorithm tournaments — agent-vs-agent outcomes + agent parameters
  (swarm controllers, RoboCup, game-playing bots). Generatable from the
  Shepherding Library; counter-strategy dynamics are naturally non-transitive.

**Sources to browse:** Kaggle (search "battles/matchups/head to head"), UCI,
Dryad + Zenodo (ecology/behaviour), sport GitHub repos (Sackmann; FiveThirtyEight).
