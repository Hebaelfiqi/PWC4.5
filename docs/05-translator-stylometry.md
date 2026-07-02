# Translator Stylometry Identification Problem

> Corresponds to: the PhD thesis (2013) and the ACM TALLIP 2016 article
> (translator identification experiments); the network-motif features are
> detailed in the PLOS ONE 2019 article. Datasets:
> [`../data/translators_data/`](../data/translators_data/) — see also the
> [data documentation](../data/README.md).

## Problem definition

The translator stylometry identification problem refers to the identification
of stylistic differences that distinguish the writing style of different
translators.

## Data

We use parallel Arabic-to-English translations of the Holy Qur'an for this
study. The Holy Qur'an is divided mainly into 114 surah (pl. suwar), also
known as chapters, although they are not equal in length: a surah varies from
three ayat (verses) to 286 ayat. We refer to them as chapters and verses in
this study. The study uses the translations of 74 chapters of the Holy Qur'an,
for seven translators (Asad, Daryabadi, Maududi, Pickthall, Raza, Sarwar,
Yousif Ali), giving the 21 pairwise datasets under
[`../data/translators_data/`](../data/translators_data/).

## Feature extraction

We target the translator style by detecting the repeated patterns in the
translator's writings, using social network analysis. By representing the text
as a word adjacency network, we can use network motifs to detect the existence
of repeated patterns of ordered words — known in linguistics as "lexical
chunks" — in the text. The attributes (features) of the examined dataset are
the frequencies of network motifs of size three and size four.

## Results

Summary of the results of one-tail paired t-tests between the accuracy of
C4.5 and the accuracy of PWC4.5 (alpha = 0.05, degrees of freedom = 9):

| Translator Pair | C4.5 Mean | C4.5 Variance | PWC4.5 Mean | PWC4.5 Variance | t-Stat | P(T ≤ t) one-tail |
|---|---|---|---|---|---|---|
| Asad–Daryabadi | 56.24% | 1.46E-03 | 98.31% | 8.12E-04 | −31.70 | 7.59E-11 |
| Asad–Maududi | 52.99% | 6.74E-04 | 98.44% | 8.75E-04 | −28.22 | 2.14E-10 |
| Asad–Pickthall | 57.42% | 2.96E-03 | 95.46% | 1.15E-03 | −19.38 | 6.00E-09 |
| Asad–Raza | 55.48% | 4.31E-03 | 82.28% | 1.60E-03 | −12.41 | 2.90E-07 |
| Asad–Sarwar | 54.92% | 6.33E-03 | 95.04% | 7.42E-04 | −16.19 | 2.91E-08 |
| Asad–Yousif Ali | 55.63% | 2.78E-03 | 93.09% | 5.47E-04 | −19.78 | 5.00E-09 |
| Daryabadi–Maududi | 50.00% | 0 | 85.07% | 1.32E-03 | −30.57 | 1.05E-10 |
| Daryabadi–Pickthall | 50.00% | 0 | 59.16% | 1.67E-03 | −7.09 | 2.86E-05 |
| Daryabadi–Raza | 52.94% | 1.13E-03 | 80.66% | 7.90E-03 | −9.26 | 3.37E-06 |
| Daryabadi–Sarwar | 50.00% | 0 | 76.82% | 2.23E-03 | −17.97 | 1.16E-08 |
| Daryabadi–Yousif Ali | 51.67% | 2.52E-04 | 91.09% | 4.79E-04 | −59.29 | 2.78E-13 |
| Maududi–Pickthall | 50.00% | 0 | 79.45% | 5.45E-03 | −12.61 | 2.52E-07 |
| Maududi–Raza | 50.79% | 1.09E-03 | 55.73% | 3.36E-03 | −2.80 | 1.04E-02 |
| Maududi–Sarwar | 49.81% | 3.43E-05 | 63.77% | 1.50E-03 | −11.49 | 5.55E-07 |
| Maududi–Yousif Ali | 50.00% | 0 | 56.73% | 1.68E-03 | −5.19 | 2.85E-04 |
| Pickthall–Raza | 52.46% | 7.80E-04 | 81.96% | 2.36E-03 | −16.37 | 2.63E-08 |
| Pickthall–Sarwar | 49.78% | 4.73E-05 | 67.99% | 1.84E-03 | −13.94 | 1.07E-07 |
| Pickthall–Yousif Ali | 51.34% | 1.23E-03 | 92.44% | 7.93E-04 | −30.49 | 1.07E-10 |
| Raza–Sarwar | 50.66% | 2.29E-04 | 60.89% | 1.13E-03 | −8.32 | 8.05E-06 |
| Raza–Yousif Ali | 49.64% | 5.93E-05 | 65.97% | 4.27E-03 | −8.22 | 8.87E-06 |
| Sarwar–Yousif Ali | 52.72% | 1.04E-03 | 74.71% | 2.24E-03 | −11.12 | 7.32E-07 |
| **Average** | **52.12%** ± 6.13E-04 | | **78.81%** ± 2.08E-02 | | | |

The null hypothesis H0 (μ(PWC4.5) ≤ μ(C4.5)) was rejected for **all 21
translator pairs** (P < 0.05).

To reproduce: `scripts/run_translators.sh <pair_folder> <exp_number>` — see
[Using PWC4.5](03-using-pwc45.md).
