# Artificial (Synthetic) Data

> Corresponds to: the synthetic-data experiments in the ACM TALLIP 2016
> article and the PhD thesis (2013). Datasets:
> [`../data/2d_data/`](../data/2d_data/) and
> [`../data/5d_data/`](../data/5d_data/) — see also the
> [data documentation](../data/README.md).

## Data generation

The data were generated using an XOR function over the predicate that Vj(p1)
is the minimum between Vj(p1) and Vj(p2), as follows:

```
if ⊕ (R(Vj(p1), {Vj(p1), Vj(p2)}) = min) then p1 → +, p2 → −
```

In these rules, p1 is labeled `+` if the number of minimum relations is even;
otherwise it is negative. For the 2D case, 200 pairs were generated for
training and 100 for testing. For the 5D case, we increased the number of
pairs to 500 for training and 200 for testing to cover the larger space.

We experimented with 7 levels of noise in addition to the noise-free baseline
case. The test sets were always noise-free. For each of the eight cases
(7 noise levels plus noise-free) in each of the two problems (2D and 5D),
10 datasets were independently sampled using the above XOR relationship
(the `1st_exp` … `10th_exp` folders).

These results were then collected to perform t-test analysis of the
performance of C4.5 and PWC4.5 at each noise level. A one-tail paired t-test
with confidence level of 95% (alpha = 0.05) was carried out. The proposed
hypothesis is "the average accuracy of PWC4.5 is better than traditional
C4.5":

```
H0 : μ(PWC4.5) ≤ μ(C4.5)
H1 : μ(PWC4.5) > μ(C4.5)
```

## Results — 2D artificial data

Summary of the results of one-tail paired t-tests between the accuracy of
C4.5 and the accuracy of PWC4.5 on 2D artificial data
(alpha = 0.05, degrees of freedom = 9). Accuracy per noise level,
C4.5 / PWC4.5:

| Experiment | 0% C4.5 | 0% PWC4.5 | 1% C4.5 | 1% PWC4.5 | 2.5% C4.5 | 2.5% PWC4.5 | 5% C4.5 | 5% PWC4.5 | 10% C4.5 | 10% PWC4.5 | 15% C4.5 | 15% PWC4.5 | 20% C4.5 | 20% PWC4.5 | 25% C4.5 | 25% PWC4.5 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1st Exp | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 55.00% | 100.00% | 55.00% | 100.00% | 58.00% | 88.00% |
| 2nd Exp | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 51.50% | 100.00% | 55.00% | 100.00% | 51.50% | 100.00% | 55.50% | 100.00% | 52.00% | 67.00% |
| 3rd Exp | 50.00% | 100.00% | 53.50% | 100.00% | 48.50% | 100.00% | 49.50% | 100.00% | 54.50% | 100.00% | 50.00% | 100.00% | 54.50% | 100.00% | 54.50% | 88.00% |
| 4th Exp | 51.50% | 100.00% | 63.00% | 100.00% | 50.00% | 100.00% | 57.00% | 100.00% | 50.00% | 100.00% | 58.00% | 100.00% | 55.50% | 82.00% | 60.50% | 69.00% |
| 5th Exp | 55.50% | 100.00% | 50.00% | 100.00% | 56.00% | 100.00% | 50.00% | 100.00% | 54.50% | 100.00% | 61.50% | 100.00% | 50.00% | 100.00% | 52.00% | 100.00% |
| 6th Exp | 50.00% | 100.00% | 50.00% | 100.00% | 51.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 58.50% | 100.00% | 51.50% | 100.00% | 52.00% | 100.00% |
| 7th Exp | 50.00% | 100.00% | 51.50% | 100.00% | 50.00% | 100.00% | 59.00% | 100.00% | 50.00% | 100.00% | 58.00% | 100.00% | 55.50% | 100.00% | 52.50% | 63.00% |
| 8th Exp | 54.50% | 100.00% | 52.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 57.00% | 100.00% | 54.00% | 100.00% | 55.50% | 100.00% | 58.50% | 86.00% |
| 9th Exp | 52.00% | 100.00% | 53.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 56.00% | 100.00% | 56.50% | 100.00% | 52.50% | 100.00% | 53.00% | 100.00% |
| 10th Exp | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 57.00% | 100.00% | 53.00% | 100.00% | 56.50% | 100.00% | 56.00% | 68.00% |
| **Average** | 51.35% | 100.00% | 52.30% | 100.00% | 50.55% | 100.00% | 51.70% | 100.00% | 53.40% | 100.00% | 55.60% | 100.00% | 54.20% | 98.20% | 54.90% | 82.90% |
| **STD** | 2.07% | 0.00% | 3.99% | 0.00% | 2.01% | 0.00% | 3.39% | 0.00% | 3.05% | 0.00% | 3.55% | 0.00% | 2.12% | 5.69% | 3.16% | 14.92% |

