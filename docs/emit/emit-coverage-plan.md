# EMIT Coverage Plan — Closing the Feature-Combination Gaps

A companion to [`emit.md`](emit.md) (design/architecture) and
[`emit-authoring.md`](emit-authoring.md) (how to write a test). This document
takes inventory of the EMIT tests that exist **today**, measures that inventory
against the controlled feature taxonomy in `emit.md` §6.2, and lays out a
prioritized plan to add tests for the feature combinations that are not yet
covered.

Scope note: the **framework self-test fixtures** in `legend-engine-emit` and
`legend-engine-emit-junit` (`class-simple`, `m2m-passing`, `m2m-mixed`,
`file-generation`, `model-generation`, `artifact-generation`, `compile-failure`,
`diamond/*`, `clash-*`) are **excluded** from the coverage inventory below —
they exist to exercise the runner itself, several use fake test-only SPIs
(`EmitDemo*Extension`) rather than real engine extensions, and the server-side
coverage report already excludes the junit module (`emit.md` §5.4). Where a
framework fixture is nonetheless the *only* place a capability appears, this is
called out explicitly, because it means the capability has **no distributed,
real-extension example**.

---

## 1. Inventory of Existing (Distributed) EMIT Tests

Seventeen distributed descriptors exist across three modules. Two of them
(`relational-shared-domain`, `relational-shared-firm-db`) are reusable
dependency bundles that only run parse + compile on their own.

### 1.1 `legend-engine-xts-relationalStore/legend-engine-xt-relationalStore-emit`

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `relational-shared-domain` | `grammar:association`, `grammar:derived-property` | basic |
| `relational-shared-firm-db` | `store:relational-inner-join`, `store:relational-multi-table` | basic |
| `relational-simple` | `execution:data-element`, `execution:test-data` | basic |
| `relational-filter` | `execution:data-element`, `execution:test-data`, `store:relational-filter` | basic |
| `relational-joins` | `execution:data-element`, `execution:test-data`, `grammar:association`, `store:relational-inner-join` | intermediate |
| `relational-enumeration` | `execution:data-element`, `execution:test-data`, `grammar:enumeration`, `mapping:enumeration-mapping` | intermediate |
| `relational-service` | `execution:service`, `execution:service-test` | basic |

### 1.2 `legend-engine-config/legend-engine-emit-tests` (cross-feature)

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `service-with-binding` | `execution:external-format-binding`, `execution:service`, `execution:service-test`, `grammar:derived-property` | basic |

### 1.3 `legend-engine-xts-persistence/legend-engine-xt-persistence-emit`

| Descriptor | Non-scaffolding features (persistence + execution) | Complexity |
|---|---|---|
| `persistence-snapshot` | `execution:service`, `persistence:{nontemporal, persistence, service-output-target, snapshot}` | basic |
| `persistence-snapshot-audit` | + `persistence:auditing` | basic |
| `persistence-snapshot-unitemporal` | `persistence:{persistence, service-output-target, snapshot, unitemporal}` | basic |
| `persistence-notifier` | `persistence:{nontemporal, notifier, persistence, service-output-target, snapshot}` | basic |
| `persistence-append-only` | `persistence:{append-only, auditing, delta, nontemporal, persistence, service-output-target}` | basic |
| `persistence-delta-nontemporal` | `persistence:{auditing, delete-indicator, delta, nontemporal, persistence, service-output-target}` | basic |
| `persistence-delta-unitemporal` | `persistence:{delta, persistence, service-output-target, unitemporal}` | basic |
| `persistence-delta-bitemporal` | `persistence:{bitemporal, delta, persistence, service-output-target}` | basic |
| `persistence-graphfetch-output` | `persistence:{bitemporal, delete-indicator, delta, graph-fetch-service-output, persistence, service-output-target}` | basic |

### 1.4 Test-hosting modules that exist today

Only three modules currently host distributed EMIT tests:

- `legend-engine-xt-relationalStore-emit`
- `legend-engine-xt-persistence-emit`
- `legend-engine-emit-tests` (cross-feature)

The authoring guide (`emit-authoring.md` §3.1) references several per-feature
modules that **do not exist yet** and must be stood up (§9 of that guide) before
their tests can land:

