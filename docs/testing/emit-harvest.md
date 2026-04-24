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
- **Simplification**: Production projects are large, entangled, and use organization-specific
  naming. EMIT tests should be small, focused, self-contained, and use generic naming.
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
│ Scan for     │     │ Feature      │     │ Pick best    │     │ Simplify     │     │ Module &     │
│ Studio       │     │ fingerprint  │     │ candidate    │     │ packages,    │     │ emit.yaml    │
│ projects     │     │ each project │     │ per feature  │     │ restructure  │     │ generation   │
│              │     │              │     │ combination  │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
```

The process is designed to be run as a **batch tool** (not a continuously running service). It
can be re-run periodically to discover new feature combinations or refresh the catalog with
better examples.

---

## 3. Stage 1: Discover

Use the backend abstraction (see §8.2) to enumerate all Studio projects on the hosting
platform. For each identified project, extract the `.pure` files from the default branch.
Studio projects store their models as `.pure` files written in Legend Engine Grammar — the
same grammar format used by EMIT tests. If the project has declared dependencies (other
versioned Studio projects), resolve those transitively and download the dependency files
as well, keeping the primary model files separate from dependency files.

This stage is implemented by the SDLC-side project extractor (see §8.2). Its output is a
portable directory of `.pure` files and project manifests, which is consumed by the
subsequent stages in the engine-side harvester.

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
to its feature fingerprint, complexity score, element count, and project metadata (project ID,
URL, branch).

---

## 5. Stage 3: Select

### 5.1 Deduplication by Feature Fingerprint

Group projects by their feature fingerprint. For each distinct fingerprint, we want a small
number of representative examples — not hundreds of duplicates, but not necessarily just one.

### 5.2 Candidate Ranking and Selection

Within each fingerprint group, rank candidates by suitability as an EMIT test:

1. **Has tests** — projects with test suites are more valuable.
2. **Fewest dependencies** — self-contained models are preferable.
3. **Clean package structure** — fewer deeply nested packages are easier to understand.

For each fingerprint, select up to **one candidate per complexity tier** (basic, intermediate,
advanced). Small, focused models are valuable as clear examples of a feature combination.
But larger, more realistic models are also valuable — they exercise interactions between
features, stress-test the engine pipeline at scale, and catch bugs that only appear in
non-trivial models. A catalog with only trivial examples would miss an important class of
regressions.

The selection can be overridden manually via a curated allowlist/blocklist.

### 5.3 Coverage Gap Analysis

Compare the set of selected feature fingerprints against the full EMIT feature taxonomy.
Report any feature tags or tag combinations that have no representation — these are
**coverage gaps** that may need hand-authored examples.

---

## 6. Stage 4: Translate

### 6.1 Restructuring and Simplification

Since Studio projects already store models as `.pure` files in Legend grammar — the same
format used by EMIT — no grammar conversion is needed. However, the source files must be
restructured and simplified:

- **Directory layout**: Studio projects may organize files differently than the EMIT
  convention (model, store, mapping, service, etc. subdirectories). Files are split or
  merged to produce one file per concern.
- **Package simplification**: Production projects often use organization-specific package
  hierarchies (e.g., `com::acme::finance::trading::model::Position`). These are replaced
  with a short, generic prefix (e.g., `demo::Position`).

Both transformations are applied **jointly** across the primary model and all of its
dependencies. If the primary model's packages are simplified but a dependency's are not,
cross-references break.

The transformation works as follows:
1. Parse all `.pure` files — from the primary model and all dependencies — into a combined
   `PureModelContextData`.
2. Identify the common package prefix across **all** elements (primary and dependency).
3. Replace it with a short prefix (e.g., `demo`).
4. Update all cross-references (mapping source/target paths, service query references, etc.).
5. Re-emit the modified `PureModelContextData` to `.pure` files using `PureGrammarComposer`,
   splitting output by element type and partitioning back into primary model files and
   dependency files.

### 6.2 Element Renaming

Beyond package simplification, element names that are overly specific to the source organization
(e.g., `AcmeTradingService_v2_PROD`) should be renamed to generic, descriptive names
(e.g., `TradingService`). As with package simplification, renames must be applied consistently
across the primary model and dependencies. This step may require manual review for non-trivial
cases.

### 6.3 Dependency Handling

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

### 6.4 Validation

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

The harvest process is implemented as a Java command-line tool. Each stage of the pipeline
is a separate subcommand that reads input from the previous stage and writes output for the
next, making the pipeline resumable and debuggable.

### 8.1 Tool Split

The pipeline is split across two separate tools:

- **Project Extractor** (in `legend-sdlc`): Scans the hosting platform for Studio projects
  and exports their `.pure` files into a portable extracted directory. This is the only
  component that requires SDLC project structure knowledge.
- **Harvester** (in `legend-engine`): Consumes the extracted directory and runs all
  remaining stages (classify, select, translate, place). It depends only on `legend-engine`
  modules.

### 8.2 Architecture

The harvester needs both `legend-sdlc` project structure knowledge (to locate `.pure` files
and resolve dependencies) and `legend-engine` APIs (to parse, compile, classify, and
translate models). Placing the entire tool in either repository creates problems:
placing it in `legend-engine` with a dependency on `legend-sdlc` creates a pseudo-circular
version dependency (engine vN → sdlc vM → engine vN-1); placing it entirely in `legend-sdlc`
puts the tool far from the EMIT framework it serves.

The solution is to **split the tool at the natural boundary**: the `discover` stage is the
only part that needs SDLC project structure knowledge. Everything after it operates on `.pure`
files and `PureModelContextData`, which are engine-native concepts.

#### SDLC-side: Project Extractor

A tool in `legend-sdlc` (e.g., `legend-sdlc-project-extractor`) that reads Studio projects
from any supported backend and exports their `.pure` files into a portable directory
structure.

The extractor is **backend-agnostic**. It uses a backend abstraction that encapsulates
how to enumerate projects, read file contents, and resolve dependencies for a given
hosting platform. Backend implementations are provided for each supported platform
(e.g., GitLab, GitHub, Bitbucket) and selected via configuration. This requires
extracting the project structure logic from `legend-sdlc-server` into a standalone module
that can be used without the full server.

For each Studio project, the extractor:
1. Uses the backend to locate the project's `.pure` files.
2. Resolves declared dependencies to other Studio projects.
3. Downloads the `.pure` files and writes them to a local directory.
4. Writes a `project-manifest.json` alongside each project's files, recording the project
   ID, URL, branch, and dependency references.

The output is a directory tree:
```
extracted/
  project-1234/
    project-manifest.json
    src/
      model/types.pure
      store/db.pure
      ...
  project-5678/
    project-manifest.json
    src/
      model/common.pure
      ...
