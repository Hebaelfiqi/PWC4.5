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
    __slots__ = ("kind", "attr", "theta", "agg", "children", "pred", "dist")

class PWC45Py:
    def __init__(self, magnitude=False, mag_kind="std", min_leaf=5, max_depth=12,
                 n_thresh=16, deadzone=False, value_split=False,
                 value_aggs=(0, 1, 2), prefer_rel=0.0, numeric_penalty=False):
        # value_split: also allow C4.5-style thresholds on a SYMMETRIC pair
        # aggregate, which keeps the pair together and is orientation-invariant.
        # value_aggs selects which aggregates are tried: 0=min, 1=max, 2=mean.
        #   (0,)=min-only  (2,)=mean-only  (1,)=max-only  (0,1,2)=any (fit)
        # prefer_rel: Occam margin — use a value/magnitude split only if it beats
        #   the best relational split by this fraction (0 = no bias).
        self.value_split = value_split
        self.value_aggs = value_aggs
        self.prefer_rel = prefer_rel
        self.numeric_penalty = numeric_penalty  # C4.5-style log2(#thresholds)/N
        self.magnitude = magnitude
        # mag_kind: 'rel'=(a-b)/(|a|+|b|) [old/flawed]; 'raw'=a-b;
        #           'std'=(a-b)/std_j; 'mad'=(a-b)/MAD_j; 'quantile'=q(a)-q(b)
        self.mag_kind = mag_kind
        self.deadzone = deadzone   # if True: symmetric 3-way {much-less,similar,much-greater}
        self.min_leaf = min_leaf
        self.max_depth = max_depth
        self.n_thresh = n_thresh

    def fit(self, split):
        self.A = np.asarray(split["A"], float)
        self.B = np.asarray(split["B"], float)
        self.y = np.asarray(split["y"], int)
        self.K = int(self.y.max()) + 1
        self.m = self.A.shape[1]
        # per-attribute scales / CDFs computed from TRAINING data only
        d = self.A - self.B
        self._std = d.std(axis=0) + EPS
        self._mad = np.median(np.abs(d - np.median(d, axis=0)), axis=0) * 1.4826 + EPS
        if self.mag_kind == "quantile":
            vals = np.concatenate([self.A, self.B], axis=0)
            self._sorted = [np.sort(vals[:, j]) for j in range(self.m)]
        self.root = self._build(np.arange(len(self.y)), 0)
        return self

    def _mag(self, a, b, j):
        """Scaled signed difference for attribute j (array or scalar)."""
        k = self.mag_kind
        if k == "raw":
            return a - b
        if k == "rel":
            return (a - b) / (np.abs(a) + np.abs(b) + EPS)
        if k == "std":
            return (a - b) / self._std[j]
        if k == "mad":
            return (a - b) / self._mad[j]
        if k == "quantile":
            s = self._sorted[j]; n = len(s)
            return (np.searchsorted(s, a) - np.searchsorted(s, b)) / n
        raise ValueError(k)

    def _dist(self, idx):
        return np.bincount(self.y[idx], minlength=self.K).astype(float)

    def _sign_rel(self, idx, j):
        d = self.A[idx, j] - self.B[idx, j]
        r = np.ones(len(idx), int)          # eq
        r[d < 0] = 0                         # min
        r[d > 0] = 2                         # max
        return r

    def _delta(self, idx, j):
        return self._mag(self.A[idx, j], self.B[idx, j], j)

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

        yv = self.y[idx]
        # Pass 1 — relational (sign) splits; the default and the Occam reference
        best = (0.0, None)
        best_rel = 0.0
        for j in range(self.m):
            r = self._sign_rel(idx, j)
            parts = [np.bincount(yv[r == k], minlength=self.K).astype(float) for k in range(3)]
            gr = self._gain_ratio(dist, parts)
            if gr > best_rel:
                best_rel = gr
            if gr > best[0]:
                best = (gr, ("sign", j, None))

        # Pass 2 — value/magnitude splits, used ONLY if they beat the best
        # relational split by the Occam margin (bias toward the simpler,
        # scale-invariant relation; avoids overfitting relational datasets)
        if self.value_split or self.magnitude:
            floor = best_rel * (1.0 + self.prefer_rel)
            for j in range(self.m):
                if self.value_split:
                    a, b = self.A[idx, j], self.B[idx, j]
                    allagg = {0: np.minimum(a, b), 1: np.maximum(a, b), 2: (a + b) / 2}
                    for aid in self.value_aggs:
                        g = allagg[aid]
                        order = np.unique(g)
                        if len(order) <= 1:
                            continue
                        cand = order[:-1] + np.diff(order) / 2
                        if len(cand) > self.n_thresh:
                            cand = np.quantile(cand, np.linspace(0, 1, self.n_thresh))
                        pen = (np.log2(max(len(cand), 2)) / len(idx)
                               if self.numeric_penalty else 0.0)
                        for theta in cand:
                            left = g <= theta
                            parts = [np.bincount(yv[left], minlength=self.K).astype(float),
                                     np.bincount(yv[~left], minlength=self.K).astype(float)]
                            gr = self._gain_ratio(dist, parts) - pen
                            if gr > best[0] and gr > floor:
                                best = (gr, ("val", j, aid, float(theta)))
                if self.magnitude:
                    delta = self._delta(idx, j)
                    if not self.deadzone:
                        order = np.unique(delta)
                        if len(order) > 1:
                            cand = order[:-1] + np.diff(order) / 2
                            if len(cand) > self.n_thresh:
                                cand = np.quantile(cand, np.linspace(0, 1, self.n_thresh))
                            for theta in cand:
                                left = delta <= theta
                                parts = [np.bincount(yv[left], minlength=self.K).astype(float),
                                         np.bincount(yv[~left], minlength=self.K).astype(float)]
                                gr = self._gain_ratio(dist, parts)
                                if gr > best[0] and gr > floor:
                                    best = (gr, ("mag", j, float(theta)))
                    else:
                        ad = np.abs(delta)
                        pos = np.unique(ad[ad > 0])
                        if len(pos) > 1:
                            cand = np.quantile(pos, np.linspace(0.1, 0.9, self.n_thresh))
                            for theta in np.unique(cand):
                                r = np.where(delta < -theta, 0, np.where(delta > theta, 2, 1))
                                parts = [np.bincount(yv[r == k], minlength=self.K).astype(float)
                                         for k in range(3)]
                                gr = self._gain_ratio(dist, parts)
                                if gr > best[0] and gr > floor:
                                    best = (gr, ("mag3", j, float(theta)))

        if best[1] is None:
            node.kind = "leaf"; node.pred = int(dist.argmax()); return node

        spec = best[1]
        kind = spec[0]; j = spec[1]
        node.kind = kind; node.attr = j
        if kind == "val":
            node.agg = spec[2]; node.theta = spec[3]
            a, b = self.A[idx, j], self.B[idx, j]
            g = (np.minimum(a, b) if node.agg == 0 else
                 np.maximum(a, b) if node.agg == 1 else (a + b) / 2)
            left = g <= node.theta
            node.children = [self._build(idx[left], depth + 1) if left.any() else self._leaf(dist),
                             self._build(idx[~left], depth + 1) if (~left).any() else self._leaf(dist)]
            return node
        theta = spec[2]; node.theta = theta
        if kind == "sign":
            r = self._sign_rel(idx, j)
            node.children = [self._build(idx[r == k], depth + 1) if (r == k).any()
                             else self._leaf(dist) for k in range(3)]
        elif kind == "mag3":
            delta = self._delta(idx, j)
            r = np.where(delta < -theta, 0, np.where(delta > theta, 2, 1))
            node.children = [self._build(idx[r == k], depth + 1) if (r == k).any()
                             else self._leaf(dist) for k in range(3)]
        else:                                          # 'mag' binary
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
            elif node.kind == "val":
                g = (min(a[j], b[j]) if node.agg == 0 else
                     max(a[j], b[j]) if node.agg == 1 else (a[j] + b[j]) / 2)
                node = node.children[0] if g <= node.theta else node.children[1]
            elif node.kind == "mag3":
                delta = self._mag(a[j], b[j], j)
                k = 0 if delta < -node.theta else (2 if delta > node.theta else 1)
                node = node.children[k]
            else:
                delta = self._mag(a[j], b[j], j)
                node = node.children[0] if delta <= node.theta else node.children[1]
        return node.pred

    def accuracy(self, split):
        A = np.asarray(split["A"], float); B = np.asarray(split["B"], float)
        y = np.asarray(split["y"], int)
        pred = np.array([self._predict_one(A[i], B[i], self.root) for i in range(len(y))])
        return float((pred == y).mean())