- `legend-engine-core-emit/legend-engine-emit-m2m` — M2M mapping / grammar-only
- `legend-engine-xts-service/legend-engine-xt-service-emit` — service shapes
- `legend-engine-xts-generation/legend-engine-xt-generation-emit` — file/model generation
- `legend-engine-xts-flatdata/legend-engine-xt-flatdata-emit` — flat-data store
- external-format `-emit` modules (e.g. `legend-engine-xts-json/…-jsonSchema-emit`)

---

## 2. Coverage Matrix Against the Taxonomy

Legend: **✅** covered by a distributed test · **▲** covered only by a framework
self-test fixture (no distributed/real-extension example) · **❌** no coverage ·
**⛔** no real legend-engine implementation — not a coverage target (§2.9).

### 2.1 Scaffolding (baseline — not the feature under test)

| Capability | Status | Where |
|---|---|---|
| `scaffolding:class` | ✅ | ubiquitous |
| `scaffolding:relational-store` | ✅ | relational + persistence |
| `scaffolding:relational-connection` | ✅ | `relational-service`, `service-with-binding` |
| `scaffolding:relational-mapping` | ✅ | relational suite |
| `scaffolding:m2m-mapping` | ✅ | `persistence-graphfetch-output` |
| `scaffolding:runtime` | ✅ | `relational-service`, `service-with-binding` |
| `scaffolding:model-connection` | ✅ | `persistence-graphfetch-output` |

Scaffolding is fully exercised.

### 2.2 Grammar — 3 / 10 covered

| Capability | Status |
|---|---|
| `grammar:association` | ✅ |
| `grammar:derived-property` | ✅ |
| `grammar:enumeration` | ✅ |
| `grammar:class-inheritance` | ❌ |
| `grammar:constraint` | ❌ |
| `grammar:function` | ❌ |
| `grammar:measure` | ❌ |
| `grammar:nested-association` | ❌ |
| `grammar:profile` | ❌ |
| `grammar:qualified-property` | ❌ |

### 2.3 Mapping — 1 / 27 covered

| Capability | Status |
|---|---|
| `mapping:enumeration-mapping` | ✅ |
| `mapping:mapping` | ▲ (framework `m2m-passing` / `m2m-mixed` only) |
| `mapping:aggregation-aware-mapping` | ❌ |
| `mapping:cross-store` | ❌ |
| `mapping:m2m-derived-source-property` | ❌ |
| `mapping:m2m-local-property` | ❌ |
| `mapping:m2m-transform` | ❌ |
| `mapping:mapping-include` | ❌ |
| `mapping:operation-mapping` | ❌ |
| `mapping:operation-mapping-merge` | ❌ |
| `mapping:operation-mapping-merge-validation` | ❌ |
| `mapping:relational-association-implementation` | ❌ |
| `mapping:relational-distinct` | ❌ |
| `mapping:relational-embedded` | ❌ |
| `mapping:relational-group-by` | ❌ |
| `mapping:relational-inline-embedded` | ❌ |
| `mapping:relational-joined-table-inheritance` | ❌ |
| `mapping:relational-literal` | ❌ |
| `mapping:relational-literal-list` | ❌ |
| `mapping:relational-main-table-alias` | ❌ |
| `mapping:relational-otherwise-embedded` | ❌ |
| `mapping:relational-polymorphic-query` | ❌ |
| `mapping:relational-primary-key` | ❌ |
| `mapping:relational-single-table-inheritance` | ❌ |
| `mapping:relational-table-alias-column` | ❌ |
| `mapping:router-union` | ❌ |
| `mapping:store-union` | ❌ |

> Note: `relational-joins` navigates an association via `[db]@Join` property
> mappings but is tagged `store:relational-inner-join` + `grammar:association`,
> **not** `mapping:relational-association-implementation`. That capability is
> still an uncovered gap.

### 2.4 Store — 3 / 13 covered

