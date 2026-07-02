# Sample output

Output files produced by the classifier on the datasets in
[`../benchmark-data/`](../benchmark-data/), kept as a reference for what a
run generates:

- `Pruned_<stem>_decision_rules.txt` / `Unpruned_<stem>_decision_rules.txt` —
  the learned decision tree printed as rules, with leaf counts and tree size.
- `pruned_out.csv` / `unpruned_out.csv` / `test_output.csv` —
  actual vs. predicted labels per instance.

A PWC4.5 run produces the same set of files for the pairwise datasets; the
original published outputs of the Asad–Daryabadi experiment 1 (run 1) are in
[`../../data/translators_data/asad_daryabadi/run_1/`](../../data/translators_data/asad_daryabadi/run_1/).
