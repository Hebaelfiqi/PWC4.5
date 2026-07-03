package weka.classifiers.trees;

import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * PWC45 — a WEKA classifier for Pairwise Comparative Classification Problems
 * (PWCCP), extending the idea of C4.5 to split on the <i>relationship</i>
 * between the two members of a pair rather than on a value threshold.
 *
 * <p><b>Input representation (pair-as-single-instance).</b> Each instance is one
 * pair: the first {@code n} numeric attributes are p1's features, the next
 * {@code n} are p2's features (same order), and the nominal class is the class
 * of p1 (p2 is the other class). This makes the pairing invisible to WEKA —
 * cross-validation folds can never split a pair.
 *
 * <p>For each numeric feature i, the within-pair relation
 * R(V_i(p1), {V_i(p1),V_i(p2)}) is <i>min</i> if p1&lt;p2, <i>max</i> if
 * p1&gt;p2, else <i>eq</i>. For each nominal feature the relation is
 * <i>eq</i> / <i>neq</i> (values equal / not equal). The tree splits on the
 * relation that maximises the C4.5 gain ratio.
 *
 * <p><b>Status:</b> functional clean-room implementation of the relational-split
 * core (unpruned; C4.5-style gain-ratio selection). It is not yet a line-for-line
 * port of the reference {@code PWC45.java} (no error-based pruning, single
 * relation family). Sufficient for use in the WEKA Explorer/Experimenter and for
 * the benchmark comparisons; see research/PLAN.md.
 */
public class PWC45 extends AbstractClassifier {

  private static final long serialVersionUID = 1L;

  /** Minimum instances at a node to consider splitting. */
  protected int m_MinInstances = 2;
  /** Maximum tree depth (0 = unlimited). */
  protected int m_MaxDepth = 0;

  protected Node m_Root;
  protected int m_NumClasses;
  protected int m_NumPairAttrs;   // n = (numAttributes-1)/2
  protected boolean[] m_Nominal;  // per pair-attribute: nominal relation?
  protected String[] m_AttrNames; // per pair-attribute, for readable output

  /** A tree node: leaf (m_Split < 0) or internal split on feature m_Split. */
  protected static class Node {
    int m_Split = -1;             // feature index [0, n) or -1 for leaf
    Node m_Min, m_Eq, m_Max;      // children by relation
    double[] m_Dist;              // class distribution (leaf or fallback)
  }

