"""Reference Python PWC4.5 with an optional MAGNITUDE-AWARE relation, to test
whether adding magnitude sensitivity flips the losses without hurting the wins.

Numeric relation options at each node (chosen per attribute by gain ratio):
  - 'sign' 3-way split: min / eq / max  (the original PWC4.5 relation)
  - 'magnitude' binary split on the scale-invariant signed relative difference
        delta = (a - b) / (|a| + |b| + eps)  in [-1, 1]
    at a learned threshold theta:  delta <= theta  vs  delta > theta.
When magnitude=True the node evaluates BOTH and keeps whichever maximises gain
ratio, so magnitude is used only when it helps. magnitude=False reproduces the
original method (sanity baseline).

Consumes the pair representation from pair_arff_loader / pwccp_data: A, B arrays.
"""
import numpy as np

EPS = 1e-9

def _entropy(counts):
    tot = counts.sum()
    if tot <= 0:
        return 0.0
    p = counts[counts > 0] / tot
    return float(-(p * np.log2(p)).sum())

class Node:
    __slots__ = ("kind", "attr", "theta", "children", "pred", "dist")

class PWC45Py:
    def __init__(self, magnitude=False, mag_kind="rel", min_leaf=5, max_depth=12, n_thresh=16):
        self.magnitude = magnitude
        self.mag_kind = mag_kind   # 'rel' = scale-invariant delta; 'raw' = a-b
        self.min_leaf = min_leaf
        self.max_depth = max_depth
        self.n_thresh = n_thresh

    def fit(self, split):
        self.A = np.asarray(split["A"], float)
        self.B = np.asarray(split["B"], float)
        self.y = np.asarray(split["y"], int)
        self.K = int(self.y.max()) + 1
        self.m = self.A.shape[1]
        self.root = self._build(np.arange(len(self.y)), 0)
        return self

    def _dist(self, idx):
        return np.bincount(self.y[idx], minlength=self.K).astype(float)

    def _sign_rel(self, idx, j):
        d = self.A[idx, j] - self.B[idx, j]
        r = np.ones(len(idx), int)          # eq
        r[d < 0] = 0                         # min
        r[d > 0] = 2                         # max
        return r

    def _delta(self, idx, j):
        a, b = self.A[idx, j], self.B[idx, j]
        if self.mag_kind == "raw":
            return a - b
        return (a - b) / (np.abs(a) + np.abs(b) + EPS)

    def _gain_ratio(self, parent_dist, parts):
        total = parent_dist.sum()
        if total <= 0:
            return 0.0
        parent_info = _entropy(parent_dist)
        child_info = 0.0
        split_info = 0.0
        for c in parts:
            t = c.sum()
            if t <= 0:
                continue
            child_info += (t / total) * _entropy(c)
            p = t / total
            split_info -= p * np.log2(p)
        gain = parent_info - child_info
        if gain <= 0 or split_info <= 0:
            return 0.0
        return gain / split_info

    def _build(self, idx, depth):
        node = Node()
        dist = self._dist(idx)
        node.dist = dist
        if len(idx) < self.min_leaf or (dist > 0).sum() <= 1 or depth >= self.max_depth:
            node.kind = "leaf"; node.pred = int(dist.argmax()); return node

        best = (0.0, None)   # (gain_ratio, spec)
        for j in range(self.m):
            # sign 3-way
            r = self._sign_rel(idx, j)
            parts = [np.bincount(self.y[idx][r == k], minlength=self.K).astype(float) for k in range(3)]
            gr = self._gain_ratio(dist, parts)
            if gr > best[0]:
                best = (gr, ("sign", j, None))
            # magnitude binary on delta
            if self.magnitude:
                delta = self._delta(idx, j)
                order = np.unique(delta)
                if len(order) > 1:
                    cand = order[:-1] + np.diff(order) / 2
                    if len(cand) > self.n_thresh:
                        cand = np.quantile(cand, np.linspace(0, 1, self.n_thresh))
                    yv = self.y[idx]
                    for theta in cand:
                        left = delta <= theta
                        parts = [np.bincount(yv[left], minlength=self.K).astype(float),
                                 np.bincount(yv[~left], minlength=self.K).astype(float)]
                        gr = self._gain_ratio(dist, parts)
                        if gr > best[0]:
                            best = (gr, ("mag", j, float(theta)))

        if best[1] is None:
            node.kind = "leaf"; node.pred = int(dist.argmax()); return node

        kind, j, theta = best[1]
        node.kind = kind; node.attr = j; node.theta = theta
        if kind == "sign":
            r = self._sign_rel(idx, j)
            node.children = [self._build(idx[r == k], depth + 1) if (r == k).any()
                             else self._leaf(dist) for k in range(3)]
        else:
            delta = self._delta(idx, j)
            left = delta <= theta
            node.children = [self._build(idx[left], depth + 1) if left.any() else self._leaf(dist),
                             self._build(idx[~left], depth + 1) if (~left).any() else self._leaf(dist)]
        return node

    def _leaf(self, dist):
        n = Node(); n.kind = "leaf"; n.pred = int(dist.argmax()); n.dist = dist; return n

    def _predict_one(self, a, b, node):
        while node.kind != "leaf":
            j = node.attr
            if node.kind == "sign":
                d = a[j] - b[j]; k = 0 if d < 0 else (2 if d > 0 else 1)
                node = node.children[k]
            else:
                delta = (a[j] - b[j]) if self.mag_kind == "raw" else \
                        (a[j] - b[j]) / (abs(a[j]) + abs(b[j]) + EPS)
                node = node.children[0] if delta <= node.theta else node.children[1]
        return node.pred

    def accuracy(self, split):
        A = np.asarray(split["A"], float); B = np.asarray(split["B"], float)
        y = np.asarray(split["y"], int)
        pred = np.array([self._predict_one(A[i], B[i], self.root) for i in range(len(y))])
        return float((pred == y).mean())
