# Submitting the `pwc45` WEKA package

The classifier is packaged per the WEKA package-management spec. Two audiences:

## A. Users installing it now (unofficial)

1. Build the zip: `WEKA_JAR=/path/to/weka.jar research/weka/build_package.sh`
   → produces `dist/pwc45-1.1.0.zip`.
2. In WEKA GUI: **Tools → Package Manager → File/URL** (unofficial install),
   point at the zip. Or CLI:
   `java -cp weka.jar weka.core.WekaPackageManager -install-package dist/pwc45-1.1.0.zip`
3. `weka.classifiers.trees.PWC45` then appears in the Explorer/Experimenter.
   (On Java 9+, launch WEKA with `--add-opens java.base/java.lang=ALL-UNNAMED`.)

The GitHub release also hosts the zip, so the `PackageURL` in
`Description.props` resolves for direct download.

## B. Official listing in the WEKA package repository

Official packages are curated by the WEKA team at the University of Waikato.
Process (per WEKA docs — verify current instructions before submitting):

1. Host the built `pwc45-<version>.zip` at a stable URL (the GitHub release
   asset URL works). Ensure `Description.props` `PackageURL` points to it.
2. Email the WEKA maintainers (wekalist / the package-submission address in
   the current WEKA documentation) requesting addition to the central
   package list, including: package name, description, the zip URL, license,
   and a one-line summary.
3. On acceptance the package becomes installable by name
   (`weka.core.WekaPackageManager -install-package pwc45`).

Pre-submission checklist:
- [ ] `MAINTAINER_EMAIL_TODO` and support email filled in `Description.props`.
- [ ] Version bumped and matching the git tag.
- [ ] Zip builds cleanly and installs into a fresh WEKA.
- [ ] Classifier runs in Explorer + Experimenter on an example pair-ARFF.
- [ ] README + example datasets included under `doc/`.
- [ ] Citation (TALLIP 2016 + Zenodo software DOI) present in globalInfo and
      Description.

Fallback: the unofficial-install route (A) is fully sufficient for the
SoftwareX paper's reproducibility requirement; official listing is a
visibility bonus, not a blocker.