| Noise level | 0% | 1% | 2.5% | 5% | 10% | 15% | 20% | 25% |
|---|---|---|---|---|---|---|---|---|
| P(T ≤ t) one-tail | 3.64E-14 | 1.59E-11 | 2.38E-14 | 3.29E-12 | 1.76E-12 | 1.05E-11 | 2.46E-09 | 1.83E-04 |
| t-Stat | −74.36 | −37.76 | −77.94 | −45.02 | −48.26 | −39.55 | −21.44 | −5.53 |
| Hypothesis | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected |

## Results — 5D artificial data

Summary of the results of one-tail paired t-tests between the accuracy of
C4.5 and the accuracy of PWC4.5 on 5D artificial data
(alpha = 0.05, degrees of freedom = 9):

| Experiment | 0% C4.5 | 0% PWC4.5 | 1% C4.5 | 1% PWC4.5 | 2.5% C4.5 | 2.5% PWC4.5 | 5% C4.5 | 5% PWC4.5 | 10% C4.5 | 10% PWC4.5 | 15% C4.5 | 15% PWC4.5 | 20% C4.5 | 20% PWC4.5 | 25% C4.5 | 25% PWC4.5 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1st Exp | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 45.00% | 89.00% | 47.25% | 92.00% | 44.50% | 44.00% |
| 2nd Exp | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 49.75% | 98.50% | 46.50% | 57.00% | 45.25% | 87.00% |
| 3rd Exp | 53.00% | 100.00% | 51.50% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 49.50% | 100.00% | 50.00% | 94.50% | 48.75% | 90.00% | 46.75% | 47.50% |
| 4th Exp | 54.25% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 52.75% | 99.00% | 50.00% | 93.50% | 51.00% | 73.50% | 48.00% | 51.00% |
| 5th Exp | 55.25% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 52.00% | 92.00% | 48.25% | 83.50% | 43.25% | 40.00% |
| 6th Exp | 50.00% | 100.00% | 50.00% | 100.00% | 51.25% | 100.00% | 50.00% | 100.00% | 50.00% | 99.00% | 50.50% | 95.00% | 47.75% | 85.50% | 50.25% | 76.00% |
| 7th Exp | 51.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 52.00% | 100.00% | 50.00% | 100.00% | 50.00% | 94.50% | 55.00% | 78.00% | 50.25% | 74.50% |
| 8th Exp | 50.00% | 100.00% | 50.00% | 100.00% | 50.00% | 100.00% | 51.25% | 100.00% | 50.00% | 97.00% | 49.25% | 94.50% | 51.25% | 92.00% | 54.75% | 82.00% |
| 9th Exp | 50.00% | 100.00% | 53.25% | 100.00% | 50.00% | 100.00% | 53.50% | 100.00% | 50.00% | 97.00% | 55.25% | 98.00% | 52.25% | 87.00% | 49.75% | 79.50% |
| 10th Exp | 50.00% | 100.00% | 50.25% | 100.00% | 50.00% | 100.00% | 55.25% | 100.00% | 50.00% | 100.00% | 51.25% | 93.00% | 53.00% | 89.50% | 52.50% | 55.00% |
| **Average** | 51.35% | 100.00% | 50.50% | 100.00% | 50.13% | 100.00% | 51.20% | 100.00% | 50.23% | 99.20% | 50.30% | 94.25% | 50.10% | 82.80% | 48.53% | 63.65% |
| **STD** | 2.04% | 0.00% | 1.07% | 0.00% | 0.40% | 0.00% | 1.86% | 0.00% | 0.90% | 1.23% | 2.54% | 2.74% | 2.81% | 10.89% | 3.65% | 17.78% |

| Noise level | 0% | 1% | 2.5% | 5% | 10% | 15% | 20% | 25% |
|---|---|---|---|---|---|---|---|---|
| P(T ≤ t) one-tail | 3.19E-14 | 8.54E-17 | 9.93E-21 | 1.33E-14 | 3.25E-15 | 2.37E-13 | 2.32E-06 | 8.07E-03 |
| t-Stat | −75.47 | −145.79 | −399.00 | −83.18 | −97.29 | −60.35 | −9.69 | −2.95 |
| Hypothesis | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected | H0 rejected |

To reproduce: `scripts/run_synthetic.sh 2d <exp_folder>` and
`scripts/run_synthetic.sh 5d <exp_folder>` — see
[Using PWC4.5](03-using-pwc45.md).
