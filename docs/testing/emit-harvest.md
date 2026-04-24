# EMIT Harvest — Collecting EMIT Tests from Studio Projects

## 1. Motivation

A GitLab instance hosting 4,000–5,000 Studio projects represents a vast corpus of
real-world Legend models. These projects collectively exercise a wide range of Legend Engine
features — relational mappings, service definitions, test suites, generation specifications,
external formats, and more.

The EMIT framework (see `emit.md`) needs a rich catalog of test models. Rather than hand-authoring
every example from scratch, we can **harvest** models from the existing Studio project population.
This is more efficient and produces tests grounded in real usage patterns.

However, harvesting at scale introduces challenges:
- **Redundancy**: Many projects use the same feature combinations. We don't need 200 tests for
  "simple service with relational mapping" — we need the best one.
- **Complexity**: Production projects are often large and entangled. A good EMIT test should be
  small, focused, and self-contained.
- **Simplification**: Studio projects often have large, organization-specific models. EMIT tests
  should be small, focused, and use generic naming.
- **Placement**: Each harvested test must land in the right `legend-engine` module, near the
  feature code it exercises.

This document describes a process to scan the project population, classify models by feature
usage, select the best candidates, translate them to the EMIT format, and place them in the
codebase.

---

## 2. Overview

The harvest process has five stages:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   DISCOVER   │────▶│   CLASSIFY   │────▶│    SELECT    │────▶│  TRANSLATE   │────▶│    PLACE     │
│              │     │              │     │              │     │              │     │              │
│ Scan GitLab  │     │ Feature      │     │ Pick best    │     │ Simplify     │     │ Module &     │
│ for Studio   │     │ fingerprint  │     │ candidate    │     │ packages,    │     │ emit.yaml    │
│ projects     │     │ each project │     │ per feature  │     │ restructure  │     │ generation   │
│              │     │              │     │ combination  │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
```

The process is designed to be run as a **batch tool** (not a continuously running service). It
can be re-run periodically to discover new feature combinations or refresh the catalog with
better examples.

---

## 3. Stage 1: Discover

### 3.1 Scanning GitLab

Use the GitLab API to enumerate all projects in the instance. For each project, determine
whether it is a Studio project by checking for the presence of `.pure` files in Legend
grammar under the project's source directory.

### 3.2 Model Extraction

For each identified Studio project, extract the `.pure` files from the default branch.
Studio projects store their models as `.pure` files written in Legend Engine Grammar — the
same grammar format used by EMIT tests.

Parse the files into `PureModelContextData` using `PureGrammarParser`.
If the project has declared dependencies (other versioned Studio projects), resolve those
transitively to understand the full compilation context, but keep the primary model files
separate from dependency files.

### 3.3 Compilation Check

Attempt to compile each project's `PureModelContextData` (with dependencies). Projects that
fail to compile are excluded from harvesting — they are not suitable as examples.

Record basic statistics: total element count, compilation time.

---

## 4. Stage 2: Classify

### 4.1 Feature Fingerprinting

For each successfully compiled project, compute a **feature fingerprint** — a set of tags
describing the Legend features exercised by the model. This uses the same taxonomy defined
in `emit.md` §7.2.

The fingerprint is derived by inspecting the `PureModelContextData`:
- **Element types**: Which `classifierPath` values are present? (e.g., `meta::pure::metamodel::type::Class`,
  `meta::legend::service::metamodel::Service`, `meta::relational::metamodel::Database`)
- **Mapping types**: What kinds of class mappings exist? (relational, M2M, enumeration, etc.)
- **Store types**: What store connections are defined?
- **Test presence**: Do any `Testable` elements have `testSuites`?
- **Generation presence**: Is there a `GenerationSpecification`?

### 4.2 Complexity Scoring

Assign each project a complexity score based on:
- Total number of elements
- Depth of dependency chain
- Number of distinct feature tags
- Whether it uses advanced features (multi-execution services, custom connections, etc.)

Projects are bucketed into `basic`, `intermediate`, and `advanced`.

### 4.3 Output

The classification stage produces a **project manifest**: a JSON file mapping each project
to its feature fingerprint, complexity score, element count, and GitLab metadata (project ID,
URL, branch).

---

## 5. Stage 3: Select

### 5.1 Deduplication by Feature Fingerprint

Group projects by their feature fingerprint. For each distinct fingerprint, we want at most
one representative example.

### 5.2 Candidate Ranking

Within each fingerprint group, rank candidates by suitability as an EMIT test:

1. **Smallest element count** — simpler is better for test examples.
2. **Fewest dependencies** — self-contained models are preferable.
3. **Has tests** — projects with test suites are more valuable.
4. **Clean package structure** — fewer deeply nested packages are easier to understand.

The top-ranked candidate for each fingerprint is selected. The selection can be overridden
manually via a curated allowlist/blocklist.

### 5.3 Coverage Gap Analysis

Compare the set of selected feature fingerprints against the full EMIT feature taxonomy.
Report any feature tags or tag combinations that have no representation — these are
**coverage gaps** that may need hand-authored examples.

---

## 6. Stage 4: Translate

### 6.1 File Restructuring

Since Studio projects already store models as `.pure` files in Legend grammar — the same
format used by EMIT — no grammar conversion is needed. However, the source files must be
restructured to match EMIT directory conventions.

Studio projects may organize files differently than the EMIT convention (model, store, mapping,
service, etc. subdirectories). The translation stage reads the source `.pure` files, parses
them, and re-emits them into the EMIT directory structure — splitting or merging files as
needed to produce one file per concern.

### 6.2 Package Simplification

Production Studio projects often use organization-specific package hierarchies (e.g.,
`com::acme::finance::trading::model::Position`). For EMIT test clarity, these should be
simplified to a short, generic namespace (e.g., `demo::Position`).

Package simplification must be applied **jointly** across the primary model and all of its
dependencies. The primary model may reference types defined in a dependency (e.g., a mapping
in the primary model maps to a class defined in a shared types dependency). If the primary
model's packages are simplified but the dependency's are not, the cross-references break.

The transformation works as follows:
1. Parse all `.pure` files — from the primary model and all dependencies — into a combined
   `PureModelContextData`.
2. Identify the common package prefix across **all** elements (primary and dependency).
3. Replace it with a short prefix (e.g., `demo`).
4. Update all cross-references (mapping source/target paths, service query references, etc.).
5. Re-emit the modified `PureModelContextData` to `.pure` files using `PureGrammarComposer`,
   partitioning the output back into primary model files and dependency files.

### 6.3 Element Renaming

Beyond package simplification, element names that are overly specific to the source organization
(e.g., `AcmeTradingService_v2_PROD`) should be renamed to generic, descriptive names
(e.g., `TradingService`). As with package simplification, renames must be applied consistently
across the primary model and dependencies. This step may require manual review for non-trivial
cases.

### 6.4 Dependency Handling

If the selected project has dependencies on other Studio projects, there are two approaches:

- **Inline**: If the dependency is small, inline its `.pure` files into the EMIT test
  as a dependency with a separate root. The dependency's packages are simplified alongside
  the primary model (see §6.2).
- **Shared**: If multiple selected tests share the same dependency, create a shared
  EMIT model for it and reference it via the `source` dependency mechanism. The shared
  model's packages must be simplified once, and all referencing tests must use the
  simplified names consistently.

When a dependency is shared, its package simplification becomes a cross-cutting concern:
any change to the shared model's naming affects all tests that depend on it. The harvest
tool should detect shared dependencies during the selection stage and process them as a
group.

### 6.5 Validation

After translation, run the full EMIT pipeline (parse → compile → generate → test → plan) on
the translated model — including its simplified dependencies — to verify that the
restructuring and renaming are faithful. Any failures indicate translation bugs.

---

## 7. Stage 5: Place

### 7.1 Module Assignment

Each translated test must be placed in the appropriate `legend-engine` module. The primary
feature tag determines the module:

| Primary Feature | Target Module Area |
|---|---|
| `relational-*` | `legend-engine-xts-relational` |
| `service`, `service-test` | `legend-engine-xts-service` |
| `m2m-mapping` | `legend-engine-xts-service` (or M2M-specific module) |
| `file-generation`, `model-generation` | `legend-engine-xts-generation` |
| `external-format`, `binding` | `legend-engine-xts-externalFormat` |
| `flat-data-store` | `legend-engine-xts-flatdata` |
| Basic types only | `legend-engine-core-emit` (bootstrap examples) |

### 7.2 `emit.yaml` Generation

For each placed test, auto-generate the `*.emit.yaml` descriptor:
- `name`: derived from the model's primary concern
- `title` / `description`: derived from the model contents (can be refined manually)
- `modelSources`: root and file list matching the generated directory structure
- `features`: from the feature fingerprint
- `stores`: from the classification
- `complexity`: from the complexity score
- `tags`: auto-generated, supplemented manually

### 7.3 Directory Structure

Place files following the EMIT conventions:

```
src/test/resources/emit-models/
  <name>/
    model/
      types.pure
    store/
      db.pure
    mapping/
      mapping.pure
    service/
      myService.pure
  <name>.emit.yaml