| Capability | Status |
|---|---|
| `store:relational-filter` | ✅ |
| `store:relational-inner-join` | ✅ |
| `store:relational-multi-table` | ✅ |
| `store:relational-cross-schema` | ❌ |
| `store:relational-cross-table-filter` | ❌ |
| `store:relational-dyna-function` | ❌ |
| `store:relational-inline-view` | ❌ |
| `store:relational-left-outer-join` | ❌ |
| `store:relational-nested-join` | ❌ |
| `store:relational-outer-join` | ❌ |
| `store:relational-right-outer-join` | ❌ |
| `store:service-store` | ❌ |
| `store:flat-data-store` | ❌ |

### 2.5 Milestoning — 0 / 7 covered (entire domain uncovered)

| Capability | Status |
|---|---|
| `milestoning:business-temporal` | ❌ |
| `milestoning:processing-temporal` | ❌ |
| `milestoning:bi-temporal` | ❌ |
| `milestoning:point-in-time-query` | ❌ |
| `milestoning:all-versions-query` | ❌ |
| `milestoning:all-versions-in-range-query` | ❌ |
| `milestoning:milestoning` | ❌ |

> This is the *class/relational* milestoning domain (temporal classes + temporal
> query functions), distinct from the persistence temporal capabilities in §2.7
> which are fully covered.

### 2.6 Execution — 5 / 17 covered

| Capability | Status |
|---|---|
| `execution:data-element` | ✅ |
| `execution:external-format-binding` | ✅ |
| `execution:service` | ✅ |
| `execution:service-test` | ✅ |
| `execution:test-data` | ✅ |
| `execution:file-generation` | ❌ (real generators exist — Avro/Protobuf/JSON Schema/…; only the fake-SPI framework fixture exercises the path) |
| `execution:model-generation` | ⛔ (no real `ModelGenerationExtension` SPI in legend-engine — §2.9) |
| `execution:binding` | ❌ |
| `execution:external-format` | ❌ |
| `execution:schema-set` | ❌ |
| `execution:multi-execution-service` | ❌ |
| `execution:post-validation` | ❌ |
| `execution:shared-test-data` | ❌ |
| `execution:plan-generation` | ❌ (tag never applied; every service model exercises the path) |
| `execution:hosted-service` | ❌ |
| `execution:snowflake-app` | ❌ |
| `execution:bigquery-function` | ❌ |

### 2.7 Persistence — 12 / 12 covered ✅

The entire persistence domain is covered at the per-capability level. Remaining
opportunities here are combination-level only (see §3.4) and are low priority.

### 2.8 Coverage headline

| Domain | Covered / Total (distributed) |
|---|---|
| Scaffolding | 7 / 7 |
| Grammar | 3 / 10 |
| Mapping | 1 / 27 |
| Store | 3 / 13 |
| Milestoning | 0 / 7 |
| Execution | 5 / 17 (1 out-of-scope — §2.9) |
| Persistence | 12 / 12 |
| **Total** | **31 / 93** (of which 1 is out of scope) |

The concentration is stark: **persistence is complete**, relational scaffolding
is solid, and everything else — the mapping domain above all — is sparse.

### 2.9 Out of scope: model generation (no real feature to test)

`execution:model-generation` is in the taxonomy but has **no real
implementation** in legend-engine, so this plan proposes **no** model-generation
tests. EMIT Phase 3 discovers generators via
`ServiceLoader.load(ModelGenerationExtension.class)` — the DSL SPI
`org.finos.legend.engine.language.pure.dsl.generation.extension.ModelGenerationExtension`
(type `"Generation_Model"`), driven by a `GenerationSpecification`'s
`generationNodes`. The only registered implementations of that SPI in the entire
workspace are the fake `EmitDemoModelGenerationExtension` test doubles in
`legend-engine-emit` / `legend-engine-emit-junit`; there is no production
`META-INF/services` entry for it anywhere.

The real `ExternalFormatModelGenerationExtension` (schema→model, e.g. JSON
Schema → Pure classes) is a **different** interface — it extends
`ExternalFormatExtension` (type `"Model_Generation"`) and is reached through the
external-format `GenerateModelInput` API, **not** through a
`GenerationSpecification` and **not** through EMIT's Phase 3 SPI. It is therefore
not exercisable as an EMIT model-generation test today. Should a real
`ModelGenerationExtension` SPI ever ship (or Phase 3 be wired to the
external-format model-generation path), a distributed test should be added then —
tracked as future work, not part of this plan.

