# Using PWC4.5

> The commands on this page reproduce the experiments reported in the
> publications ([citations](../README.md#citing-this-work)). The scripts in
> [`../scripts/`](../scripts/) wrap them for Linux/macOS.

## Command

```
PWC45 [-f filestem] [-u] [-p] [-o] [-r seed] [-ip input_datapath] [-op output_path] [-b]
```

| Flag | Description |
|------|-------------|
| `-f filestem` | Specify the filename stem (default `DF`) |
| `-u` | Evaluate trees produced on unseen cases in `filestem.test` |
| `-p` | Unpruned tree |
| `-o` | Ordered data that need randomization for class balance |
| `-r seed` | Specify the seed number for the random generator |
| `-ip input_datapath` | Specify the data input path |
| `-op output_path` | Specify the output path for the generated files |
| `-b` | Generate all possible branches for each relationship-based split |

The commands below are as published on the website (Windows classpath
separator `;`). On Linux/macOS replace `bin;lib/cli.jar` with `bin:lib/cli.jar`
— or simply use `scripts/run_synthetic.sh` and `scripts/run_translators.sh`.

## Output files

Each run prints the test-set accuracy to stdout and writes (to `-op`, or to
the input folder if `-op` is not given):

- `Pruned_<stem>_decision_rules.txt` / `Unpruned_<stem>_decision_rules.txt` —
  the learned tree as rules, with leaf count and tree size
- `<stem>_pruned_out.csv` / `<stem>_unpruned_out.csv`, `output_of_<stem>.csv` —
  actual vs. predicted labels

## Examples

### 5D data

```sh
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.0 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.01 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.025 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.05 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.1 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.15 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.2 -u
java -cp bin;lib/cli.jar PWC45 -ip data//5d_data//1st_exp// -f 5D_Noise_0.25 -u
```

### 2D data

```sh
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.0 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.01 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.025 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.05 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.1 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.15 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.2 -u
java -cp bin;lib/cli.jar PWC45 -ip data//2d_data//1st_exp// -f 2D_Noise_0.25 -u
```

The same commands apply to the other experiment folders
(`1st_exp` … `10th_exp`) under `data/2d_data/` and `data/5d_data/`.

### Translator stylometry — Asad vs. Daryabadi, experiment 1

To reproduce the results of exp1 of translators Asad and Daryabadi: create
output folders `run_1`, `run_2`, …, `run_10` under
`data/translators_data/asad_daryabadi/`, then call PWC45 as follows
(or run `scripts/run_translators.sh asad_daryabadi 1`, which creates the
folders and uses the same seeds):

```sh
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_1// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 42
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_2// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 61
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_3// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 13
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_4// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 3
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_5// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 82
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_6// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 27
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_7// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 19
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_8// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 47
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_9// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 93
java -cp bin;lib/cli.jar PWC45 -ip data//translators_data//asad_daryabadi// -op data//translators_data//asad_daryabadi//run_10// -f M3_M4_Asad_Daryabadi_Exp_1 -u -o -b -r 2
```

The original usage guide shipped with the source release is available as
[PWC4.5_guide.docx](PWC4.5_guide.docx).
