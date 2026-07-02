# Datasets

All datasets use the classic **C4.5 file format**, extended with a pairwise
convention (see "Pair encoding" below). Each dataset is a trio of files
sharing a *filestem*:

| File | Content |
|------|---------|
| `<stem>.names` | Schema: first line lists the class labels, then one line per attribute (`name: continuous.`) |
| `<stem>.data`  | Training instances, one per line: comma-separated attribute values, then the class label |
| `<stem>.test`  | Test instances, same format as `.data` |

These files are plain text and easy to parse; `scripts/convert_to_csv.py`
converts any `.data`/`.test` file to CSV with a header row taken from the
`.names` file.

## Pair encoding (important)

PWCCP data is **paired**: every two consecutive lines in a `.data`/`.test`
file form one pair `(p1, p2)` — the first line is `p1`, the second is `p2`,
each carrying its own class label. PWC4.5 reads the files two lines at a
time; the pairing is positional, so **do not shuffle lines individually** —
shuffle pairs. A file with 400 lines therefore contains 200 pairs.

## `2d_data/` and `5d_data/` — synthetic XOR data

Generated using an XOR function over the predicate "Vj(p1) is the minimum of
{Vj(p1), Vj(p2)}": p1 is labeled `+` if the number of minimum-relations
across attributes is even, otherwise `-`. See
[docs/04-artificial-data.md](../docs/04-artificial-data.md) for the full
generation procedure.

- Attributes: `V1, V2` (2D) or `V1..V5` (5D), all continuous. Classes: `+`, `-`.
- Sizes: 2D = 200 training pairs (400 lines) + 100 test pairs;
  5D = 500 training pairs + 200 test pairs.
- `1st_exp/` … `10th_exp/`: 10 independently sampled replications, used for
  the paired t-tests reported in the papers.
- Filestems encode the training-set noise level:
  `2D_Noise_0.0` (noise-free baseline), `_0.01`, `_0.025`, `_0.05`, `_0.1`,
  `_0.15`, `_0.2`, `_0.25`. **Test sets are always noise-free.**

## `translators_data/` — translator stylometry (Qur'an translations)

Parallel Arabic-to-English translations of 74 chapters of the Holy Qur'an by
seven translators: Asad, Daryabadi, Maududi, Pickthall, Raza, Sarwar,
Yousif Ali. Each of the 21 folders holds one translator pair (e.g.
`asad_daryabadi/`), with 10 experiments (`Exp_1` … `Exp_10`) per pair —
different random train/test chapter splits.

- Classes: the two translator names (e.g. `Asad`, `Daryabadi`).
- Attributes (212, all continuous): frequencies of **network motifs** in the
  directed word-adjacency network built from each translation (nodes = words,
  edges = "occurs immediately before" within a sentence) —
  `M3_ID1` … `M3_ID13` are the 13 connected directed 3-node motif types and
  `M4_ID1` … `M4_ID199` are the 199 connected directed 4-node motif types.
  Motif IDs follow the enumeration used by the motif-detection tool (see
  *Provenance and acknowledgements* below).
- Pair encoding: each pair of consecutive lines is the *same source chapter*
  translated by the two translators — the pairing is what PWC4.5 exploits.
- Sizes per experiment: 50 training pairs (100 lines), 24 test pairs
  (48 lines).
- These datasets are ordered; run PWC4.5 with `-o` (randomize for class
  balance) and `-b`, as in the published commands
  ([docs/03-using-pwc45.md](../docs/03-using-pwc45.md)).
- `asad_daryabadi/run_1/` contains the original published outputs of
  experiment 1, run 1, as released with the source code.

The network-motif feature representation follows the approach of the 2013
UNSW PhD thesis and the PLOS ONE 2019 article; see those references for the
full feature-extraction methodology.

## Provenance and acknowledgements

Only the derived numeric feature vectors are distributed here — **the source
translation texts are not included.** The features were computed from
third-party resources, gratefully acknowledged:

- **Translations** — obtained from the [Tanzil](https://tanzil.net) Qur'an
  project. The seven translators are Muhammad Asad, Abdul Majid Daryabadi,
  Abul Ala Maududi, Mohammed Marmaduke Pickthall, Ahmed Raza Khan,
  Muhammad Sarwar, and Abdullah Yusuf Ali. 74 chapters were used (the final
  six parts / *juz'* of the Qur'an).
- **Preprocessing (lemmatization)** — the
  [Natural Language Toolkit (NLTK)](https://www.nltk.org/).
- **Motif counting (size 3 and 4)** — the
  [Mfinder](https://www.weizmann.ac.il/mcb/UriAlon/download/network-motif-software)
  network-motif detection tool.

## License and citation

The datasets are licensed under **CC BY 4.0** (see [LICENSE](LICENSE)).
If you use them, please cite the publications in the repository
[README](../README.md#citing-this-work). The network-motif translator
features correspond to the 2013 UNSW PhD thesis
(DOI [10.26190/unsworks/16460](https://doi.org/10.26190/unsworks/16460)) and
the PLOS ONE 2019 article
(DOI [10.1371/journal.pone.0211809](https://doi.org/10.1371/journal.pone.0211809));
the PWC4.5 algorithm itself is described in the ACM TALLIP 2016 article
(DOI [10.1145/2898997](https://doi.org/10.1145/2898997)).