File generation (Phase 4a) and artifact generation (Phase 4b), by contrast, both
have real, registered extensions (Avro, Protobuf, JSON Schema, GraphQL, DAML,
Morphir for `GenerationExtension`; the function activators, data-space,
dataquality, OpenAPI, PowerBI, … for `ArtifactGenerationExtension`), so they
remain in scope — see §3.6.

---

## 3. Proposed New Tests

Each row below is a proposed descriptor: a name, the capability gap(s) it closes,
its non-scaffolding feature set, and its target module. Feature sets were checked
against §1's inventory for exact-match duplicates per `emit-authoring.md` §11.2 —
none duplicate an existing set. Subset/superset relationships are intentional
(they provide distinct regression coverage). `.pure` authoring follows
`emit-authoring.md` §4; reuse `relational-shared-domain` /
`relational-shared-firm-db` via `dependencies` wherever the domain fits.

Complexity is derived mechanically (distinct non-scaffolding domains: 1–2 basic,
3–4 intermediate, 5+ advanced).

### 3.1 Relational mapping features → `legend-engine-xt-relationalStore-emit`

All of these fit the existing relational module's classpath — **no new module
needed**. This is the highest-value, lowest-friction batch.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `relational-primary-key` | `mapping:relational-primary-key` | `execution:{data-element,test-data}`, `mapping:relational-primary-key` |
| `relational-embedded` | `mapping:relational-embedded` | `execution:{data-element,test-data}`, `mapping:relational-embedded` |
| `relational-inline-embedded` | `mapping:relational-inline-embedded` | `execution:{data-element,test-data}`, `mapping:relational-inline-embedded` |
| `relational-otherwise-embedded` | `mapping:relational-otherwise-embedded` | `execution:{data-element,test-data}`, `mapping:relational-otherwise-embedded` |
| `relational-association-implementation` | `mapping:relational-association-implementation` | `execution:{data-element,test-data}`, `grammar:association`, `mapping:relational-association-implementation` |
| `relational-distinct` | `mapping:relational-distinct` | `execution:{data-element,test-data}`, `mapping:relational-distinct` |
| `relational-group-by` | `mapping:relational-group-by` | `execution:{data-element,test-data}`, `mapping:relational-group-by` |
| `relational-literal` | `mapping:relational-literal` (see note 2) | `execution:{data-element,test-data}`, `mapping:relational-literal` |
| `relational-main-table-alias` | `mapping:relational-main-table-alias`, `mapping:relational-table-alias-column` | `execution:{data-element,test-data}`, `mapping:{relational-main-table-alias,relational-table-alias-column}` |
| `relational-single-table-inheritance` | `mapping:relational-single-table-inheritance`, `grammar:class-inheritance` | `execution:{data-element,test-data}`, `grammar:class-inheritance`, `mapping:relational-single-table-inheritance` |
| `relational-joined-table-inheritance` | `mapping:relational-joined-table-inheritance` | `execution:{data-element,test-data}`, `grammar:class-inheritance`, `mapping:relational-joined-table-inheritance`, `store:relational-inner-join` |
| `relational-polymorphic-query` | `mapping:relational-polymorphic-query` | `execution:{data-element,test-data}`, `grammar:class-inheritance`, `mapping:relational-polymorphic-query` |
| `relational-operation-mapping` | `mapping:operation-mapping` | `execution:{data-element,test-data}`, `grammar:class-inheritance`, `mapping:operation-mapping` |
| ~~`relational-operation-merge`~~ | — moved to §3.4 (see note 1) | — |
| ~~`relational-operation-merge-validation`~~ | — moved to §3.4 (see note 1) | — |
| `relational-store-union` | `mapping:store-union` | `execution:{data-element,test-data}`, `mapping:store-union`, `store:relational-multi-table` |
| `relational-router-union` | `mapping:router-union` | `execution:{data-element,test-data}`, `mapping:router-union` |
| `relational-aggregation-aware` | `mapping:aggregation-aware-mapping` | `execution:{data-element,test-data}`, `mapping:{aggregation-aware-mapping,relational-group-by}` |
| `relational-mapping-include` | `mapping:mapping-include` | `execution:{data-element,test-data}`, `mapping:mapping-include` |

