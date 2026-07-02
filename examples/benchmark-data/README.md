# Benchmark data (development/testing inputs)

CSV datasets used during the development and testing of the classifier's
standard (non-pairwise) C4.5 code path. They are **not** part of the PWCCP
experiments reported in the papers — for those, see [`../../data/`](../../data/).

- `iris.csv`, `glass.csv`, `diabetes.csv`, `contact-lenses.csv`, `crx.csv`,
  `golf.csv`, `ionosphere.csv`, `labor.csv`, `soybean.csv`, `vote.csv`,
  `weather.csv` — classic benchmark datasets, widely distributed with C4.5
  and Weka; originally from the
  [UCI Machine Learning Repository](https://archive.ics.uci.edu/). Credit for
  these datasets belongs to their original donors.
- `T1.csv` … `T5.csv`, `Ex2.csv`, `example.csv`, `example_missing.csv`,
  `test_example.csv`, `testing.csv` — small hand-made examples used while
  developing the algorithm.

Sample outputs produced from these files are in
[`../sample-output/`](../sample-output/).