```

---

## 8. Tooling

The harvest process is implemented as a command-line tool:

```
emit-harvest discover --gitlab-url https://gitlab.example.com --token $TOKEN --output manifest.json
emit-harvest classify --manifest manifest.json --output classified.json
emit-harvest select --classified classified.json --output selected.json
emit-harvest translate --selected selected.json --output-dir ./harvested-models/
emit-harvest place --harvested-dir ./harvested-models/ --engine-root ./legend-engine/
```

Each stage reads the output of the previous stage, making the pipeline resumable and
debuggable. The `select` and `translate` stages are the most likely to require manual
intervention (overriding selections, reviewing renames).

---

## 9. Curation Workflow

Automated harvesting produces a first draft. Human curation is expected for:

1. **Reviewing renames**: Verify that simplified package names and element names make sense.
2. **Writing descriptions**: Auto-generated `title` and `description` fields in `emit.yaml`
   will need human polish to be genuinely useful as documentation.
3. **Adjusting selections**: If the auto-selected candidate for a feature fingerprint is
   suboptimal, manually swap it for a better one.
4. **Filling coverage gaps**: For feature combinations with no Studio project representation,
   hand-author minimal examples.

The expectation is that the tool does 80% of the work, and human review handles the remaining
20%.

---

## 10. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| **Sensitive data in production models** | The translation stage strips organization-specific names. Test data embedded in service tests must also be reviewed for sensitive content. Consider an automated PII scan. |
| **Translation fidelity** | Every translated model is validated by running the full EMIT pipeline. Failures are flagged for manual review. |
| **Stale harvest** | The tool can be re-run periodically. New projects or updated projects with novel feature combinations are discovered automatically. |
| **Overwhelming volume** | The selection stage ensures at most one test per feature fingerprint. Even with thousands of projects, the curated output is bounded by the size of the feature taxonomy. |