**Phase A is 17 tests, not 19** — all landed and passing. Two findings from implementation:

> **Note 1 — merge is an M2M capability, not a relational one.**
> `meta::pure::router::operations::merge_...` has no occurrence anywhere under
> `legend-engine-xts-relationalStore`. Its only execution binding is the in-memory /
> M2M store (`core/store/m2m/inMemory.pure` registers merge →
> `modelToModel::inMemory::mergeResult`); the relational store registers no
> `operationFunctions` and no merge handling, and the only grammar/compiler example
> in the repo is over `Pure` set implementations
> (`TestCompilationFromGrammar#testCompilationFromGrammarWithMergeOperation`).
> `mapping:operation-mapping-merge` and `mapping:operation-mapping-merge-validation`
> therefore belong in the §3.4 M2M batch (Phase D), not here.

> **Note 2 — `mapping:relational-literal-list` is blocked by an engine defect.**
> The grammar accepts an inline literal array (`in(col, ['A', 'B'])` —
> `RelationalParserGrammar.g4` rule `functionOperationArgumentArray`), but
> `RelationalParseTreeWalker.visitFunctionOperationArgument` wraps each already-built
> element `Literal` in a *second* `Literal`, so the value handed to
> `toPostgresModel::convertLiteral` is a `Literal` rather than a scalar and the
> dialect translation dies with a match failure. `LiteralList` itself is handled
> correctly (`toPostgresModel.pure` → `InListExpression`), so the fault is confined
> to the parse-tree walker. The capability is not testable until that is fixed;
> `relational-literal` covers `mapping:relational-literal` only.

> **Authoring note.** `~distinct` and `~groupBy` at class-mapping level are only
> honoured when the query projects first and sorts the TDS afterwards
> (`->project(...)->sort([asc('col')])`). Using `->sortBy(...)` on class instances
> *before* `->project(...)` routes differently and silently drops the DISTINCT /
> GROUP BY subquery — the projection then returns undeduplicated rows, or H2 rejects
> the SQL outright. Mirror the query shape in
> `core_relational/relational/tests/mapping/{distinct,groupBy}/`.

### 3.2 Relational store features → `legend-engine-xt-relationalStore-emit`

Also no new module. A shared multi-schema / multi-join store can be added as a
reusable dependency (like `relational-shared-firm-db`) and consumed by several.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `relational-left-outer-join` | `store:relational-left-outer-join` | `execution:{data-element,test-data}`, `grammar:association`, `store:relational-left-outer-join` |
| `relational-right-outer-join` | `store:relational-right-outer-join` | `execution:{data-element,test-data}`, `grammar:association`, `store:relational-right-outer-join` |
| `relational-outer-join` | `store:relational-outer-join` | `execution:{data-element,test-data}`, `grammar:association`, `store:relational-outer-join` |
| `relational-nested-join` | `store:relational-nested-join` | `execution:{data-element,test-data}`, `grammar:nested-association`, `store:relational-nested-join` |
| `relational-cross-schema` | `store:relational-cross-schema` | `execution:{data-element,test-data}`, `store:{relational-cross-schema,relational-inner-join}` |
| `relational-cross-table-filter` | `store:relational-cross-table-filter` | `execution:{data-element,test-data}`, `store:{relational-cross-table-filter,relational-inner-join}` |
| `relational-inline-view` | `store:relational-inline-view` | `execution:{data-element,test-data}`, `store:relational-inline-view` |
| `relational-dyna-function` | `store:relational-dyna-function` | `execution:{data-element,test-data}`, `store:relational-dyna-function` |

### 3.3 Class/relational milestoning → `legend-engine-xt-relationalStore-emit`

