# PWC45 as a WEKA classifier

A `weka.classifiers.AbstractClassifier` implementation of PWC4.5, usable in the
WEKA Explorer, Experimenter, and KnowledgeFlow, and directly comparable to J48
and other WEKA classifiers with WEKA's own cross-validation and significance
testing.

## Representation: pair-as-single-instance

Each pair is one WEKA instance: the first `n` numeric attributes are p1's
features, the next `n` are p2's, and the nominal class is the class of p1
(p2 is the other class). This makes the pairing invisible to WEKA — a
cross-validation fold can never split a pair. Build such ARFF files from any
PWCCP dataset with:

```sh
python ../tools/make_pair_arff.py <stem> .data -o train.arff --seed 42
python ../tools/make_pair_arff.py <stem> .test  -o test.arff  --seed 42
```

`PWC45` splits each node on the within-pair relation
`R(V_i(p1), {V_i(p1), V_i(p2)})` ∈ {min, eq, max}, choosing the attribute that
maximises the C4.5 gain ratio.

## Build and run

Requires a Weka jar (tested with Weka 3.8.6). On Java 9+ add
`--add-opens java.base/java.lang=ALL-UNNAMED` (Weka's package manager needs it).

```sh
export WEKA_JAR=/path/to/weka.jar
javac -cp "$WEKA_JAR" -d out PWC45.java

# train/test evaluation
java --add-opens java.base/java.lang=ALL-UNNAMED -cp "$WEKA_JAR:out" \
     weka.classifiers.trees.PWC45 -t train.arff -T test.arff

# 10-fold cross-validation (pairs stay intact within a fold)
java --add-opens java.base/java.lang=ALL-UNNAMED -cp "$WEKA_JAR:out" \
     weka.classifiers.trees.PWC45 -x 10 -t train.arff
```

`build_and_run.sh` wraps these (set `WEKA_JAR` first). In the WEKA **Explorer**,
put `out/` (or a jarred package) on the classpath and select
`trees > PWC45`; in the **Experimenter**, add it alongside J48 to benchmark.

## Validated results (Asad–Daryabadi, Exp_1)

Reproduced on Temurin JDK 17 with Weka 3.8.6:

| Setting | PWC45 | J48 (C4.5) |
|---------|------:|-----------:|
| Train / held-out test accuracy | **95.83%** | — |
| 10-fold CV accuracy (same pair-instances) | **98%** | **62%** |

The 95.83% test accuracy matches the reference `PWC45.java` JAR on the same
experiment. The CV head-to-head (98% vs 62%) reproduces the paper's central
finding — C4.5 on the raw pair features cannot capture the within-pair
relationship, while PWC4.5's relational splits can — now demonstrated natively
through WEKA's evaluation.

## Status / roadmap

This is a functional clean-room implementation of the relational-split core
(unpruned; min/eq/max relation family). Planned (see [../PLAN.md](../PLAN.md)):
error-based pruning to match the reference implementation exactly; nominal-
attribute relations (equal / not-equal); and the multi-class CCP generalisation.
Packaging as an installable Weka package (`Description.props` + jar) is the last
step before distribution.