```

This is a **data export** — it produces a self-contained snapshot of all Studio project
sources with no engine-specific processing. Because the output format is the same
regardless of backend, the engine-side harvester does not need to know which platform
the projects came from.

#### Engine-side: Harvester

The harvester itself lives in `legend-engine` (e.g., `legend-engine-core-emit-harvest`).
It consumes the extracted directory and runs all remaining stages: classify, select,
translate, and place. It depends only on `legend-engine` modules — no `legend-sdlc`
dependency required.

```
emit-harvest classify  --extracted-dir ./extracted/ --output classified.json
emit-harvest select    --classified classified.json --output selected.json
emit-harvest translate --selected selected.json --extracted-dir ./extracted/ --output-dir ./harvested/
emit-harvest place     --harvested-dir ./harvested/ --engine-root ./legend-engine/
```

#### Dependency Summary

| Component | Lives in | Depends on |
|---|---|---|
| Project Extractor | `legend-sdlc` | SDLC project structure, backend abstraction, backend implementation |
| Harvester | `legend-engine` | Engine grammar/compiler, `legend-engine-core-emit` |

There is **no cross-repo dependency** between the two components. They communicate through the
extracted directory, which is a plain filesystem format (`.pure` files + JSON manifests).

### 8.3 Intermediate Data Formats

Each stage produces a JSON file that serves as input to the next stage. This makes it
possible to inspect, edit, or re-run any stage independently.

#### Project Manifest (`sdlc-extract` output, per project)

Each extracted project directory contains a `project-manifest.json`:

```json
{
  "projectId": 1234,
  "projectUrl": "https://gitlab.example.com/group/project",
  "branch": "master",
  "pureFiles": ["model/types.pure", "store/db.pure", "mapping/mapping.pure"],
  "dependencies": [
    { "projectId": 5678, "version": "1.2.0" },
    { "projectId": 9012, "version": "3.0.1" }
  ]
}
```

#### Classified (`classify` → `select`)

```json
[
  {
    "projectId": 1234,
    "projectUrl": "https://gitlab.example.com/group/project",
    "fingerprint": ["class", "relational-mapping", "relational-store", "service", "service-test"],
    "complexity": "intermediate",
    "elementCount": 47,
    "hasTests": true,
    "hasGenerations": false,
    "dependencyCount": 1,
    "packageDepth": 4
  }
]
```

#### Selected (`select` → `translate`)

```json
[
  {
    "projectId": 1234,
    "projectUrl": "https://gitlab.example.com/group/project",
    "fingerprint": ["class", "relational-mapping", "relational-store", "service", "service-test"],
    "complexity": "intermediate",
    "tier": "intermediate",
    "dependencyProjectIds": [5678]
  }
]
```

### 8.4 Stage Details

#### 8.4.1 `sdlc-extract` (SDLC-side)

- Loads the configured backend implementation (e.g., `GitLabProjectBackend`).
- Uses the backend to enumerate all Studio projects.
- For each project, uses the project structure and backend to locate `.pure` files and
  resolve the dependency configuration.
- Downloads `.pure` file contents.
- Resolves dependencies by recursively fetching dependent projects' files.
- Writes each project's files to a subdirectory under the output directory, along with a
  `project-manifest.json` recording metadata and dependency references.
- **Caching**: Downloaded files are cached locally (keyed by project ID and revision) to
  avoid redundant API calls on re-runs.
- **Rate limiting**: Backend implementations handle platform-specific rate limiting.
  Uses configurable concurrency (default: 4 parallel project fetches).
- **Error handling**: Projects that fail to download are logged and skipped.

#### 8.4.2 `classify` (Engine-side)

- Reads the extracted directory. For each project subdirectory, parses the `.pure` files
  into `PureModelContextData` using `PureGrammarParser`.
- Attempts compilation. Projects that fail to compile are logged and excluded.
- Computes the feature fingerprint by examining element `classifierPath` values, mapping
  types, connection types, and the presence of test suites and generation specifications.
- Computes the complexity score from element count, dependency depth, feature tag count,
  and presence of advanced features.
- Writes the classified output.

#### 8.4.3 `select` (Engine-side)

- Groups classified projects by fingerprint.
- Within each group, ranks by the criteria in §5.2 and selects up to one candidate per
  complexity tier.
- Applies overrides from an optional `overrides.yaml` file:
  ```yaml
  # Force-include a specific project regardless of ranking
  include:
    - projectId: 1234

  # Exclude a project (e.g., contains sensitive data)
  exclude:
    - projectId: 5678
  ```
- Reports coverage gaps: feature tags or combinations present in the taxonomy but absent
  from the selected set.

#### 8.4.4 `translate` (Engine-side)

- For each selected project, reads the extracted `.pure` files and their dependency files.
- Parses all files into a combined `PureModelContextData`.
- Applies package simplification (§6.2):
  - Computes the longest common package prefix.
  - Replaces it with `demo`.
  - Walks the entire `PureModelContextData` to update all path references.
- Applies element renaming (§6.3) using a configurable rename map. Elements not in the
  rename map keep their original (post-simplification) names.
- Re-emits the modified `PureModelContextData` using `PureGrammarComposer`, splitting
  output into files by element type (model, store, mapping, service, connection, runtime,
  generation).
- Partitions output into primary model files and dependency files.
- Runs the full EMIT pipeline on the result to validate (§6.5). Failures are logged.
- Writes the translated files to the output directory, one subdirectory per model.

#### 8.4.5 `place` (Engine-side)

- For each translated model in the output directory, determines the target `legend-engine`
  module based on the feature fingerprint (§7.1).
- Generates the `*.emit.yaml` descriptor with auto-populated metadata.
- Copies the translated `.pure` files into the target module's
  `src/test/resources/emit-models/` directory.
- Writes a placement report summarizing which models went where.

### 8.5 Configuration

Each component has its own configuration file.

**Extractor configuration** (`extractor-config.yaml`, used by `sdlc-extract`):

```yaml
backend:
  type: gitlab                   # backend implementation to use
  url: https://gitlab.example.com
  token: ${GITLAB_TOKEN}
  concurrency: 4
  groups:                        # optional: restrict scan to specific groups
    - legend-projects

cache:
  directory: ./.sdlc-extract-cache
```

**Harvester configuration** (`harvest-config.yaml`, used by `emit-harvest`):

```yaml
simplification:
  defaultPrefix: demo
  renames:                       # optional: global element rename rules
    AcmeTradingService_v2_PROD: TradingService

selection:
  maxPerFingerprint: 3           # one per complexity tier
```

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
| **Overwhelming volume** | The selection stage limits candidates to a small number per feature fingerprint (up to one per complexity tier). Even with thousands of projects, the curated output is bounded by the size of the feature taxonomy. |