The relational module's classpath covers temporal classes and temporal query
functions. No new module.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `milestoning-business-temporal` | `milestoning:business-temporal`, `milestoning:point-in-time-query`, `milestoning:milestoning` | `execution:{data-element,test-data}`, `milestoning:{business-temporal,point-in-time-query,milestoning}` |
| `milestoning-processing-temporal` | `milestoning:processing-temporal` | `execution:{data-element,test-data}`, `milestoning:{processing-temporal,point-in-time-query,milestoning}` |
| `milestoning-bitemporal` | `milestoning:bi-temporal` | `execution:{data-element,test-data}`, `milestoning:{bi-temporal,point-in-time-query,milestoning}` |
| `milestoning-all-versions` | `milestoning:all-versions-query`, `milestoning:all-versions-in-range-query` | `execution:{data-element,test-data}`, `milestoning:{business-temporal,all-versions-query,all-versions-in-range-query,milestoning}` |

### 3.4 Grammar-only + M2M mapping → new `legend-engine-emit-m2m`

These need only the core compiler + M2M classpath. Stand up
`legend-engine-core-emit/legend-engine-emit-m2m` (`emit-authoring.md` §9) and
host both the grammar-only fixtures and the M2M mapping features here.

**Grammar-only** (parse + compile; no store, no mapping):

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `grammar-constraint` | `grammar:constraint` | `grammar:constraint` |
| `grammar-class-inheritance` | `grammar:class-inheritance` | `grammar:class-inheritance` |
| `grammar-function` | `grammar:function` | `grammar:function` |
| `grammar-measure` | `grammar:measure` | `grammar:measure` |
| `grammar-profile` | `grammar:profile` | `grammar:profile` |
| `grammar-qualified-property` | `grammar:qualified-property` | `grammar:qualified-property` |
| `grammar-nested-association` | `grammar:nested-association` | `grammar:association`, `grammar:nested-association` |

**M2M mapping** (real M2M transforms with an executable test suite — the
distributed counterpart to the fake-free framework fixtures):

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `m2m-transform` | `mapping:m2m-transform` | `execution:{data-element,test-data}`, `mapping:{mapping,m2m-transform}` |
| `m2m-local-property` | `mapping:m2m-local-property` | `execution:{data-element,test-data}`, `mapping:{mapping,m2m-local-property}` |
| `m2m-derived-source-property` | `mapping:m2m-derived-source-property` | `execution:{data-element,test-data}`, `grammar:derived-property`, `mapping:{mapping,m2m-derived-source-property}` |
| `m2m-enumeration-mapping` | `mapping:enumeration-mapping` (M2M variant) | `execution:{data-element,test-data}`, `grammar:enumeration`, `mapping:{mapping,enumeration-mapping}` |

### 3.5 Service shapes → new `legend-engine-xt-service-emit`

Stand up `legend-engine-xts-service/legend-engine-xt-service-emit`.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `service-multi-execution` | `execution:multi-execution-service` | `execution:{service,service-test,multi-execution-service}` |
| `service-post-validation` | `execution:post-validation` | `execution:{service,service-test,post-validation}` |
| `service-shared-test-data` | `execution:shared-test-data` | `execution:{service,service-test,shared-test-data,data-element}` |
| `service-legacy-test` | legacy `ServiceTest` path (Phase 5) | `execution:{service,service-test}` (tag `legacy-service-test`) |
| `mapping-legacy-test` | legacy `MappingTest`/`MappingTestSuite` path (Phase 5) | `mapping:mapping` (tag `legacy-mapping-test`) |

> `service-legacy-test` / `mapping-legacy-test` fill a **pipeline** gap rather
> than a taxonomy gap: EMIT Phase 5 runs three test runners (Testable, legacy
> mapping, legacy service — `emit.md` §4.6) and only the Testable path has a
> distributed example. Consider adding a `legacy` taxonomy tag in the same PR.

### 3.6 File generation (real extensions) → new `legend-engine-xt-generation-emit`

Replace the fake-SPI framework fixtures with real-extension distributed examples.
Real `GenerationExtension` implementations exist for Avro, Protobuf, JSON Schema,
GraphQL, DAML, and Morphir. Stand up
`legend-engine-xts-generation/legend-engine-xt-generation-emit` with test-scoped
deps on the generators you exercise (or place each test in the owning format's
`-emit` module, e.g. an Avro file-generation test in `legend-engine-xt-avro-emit`).

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `generation-avro` | `execution:file-generation` (real Avro) | `execution:file-generation` (tag `avro`) |
| `generation-protobuf` | `execution:file-generation` (real Protobuf) | `execution:file-generation` (tag `protobuf`) |
| `generation-json-schema` | `execution:file-generation` (real JSON Schema) | `execution:file-generation` (tag `json-schema`) |

