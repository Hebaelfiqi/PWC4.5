"""Baseline pairwise/ranking models for PWCCP.

All expose the same interface:
    fit(data_train) ; predict_pair_accuracy(data_split) -> float

The task is ordered-pair assignment; accuracy = fraction of test pairs whose
orientation label is predicted correctly.
"""
import numpy as np
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
import lightgbm as lgb


class PairwiseSVM:
    """SVM on standardized difference vectors phi(a) - phi(b)."""
    def __init__(self, kernel="rbf", C=1.0, gamma="scale"):
        self.kernel, self.C, self.gamma = kernel, C, gamma

    def fit(self, tr):
        self.scaler = StandardScaler().fit(tr["diff"])
        X = self.scaler.transform(tr["diff"])
        self.clf = SVC(kernel=self.kernel, C=self.C, gamma=self.gamma)
        self.clf.fit(X, tr["y"])
        return self

    def accuracy(self, sp):
        X = self.scaler.transform(sp["diff"])
        return float((self.clf.predict(X) == sp["y"]).mean())


class RankNet:
    """Minimal RankNet: a 2-layer MLP scorer s(x); the pair (a,b) is scored by
    sigma(s(a) - s(b)) and trained with pairwise cross-entropy. Pure numpy."""
    def __init__(self, hidden=16, lr=0.05, epochs=300, seed=0):
        self.hidden, self.lr, self.epochs, self.seed = hidden, lr, epochs, seed

    def _score(self, X):
        H = np.tanh(X @ self.W1 + self.b1)
        return (H @ self.W2 + self.b2).ravel(), H

    def fit(self, tr):
        self.scaler = StandardScaler().fit(np.vstack([tr["A"], tr["B"]]))
        A = self.scaler.transform(tr["A"]); B = self.scaler.transform(tr["B"])
        y = tr["y"].astype(float)
        rng = np.random.default_rng(self.seed)
        d = A.shape[1]
        self.W1 = rng.normal(0, 1 / np.sqrt(d), (d, self.hidden))
        self.b1 = np.zeros(self.hidden)
        self.W2 = rng.normal(0, 1 / np.sqrt(self.hidden), (self.hidden, 1))
        self.b2 = np.zeros(1)
        m = len(y)
        for _ in range(self.epochs):
            sa, Ha = self._score(A)
            sb, Hb = self._score(B)
            p = 1 / (1 + np.exp(-(sa - sb)))         # P(a ranked above b)
            g = (p - y) / m                          # dL/d(sa-sb)
            # gradients w.r.t. output layer
            gW2 = (Ha - Hb).T @ g[:, None]
            gb2 = np.array([g.sum()])
            # backprop into hidden (tanh')
            da = (g[:, None] @ self.W2.T) * (1 - Ha ** 2)
            db = (-g[:, None] @ self.W2.T) * (1 - Hb ** 2)
            gW1 = A.T @ da + B.T @ db
            gb1 = da.sum(0) + db.sum(0)
            self.W2 -= self.lr * gW2; self.b2 -= self.lr * gb2
            self.W1 -= self.lr * gW1; self.b1 -= self.lr * gb1
        return self

    def accuracy(self, sp):
        A = self.scaler.transform(sp["A"]); B = self.scaler.transform(sp["B"])
        sa, _ = self._score(A); sb, _ = self._score(B)
        pred = (sa > sb).astype(int)
        return float((pred == sp["y"]).mean())


class LambdaMART:
    """LightGBM LambdaMART ranker. Each pair is a group of 2 items; the item
    that should be 'ClassA' gets relevance 1, the other 0. Predict the pair by
    which item scores higher."""
    def __init__(self, n_estimators=100, num_leaves=7, lr=0.1, seed=0):
        self.params = dict(objective="lambdarank", n_estimators=n_estimators,
                           num_leaves=num_leaves, learning_rate=lr,
                           min_child_samples=5, random_state=seed, verbosity=-1)

    @staticmethod
    def _items(sp):
        # interleave a,b per pair -> item feature matrix, groups of 2
        A, B, y = sp["A"], sp["B"], sp["y"]
        X, rel, groups = [], [], []
        for i in range(len(y)):
            X.append(A[i]); X.append(B[i])
            # relevance: the 'ClassA' item gets 1
            rel += ([1, 0] if y[i] == 1 else [0, 1])
            groups.append(2)
        return np.array(X), np.array(rel), np.array(groups)

    def fit(self, tr):
        X, rel, grp = self._items(tr)
        self.model = lgb.LGBMRanker(**self.params)
        self.model.fit(X, rel, group=grp)
        return self

    def accuracy(self, sp):
        A, B, y = sp["A"], sp["B"], sp["y"]
        sa = self.model.predict(A); sb = self.model.predict(B)
        pred = (sa > sb).astype(int)
        return float((pred == y).mean())


def all_models(seed=0):
    return {
        "PairwiseSVM": PairwiseSVM(kernel="rbf"),
        "RankNet": RankNet(seed=seed),
        "LambdaMART": LambdaMART(seed=seed),
    }
