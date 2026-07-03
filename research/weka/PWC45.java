package weka.classifiers.trees;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.j48.Stats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * PWC45 — a WEKA classifier for Pairwise Comparative Classification Problems
 * (PWCCP), extending the idea of C4.5 to split on the <i>relationship</i>
 * between the two members of a pair rather than on a value threshold.
 *
 * <p><b>Input representation (pair-as-single-instance).</b> Each instance is
 * one pair: the first {@code n} attributes are p1's features, the next
 * {@code n} are p2's features (same order and types), and the nominal class
 * is the class of p1 (p2 is the other class). This makes the pairing
 * invisible to WEKA — cross-validation folds can never split a pair.
 *
 * <p>For each numeric feature the within-pair relation
 * R(V(p1), {V(p1),V(p2)}) is <i>min</i> / <i>eq</i> / <i>max</i>; for each
 * nominal feature it is <i>eq</i> / <i>neq</i>. The tree splits on the
 * relation that maximises the C4.5 gain ratio, and is pruned with C4.5-style
 * pessimistic (confidence-based) subtree replacement.
 *
 * Options: -U (unpruned), -C confidence (default 0.25), -M min instances
 * per node (default 2), -depth max depth (0 = unlimited).
 */
public class PWC45 extends AbstractClassifier
    implements TechnicalInformationHandler {

  private static final long serialVersionUID = 2L;

  /** Minimum instances at a node to consider splitting. */
  protected int m_MinInstances = 2;
  /** Maximum tree depth (0 = unlimited). */
  protected int m_MaxDepth = 0;
  /** Use unpruned tree. */
  protected boolean m_Unpruned = false;
  /** Confidence factor for pessimistic pruning (as in C4.5/J48). */
  protected float m_CF = 0.25f;

  protected Node m_Root;
  protected int m_NumClasses;
  protected int m_NumPairAttrs;   // n = (numAttributes-1)/2
  protected boolean[] m_Nominal;  // per pair-attribute: nominal relation?
  protected String[] m_AttrNames; // per pair-attribute, for readable output

  /** A tree node: leaf (m_Split < 0) or internal split on feature m_Split. */
  protected static class Node implements java.io.Serializable {
    private static final long serialVersionUID = 2L;
    int m_Split = -1;             // feature index [0, n) or -1 for leaf
    Node m_Min, m_Eq, m_Max;      // children by relation
    double[] m_Dist;              // training class distribution
    boolean m_Empty = false;      // empty-partition fallback leaf
  }

  public String globalInfo() {
    return "PWC4.5: a decision tree for Pairwise Comparative Classification "
        + "Problems (PWCCP), extending C4.5 to split on the relationship "
        + "between the two members of a pair (numeric: min/eq/max; nominal: "
        + "eq/neq) instead of a value threshold. Expects pair-as-single-"
        + "instance data: p1's n features, then p2's n features (same order "
        + "and types), then the class of p1.\n\nFor more information see:\n\n"
        + getTechnicalInformation().toString();
  }

  @Override
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.AUTHOR, "Heba El-Fiqi and Eleni Petraki and Hussein A. Abbass");
    result.setValue(Field.TITLE, "Pairwise Comparative Classification for Translator Stylometric Analysis");
    result.setValue(Field.JOURNAL, "ACM Transactions on Asian and Low-Resource Language Information Processing");
    result.setValue(Field.YEAR, "2016");
    result.setValue(Field.VOLUME, "16");
    result.setValue(Field.NUMBER, "1");
    result.setValue(Field.PAGES, "Article 2");
    result.setValue(Field.URL, "https://doi.org/10.1145/2898997");
    return result;
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

  // ---------------------------------------------------------------- options

  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> v = new Vector<Option>(4);
    v.addElement(new Option("\tUse unpruned tree.", "U", 0, "-U"));
    v.addElement(new Option("\tConfidence factor for pruning (default 0.25).",
        "C", 1, "-C <confidence>"));
    v.addElement(new Option("\tMinimum instances to attempt a split (default 2).",
        "M", 1, "-M <min instances>"));
    v.addElement(new Option("\tMaximum tree depth, 0 = unlimited (default 0).",
        "depth", 1, "-depth <max depth>"));
    Enumeration<Option> sup = super.listOptions();
    while (sup.hasMoreElements()) v.addElement(sup.nextElement());
    return v.elements();
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    m_Unpruned = Utils.getFlag('U', options);
    String s = Utils.getOption('C', options);
    m_CF = s.length() > 0 ? Float.parseFloat(s) : 0.25f;
    s = Utils.getOption('M', options);
    m_MinInstances = s.length() > 0 ? Integer.parseInt(s) : 2;
    s = Utils.getOption("depth", options);
    m_MaxDepth = s.length() > 0 ? Integer.parseInt(s) : 0;
    super.setOptions(options);
    Utils.checkForRemainingOptions(options);
  }

  @Override
  public String[] getOptions() {
    List<String> opts = new ArrayList<String>();
    if (m_Unpruned) opts.add("-U");
    opts.add("-C"); opts.add("" + m_CF);
    opts.add("-M"); opts.add("" + m_MinInstances);
    opts.add("-depth"); opts.add("" + m_MaxDepth);
    Collections.addAll(opts, super.getOptions());
    return opts.toArray(new String[0]);
  }

  public void setUnpruned(boolean v) { m_Unpruned = v; }
  public boolean getUnpruned() { return m_Unpruned; }
  public String unprunedTipText() { return "Whether to skip pessimistic pruning."; }
  public void setConfidenceFactor(float v) { m_CF = v; }
  public float getConfidenceFactor() { return m_CF; }
  public String confidenceFactorTipText() {
    return "Confidence factor for pessimistic pruning (smaller = more pruning), as in C4.5/J48.";
  }
  public void setMinInstances(int v) { m_MinInstances = v; }
  public int getMinInstances() { return m_MinInstances; }
  public String minInstancesTipText() { return "Minimum instances at a node to attempt a split."; }
  public void setMaxDepth(int v) { m_MaxDepth = v; }
  public int getMaxDepth() { return m_MaxDepth; }
  public String maxDepthTipText() { return "Maximum tree depth (0 = unlimited)."; }

  // ------------------------------------------------------------------ build

  @Override
  public void buildClassifier(Instances data) throws Exception {
    getCapabilities().testWithFail(data);
    data = new Instances(data);
    data.deleteWithMissingClass();

    int nAttr = data.numAttributes() - 1;      // exclude class
    if (nAttr % 2 != 0) {
      throw new Exception("PWC45 expects an even number of predictor "
          + "attributes (p1's n features followed by p2's n features); got "
          + nAttr + ". Use the pair-as-instance converters to build the input.");
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
    m_Root = build(rows, 0);
    if (!m_Unpruned) prune(m_Root);
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

  protected Node build(List<Instance> rows, int depth) {
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
    node.m_Min = mn.isEmpty() ? emptyLeaf(node.m_Dist) : build(mn, depth + 1);
    node.m_Eq  = eq.isEmpty() ? emptyLeaf(node.m_Dist) : build(eq, depth + 1);
    node.m_Max = mx.isEmpty() ? emptyLeaf(node.m_Dist) : build(mx, depth + 1);
    return node;
  }

  /** Fallback leaf for an empty relation branch: predicts the parent's
   *  majority but carries no training mass (so pruning cannot double-count). */
  protected Node emptyLeaf(double[] parentDist) {
    Node n = new Node();
    n.m_Dist = parentDist.clone();
    n.m_Empty = true;
    return n;
  }

  // ----------------------------------------------------------------- prune

  /** Pessimistic error estimate for this node treated as a leaf (C4.5-style,
   *  via WEKA's own Stats.addErrs for exact parity with J48). */
  protected double leafErrorEstimate(Node n) {
    if (n.m_Empty) return 0;
    double N = Utils.sum(n.m_Dist);
    if (N <= 0) return 0;
    double e = N - n.m_Dist[Utils.maxIndex(n.m_Dist)];
    return e + Stats.addErrs(N, e, m_CF);
  }

  /** Bottom-up subtree replacement; returns the estimated error of the
   *  (possibly pruned) subtree rooted at n. */
  protected double prune(Node n) {
    if (n.m_Split < 0) return leafErrorEstimate(n);
    double subtree = 0;
    for (Node k : children(n)) subtree += prune(k);
    double asLeaf = leafErrorEstimate(n);
    if (asLeaf <= subtree + 0.1) {
      n.m_Split = -1;
      n.m_Min = n.m_Eq = n.m_Max = null;
      return asLeaf;
    }
    return subtree;
  }

  protected Node[] children(Node n) {
    return m_Nominal[n.m_Split]
        ? new Node[]{n.m_Min, n.m_Eq}
        : new Node[]{n.m_Min, n.m_Eq, n.m_Max};
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

  // --------------------------------------------------------------- predict

  @Override
  public double[] distributionForInstance(Instance inst) {
    Node n = m_Root;
    while (n.m_Split >= 0) {
      int r = relation(inst, n.m_Split);
      n = (r == 0 ? n.m_Min : r == 1 ? n.m_Eq : n.m_Max);
      if (n == null) break;                     // nominal node, impossible r=2
    }
    double[] d = (n != null ? n.m_Dist.clone() : m_Root.m_Dist.clone());
    double s = Utils.sum(d);
    if (s > 0) Utils.normalize(d, s);
    else for (int i = 0; i < d.length; i++) d[i] = 1.0 / d.length;
    return d;
  }

  // ---------------------------------------------------------------- output

  public int numLeaves(Node n) {
    if (n == null) n = m_Root;
    if (n.m_Split < 0) return 1;
    int c = 0;
    for (Node k : children(n)) c += numLeaves(k);
    return c;
  }

  public int treeSize(Node n) {
    if (n == null) n = m_Root;
    if (n.m_Split < 0) return 1;
    int c = 1;
    for (Node k : children(n)) c += treeSize(k);
    return c;
  }

  @Override
  public String toString() {
    if (m_Root == null) return "PWC45: no model built yet.";
    StringBuilder sb = new StringBuilder("PWC45 relational decision tree ")
        .append(m_Unpruned ? "(unpruned)" : "(pruned, CF=" + m_CF + ")")
        .append("\n");
    print(m_Root, sb, "");
    sb.append("\nNumber of leaves: ").append(numLeaves(null));
    sb.append("\nSize of the tree: ").append(treeSize(null)).append("\n");
    return sb.toString();
  }

  private void print(Node n, StringBuilder sb, String indent) {
    if (n.m_Split < 0) {
      sb.append(" : class ").append(Utils.maxIndex(n.m_Dist)).append("\n");
      return;
    }
    boolean nom = m_Nominal[n.m_Split];
    String[] rel = nom ? new String[]{"neq", "eq"} : new String[]{"min", "eq", "max"};
    Node[] kids = children(n);
    String name = (m_AttrNames != null && m_AttrNames[n.m_Split] != null)
        ? m_AttrNames[n.m_Split] : ("f" + n.m_Split);
    sb.append("\n");
    for (int i = 0; i < kids.length; i++) {
      sb.append(indent).append("R(").append(name).append(") = ").append(rel[i]);
      print(kids[i], sb, indent + "|  ");
    }
  }

  public static void main(String[] args) {
    runClassifier(new PWC45(), args);
  }
}
