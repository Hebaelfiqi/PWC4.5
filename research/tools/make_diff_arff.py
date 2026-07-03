"""GBT baseline data: convert a numeric pair-as-instance ARFF into a
difference-vector ARFF (phi(p1) - phi(p2), same class) — the Gradient-Based
Transformation baseline from the TALLIP 2016 article. Numeric attributes only
(GBT is undefined for nominal features).

Usage: python make_diff_arff.py in_pair.arff out_diff.arff
"""
import sys

def main(inp, outp):
    attrs, data, in_data = [], [], False
    relation = "diff"
    for line in open(inp):
        s = line.strip()
        if not s:
            continue
        low = s.lower()
        if low.startswith("@relation"):
            relation = s.split()[1] + "_diff"
        elif low.startswith("@attribute"):
            attrs.append(s)
        elif low.startswith("@data"):
            in_data = True
        elif in_data:
            data.append(s.split(","))

    n = (len(attrs) - 1) // 2
    for a in attrs[:2 * n]:
        if "numeric" not in a.lower():
            sys.exit("make_diff_arff: numeric attributes only (GBT is "
                     "undefined for nominal features): " + a)
    names = [a.split()[1] for a in attrs[:n]]
    class_line = attrs[-1].replace("@attribute", "@attribute", 1)

    with open(outp, "w") as f:
        f.write(f"@relation {relation}\n\n")
        for name in names:
            clean = name[3:] if name.startswith("p1_") else name
            f.write(f"@attribute d_{clean} numeric\n")
        f.write(class_line + "\n\n@data\n")
        for row in data:
            diffs = [f"{float(row[i]) - float(row[i + n]):g}" for i in range(n)]
            f.write(",".join(diffs) + f",{row[-1]}\n")
    print(f"{outp}: {len(data)} rows, {n} difference attributes")

if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
