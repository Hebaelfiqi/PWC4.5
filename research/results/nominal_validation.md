# Nominal relation — validation (Paper 1, E2 acceptance test)

The nominal within-pair relation {eq, neq} was added to the WEKA `PWC45`
classifier and validated on a categorical analogue of the synthetic XOR data
(`research/tools/make_categorical_xor.py`): two nominal attributes, pair label
= parity of the within-pair equality predicates, absolute values uniformly
random (no pointwise signal). Class-balanced by construction; test sets
noise-free. Weka 3.8.6, Temurin JDK 17.

## Result (held-out test accuracy, 200 training / 100 test pairs, seed 7)

| Alphabet size | PWC45 (nominal relations) | J48 on raw pair attributes |
|---:|---:|---:|
| 3  | **100%** | 77% |
| 5  | **100%** | 41% |
| 10 | **100%** | 46% |
| 20 | **100%** | 57% |

PWC45 learns the exact 4-leaf parity tree over relations:

```
R(C1) = neq
|  R(C2) = neq : +
|  R(C2) = eq  : -
R(C1) = eq
|  R(C2) = neq : -
|  R(C2) = eq  : +
```

## Two methodological findings worth carrying into the paper

1. **The nominal necessity argument is sample-complexity, not
   representability.** Unlike the continuous case (where difference-vector /
   value-threshold methods provably cannot express relation-XOR), nominal
   equality *is* representable by a value-splitting tree through enumeration
   of value combinations (at alphabet 3, J48 reaches 77% by memorising the
   ~81 cells). The advantage of the relational split is that its sample
   complexity is independent of alphabet size, while enumeration grows with
   the square of it — at alphabet ≥5 with 200 training pairs, J48 is at
   chance. State this honestly in the paper; it is a *stronger* practical
   claim, not a weaker one (real categorical domains have large alphabets).

2. **Class-balance pitfall in parity-over-equality generators.** Sampling
   pair values uniformly makes within-pair matches rare for large alphabets;
   the parity label then collapses to the "no match" class and a
   majority-class baseline masquerades as learning (J48 appeared to *improve*
   with alphabet size — 79%→94% — purely from imbalance). The generator
   constructs balanced classes ('+' = exactly one match; '-' = both/neither,
   evenly split) so the task remains true parity at every alphabet size.

## Status

- WEKA classifier: nominal relations implemented, mixed numeric+nominal
  supported, type-consistency between pair sides enforced, readable
  attribute names in printed trees. Validated as above.
- Converter nominal support: deferred to P1.3, where the medical/choice
  datasets arrive as CSV and need the CSV → pair-ARFF path anyway.