> **Model generation is intentionally omitted** — no real `ModelGenerationExtension`
> SPI exists in legend-engine (§2.9).
>
> **Element-driven artifact generation** (Phase 4b) needs no dedicated descriptor
> here: it fires automatically for any element a registered
> `ArtifactGenerationExtension` accepts, so it is exercised incidentally by the
> function-activator, data-space, and dataquality element tests (§3.9 and future
> DSL `-emit` modules). Add a targeted artifact-generation descriptor only if you
> want to assert exact generated content for a specific extension.

### 3.7 External format → new format `-emit` modules

Stand up `-emit` modules under the format XTS trees (e.g.
`legend-engine-xts-json/legend-engine-external-format-jsonSchema-emit`, and peers
for XML / Avro as needed).

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `external-format-schema-set` | `execution:schema-set`, `execution:external-format` | `execution:{schema-set,external-format}` |
| `external-format-binding` | `execution:binding` | `execution:{binding,external-format,schema-set}` |
| `external-format-service-binding` | `execution:binding` + service round-trip | `execution:{binding,external-format,service,service-test}` |

> `service-with-binding` (existing) uses `execution:external-format-binding`;
> the taxonomy also has the distinct `execution:binding` and
> `execution:external-format` / `execution:schema-set` tags, which are the true
> gaps here.

### 3.8 Other stores → new store `-emit` modules

| Proposed test | Closes | Module (new) |
|---|---|---|
| `flatdata-simple` (`store:flat-data-store`, `execution:{data-element,test-data}`) | `store:flat-data-store` | `legend-engine-xts-flatdata/legend-engine-xt-flatdata-emit` |
| `service-store-simple` (`store:service-store`, `execution:{service}`) | `store:service-store` | `legend-engine-xts-serviceStore/legend-engine-xt-serviceStore-emit` |

### 3.9 Function activators → new activator `-emit` modules

Each activator DSL is a separate XTS module with its own classpath.

| Proposed test | Closes | Module (new) |
|---|---|---|
| `hosted-service-simple` (`execution:hosted-service`) | `execution:hosted-service` | `legend-engine-xts-hostedService/legend-engine-xt-hostedService-emit` |
| `snowflake-app-simple` (`execution:snowflake-app`) | `execution:snowflake-app` | `legend-engine-xts-snowflake/…-emit` |
| `bigquery-function-simple` (`execution:bigquery-function`) | `execution:bigquery-function` | `legend-engine-xts-bigqueryFunction/…-emit` |

> Activators are metadata-only elements: these tests will exercise parse +
> compile (+ artifact/plan generation where the extension supports it) rather
> than execution. Verify each activator's `-emit` classpath actually compiles the
> DSL before committing to the module.

### 3.10 High-value cross-feature combinations → `legend-engine-emit-tests`

Reserve the cross-feature module for combinations no single per-feature module's
classpath can host (`emit-authoring.md` §3.2). Add sparingly, only after the
per-feature gaps above are closed.

| Proposed test | Purpose | Feature set (non-scaffolding, abbreviated) |
|---|---|---|
| `cross-store-m2m-relational` | `mapping:cross-store` | `mapping:cross-store`, `execution:{service,service-test}` |
| `service-relational-multi-execution` | multi-execution over a relational mapping | `execution:{service,service-test,multi-execution-service}` |
| `service-relational-with-generation` | service + relational + file generation (the `emit.md` §6.1 exemplar) | `execution:{service,service-test,file-generation}`, `grammar:association` |

---

## 4. Prioritization & Sequencing

Ordered by value-per-unit-effort. Phases A–C need **no new modules** and close
the largest gaps; later phases are gated on standing up modules.