  @Override
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.setMinimumNumberInstances(2);
    return result;
  }

  @Override
  public void buildClassifier(Instances data) throws Exception {
    getCapabilities().testWithFail(data);
    data = new Instances(data);
    data.deleteWithMissingClass();

    int nAttr = data.numAttributes() - 1;      // exclude class
    if (nAttr % 2 != 0) {
      throw new Exception("PWC45 expects an even number of predictor "
          + "attributes (p1's n features followed by p2's n features); got "
          + nAttr + ". Use research/tools/make_pair_arff.py to build the input.");
    }
    m_NumPairAttrs = nAttr / 2;
    m_NumClasses = data.numClasses();

    m_Nominal = new boolean[m_NumPairAttrs];
    m_AttrNames = new String[m_NumPairAttrs];
    for (int f = 0; f < m_NumPairAttrs; f++) {
      boolean n1 = data.attribute(f).isNominal();
      boolean n2 = data.attribute(f + m_NumPairAttrs).isNominal();
      if (n1 != n2) {
        throw new Exception("Attribute type mismatch between pair sides at "
            + data.attribute(f).name() + " / "
            + data.attribute(f + m_NumPairAttrs).name()
            + ": p1 and p2 must carry the same feature types in the same order.");
      }
      m_Nominal[f] = n1;
      String name = data.attribute(f).name();
      m_AttrNames[f] = name.startsWith("p1_") ? name.substring(3) : name;
    }

    List<Instance> rows = new ArrayList<Instance>();
    for (int i = 0; i < data.numInstances(); i++) rows.add(data.instance(i));
    m_Root = build(rows, data, 0);
  }

  /**
   * Relation of feature f for instance inst.
   * Numeric: 0=min (p1&lt;p2), 1=eq, 2=max.
   * Nominal: 0=neq (values differ), 1=eq (string values equal); 2 unused.
   */
  protected int relation(Instance inst, int f) {
    if (m_Nominal[f]) {
      String a = inst.stringValue(f);
      String b = inst.stringValue(f + m_NumPairAttrs);
      return a.equals(b) ? 1 : 0;
    }
    double a = inst.value(f);
    double b = inst.value(f + m_NumPairAttrs);
    if (a < b) return 0;
    if (a > b) return 2;
    return 1;
  }

  protected double[] classDist(List<Instance> rows) {
    double[] d = new double[m_NumClasses];
    for (Instance in : rows) d[(int) in.classValue()] += in.weight();
    return d;
  }

  protected Node build(List<Instance> rows, Instances header, int depth) {
    Node node = new Node();
    node.m_Dist = classDist(rows);

    if (rows.size() < m_MinInstances || isPure(node.m_Dist)
        || (m_MaxDepth > 0 && depth >= m_MaxDepth)) {
      return node;                              // leaf
    }

    int best = -1;
    double bestGainRatio = 0;
    double parentInfo = entropy(node.m_Dist);
    double total = Utils.sum(node.m_Dist);

    for (int f = 0; f < m_NumPairAttrs; f++) {
      double[][] parts = new double[3][m_NumClasses];
      double[] partTot = new double[3];
      for (Instance in : rows) {
        int r = relation(in, f);
        parts[r][(int) in.classValue()] += in.weight();
        partTot[r] += in.weight();
      }
      double childInfo = 0, splitInfo = 0;
      for (int r = 0; r < 3; r++) {
        if (partTot[r] == 0) continue;
        childInfo += (partTot[r] / total) * entropy(parts[r]);
        double p = partTot[r] / total;
        splitInfo -= p * Utils.log2(p);
      }
      double gain = parentInfo - childInfo;
      if (gain <= 0 || splitInfo <= 0) continue;
      double gainRatio = gain / splitInfo;
      if (gainRatio > bestGainRatio) {
        bestGainRatio = gainRatio;
        best = f;
      }
    }

    if (best < 0) return node;                  // no useful split -> leaf

    List<Instance> mn = new ArrayList<Instance>();
    List<Instance> eq = new ArrayList<Instance>();
    List<Instance> mx = new ArrayList<Instance>();
    for (Instance in : rows) {
      int r = relation(in, best);
      (r == 0 ? mn : r == 1 ? eq : mx).add(in);
    }
    node.m_Split = best;
    node.m_Min = mn.isEmpty() ? leaf(node.m_Dist) : build(mn, header, depth + 1);
    node.m_Eq  = eq.isEmpty() ? leaf(node.m_Dist) : build(eq, header, depth + 1);
    node.m_Max = mx.isEmpty() ? leaf(node.m_Dist) : build(mx, header, depth + 1);
    return node;
  }

  protected Node leaf(double[] dist) {
    Node n = new Node();
    n.m_Dist = dist.clone();
    return n;
  }

  protected boolean isPure(double[] dist) {
    int nonzero = 0;
    for (double v : dist) if (v > 0) nonzero++;
    return nonzero <= 1;
  }

  protected double entropy(double[] dist) {
    double total = Utils.sum(dist);
    if (total <= 0) return 0;
    double e = 0;
    for (double v : dist) {
      if (v > 0) e -= (v / total) * Utils.log2(v / total);
    }
    return e;
  }

  @Override
  public double[] distributionForInstance(Instance inst) {
    Node n = m_Root;
    while (n.m_Split >= 0) {
      int r = relation(inst, n.m_Split);
      n = (r == 0 ? n.m_Min : r == 1 ? n.m_Eq : n.m_Max);
    }
    double[] d = n.m_Dist.clone();
    double s = Utils.sum(d);
    if (s > 0) Utils.normalize(d, s);
    else for (int i = 0; i < d.length; i++) d[i] = 1.0 / d.length;
    return d;
  }

  @Override
  public String toString() {
    if (m_Root == null) return "PWC45: no model built yet.";
    StringBuilder sb = new StringBuilder("PWC45 relational decision tree\n\n");
    print(m_Root, sb, "");
    return sb.toString();
  }

  private void print(Node n, StringBuilder sb, String indent) {
    if (n.m_Split < 0) {
      sb.append(" : class ").append(Utils.maxIndex(n.m_Dist)).append("\n");
      return;
    }
    boolean nom = m_Nominal[n.m_Split];
    String[] rel = nom ? new String[]{"neq", "eq"} : new String[]{"min", "eq", "max"};
    Node[] kids = nom ? new Node[]{n.m_Min, n.m_Eq} : new Node[]{n.m_Min, n.m_Eq, n.m_Max};
    String name = (m_AttrNames != null && m_AttrNames[n.m_Split] != null)
        ? m_AttrNames[n.m_Split] : ("f" + n.m_Split);
    sb.append("\n");
    for (int i = 0; i < kids.length; i++) {
      sb.append(indent).append("R(").append(name).append(") = ").append(rel[i]);
      print(kids[i], sb, indent + "|  ");
    }
  }

  public void setMinInstances(int v) { m_MinInstances = v; }
  public int getMinInstances() { return m_MinInstances; }
  public void setMaxDepth(int v) { m_MaxDepth = v; }
  public int getMaxDepth() { return m_MaxDepth; }

  public static void main(String[] args) {
    runClassifier(new PWC45(), args);
  }
}
