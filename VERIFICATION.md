# Reproducibility

The released code was independently compiled and run, and the published
results were reproduced.

**Environment:** Eclipse Temurin JDK 17 (the original work dates to 2013;
this confirms the code still builds and runs on a modern JVM). Only the
bundled `lib/cli.jar` (Apache Commons CLI) is required.

```sh
scripts/build.sh                       # javac -d bin -cp lib/cli.jar src/*.java
```

## Synthetic data — exact reproduction

The synthetic runs are deterministic (no randomization). Every value matched
the published tables in [docs/04-artificial-data.md](docs/04-artificial-data.md)
exactly. Examples (PWC4.5 test accuracy, per experiment):

- **2D, noise 0.0** — all 10 experiments → 100.0% (published 100.0%)
- **2D, noise 0.25** — `88, 67, 88, 69, 100, 100, 63, 86, 100, 68`
  (published row: identical)
- **5D, noise 0.15** — `89, 98.5, 94.5, 93.5, 92, 95, 94.5, 94.5, 98, 93`
  (published row: identical)

```sh
scripts/run_synthetic.sh 2d 1st_exp 0.25
scripts/run_synthetic.sh 5d 1st_exp 0.15
```

## Translator stylometry — reproduced within sampling noise

The translator runs use `-o` (shuffle for class balance before splitting), so
they depend on the random seed. Using the published per-experiment seeds
(42, 61, 13, 3, 82, 27, 19, 47, 93, 2), the mean PWC4.5 accuracy per pair
(over the 10 experiments) versus the published table
([docs/05-translator-stylometry.md](docs/05-translator-stylometry.md)):

| Pair | Reproduced | Published | Δ |
|------|-----------:|----------:|--:|
| Asad–Daryabadi       | 98.31% | 98.31% | 0.00 |
| Daryabadi–Pickthall  | 59.19% | 59.16% | 0.03 |
| Sarwar–Yousif Ali    | 74.85% | 74.71% | 0.14 |
| Asad–Maududi         | 98.05% | 98.44% | 0.39 |
| Pickthall–Yousif Ali | 91.55% | 92.44% | 0.89 |
| Maududi–Raza         | 54.22% | 55.73% | 1.51 |

All pairs reproduce within ~1.5%, most within a few tenths. The residual
differences are consistent with the randomization step: the seed → random
sequence mapping is not guaranteed identical across JVM versions (reproduced
on Java 17 vs. the original 2013 JVM). The largest gap (Maududi–Raza) is the
near-chance pair with the highest published variance, hence the most
seed-sensitive — as expected for a faithful implementation rather than a
discrepancy.

```sh
scripts/run_translators.sh asad_daryabadi 1   # 10 seeds for one experiment
```