| Phase | Batch | New module? | Gaps closed | Effort |
|---|---|---|---|---|
| **A** | §3.1 Relational mapping features (17 tests — **done**) | No | 19 capabilities | Low — existing classpath, reuse shared domain |
| **B** | §3.2 Relational store features (8 tests) | No | 8 capabilities | Low |
| **C** | §3.3 Milestoning (4 tests) | No | 7 capabilities (whole domain) | Low–Med — needs temporal query authoring |
| **D** | §3.4 Grammar + M2M (11 tests) | `legend-engine-emit-m2m` | 6 grammar + 4 mapping | Med — 1 module |
| **E** | §3.5 Service shapes (5 tests) | `legend-engine-xt-service-emit` | 3 execution + legacy paths | Med — 1 module |
| **F** | §3.6 File generation (3 tests) | `legend-engine-xt-generation-emit` | real file generation (Avro/Protobuf/JSON Schema) | Med |
| **G** | §3.7 External format (3 tests) | format `-emit` module(s) | 3 execution capabilities | Med |
| **H** | §3.8 Other stores (2 tests) | flatdata + serviceStore `-emit` | 2 store capabilities | Med |
| **I** | §3.9 Function activators (3 tests) | 3 activator `-emit` modules | 3 execution capabilities | Higher — 3 modules, verify classpaths |
| **J** | §3.10 Cross-feature combos (3 tests) | No (existing) | combination coverage | Low, do last |

**Milestone: every real feature covered at end of Phase I.** Every
`domain:capability` in `emit.md` §6.2 that maps to a real legend-engine feature
has at least one distributed example. The sole exception is
`execution:model-generation`, which has no real implementation and is out of
scope (§2.9). Phase J and the persistence combination extras (§3.4 of
`emit-authoring.md` dedup rules apply) are then incremental combination coverage
rather than gap-closing.

### 4.1 Per-test workflow (every phase)

For each proposed descriptor, follow `emit-authoring.md` §4:

1. Confirm placement module classpath covers the feature set (§3 there); stand
   up the module first if it's a Phase D+ new module (§9 there).
2. Author `.pure` sources under `src/test/resources/emit-models/<name>/`, with
   the Apache header on every file; reuse shared domains via `dependencies`.
3. Write the `<name>.emit.yaml` with `title`/`description` that state **what the
   test proves** (not which phases fire), sorted `features` from the taxonomy,
   explicit `stores`, mechanically-derived `complexity`, and search `tags`.
4. Ensure the module has a Surefire-visible `*EMITTests` `@TestFactory` class
   (§8 there).
5. Run `mvn test -pl <module> -Dtest=<Module>EMITTests` and confirm the model
   appears as a passing `DynamicContainer` tree.

### 4.2 Taxonomy maintenance

No **new** taxonomy entries are required to close the §2 gaps — every proposed
test maps to an existing `domain:capability`. Two optional additions to consider
in the owning PRs:

- A `legacy` marker (or `execution:legacy-mapping-test` / `execution:legacy-service-test`)
  to distinguish the legacy Phase-5 runner coverage in §3.5.
- Apply the already-defined `execution:plan-generation` tag to service-bearing
  models (it exists in the taxonomy but is currently applied nowhere).

Any genuinely new capability discovered while authoring must be added to
`emit.md` §6.2 **in the same PR** as the test (`emit-authoring.md` §10).

---

## 5. Summary

- **31 of 93** taxonomy capabilities have a distributed example today. Of the
  62 uncovered, **one is out of scope** — `execution:model-generation` has no
  real implementation (§2.9) — leaving **61 real-feature gaps** to close.
  `execution:file-generation` is a real gap (real Avro/Protobuf/JSON Schema
  generators exist; only the fake-SPI framework fixture exercises the path).
- **Persistence is complete**; **scaffolding is solid**; the **mapping domain
  (1/27)**, **milestoning (0/7)**, and the store/grammar/execution long tails are
  the substance of the gap.
- **~61 proposed tests** across 10 batches close every real-feature gap.
- **Phases A–C (31 tests) need no new modules** and should land first — they
  close 37 capability gaps against the existing relational classpath.
- Every real feature has a distributed example at the end of Phase I; the
  remaining work is combination-level and incremental. Model generation is
  revisited only if a real extension ships.
