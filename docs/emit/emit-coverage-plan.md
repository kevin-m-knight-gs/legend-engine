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

**Seventy-six** distributed descriptors exist across four modules — 56 of them in
the relational module and 10 in the core-feature module added by Phase D, both of
which host two independent suites over two resource roots. Shared-dependency bundles
(`relational-shared-domain`,
`relational-shared-firm-db`, `relation-shared-domain`, `relation-shared-db`,
`relation-shared-data`) are reusable and only run parse + compile on their own.

### 1.1 `legend-engine-xt-relationalStore-emit` — `relational-emit-models/`

Driven by `RelationalEMITTests`. 33 descriptors, 239 dynamic tests. The eight
below predate the Phase A batch; the 17 added by Phase A are listed in §3.1; the
7 added by Phase B (a shared two-schema store bundle + 6 store-feature tests) are
in §3.2; Phase C added `milestoning-bitemporal` (§3.3). (`relational-service-with-join`
was dead until §3.0 renamed it to `.emit.yaml`; it now discovers, runs, and
passes — see §2.10(d).)

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `relational-shared-domain` | `grammar:association`, `grammar:derived-property` | basic |
| `relational-shared-firm-db` | `store:relational-inner-join`, `store:relational-multi-table` | basic |
| `relational-simple` | `execution:data-element`, `execution:test-data` | basic |
| `relational-filter` | `execution:data-element`, `execution:test-data`, `store:relational-filter` | basic |
| `relational-joins` | `execution:data-element`, `execution:test-data`, `grammar:association`, `store:relational-inner-join` | intermediate |
| `relational-enumeration` | `execution:data-element`, `execution:test-data`, `grammar:enumeration`, `mapping:enumeration-mapping` | intermediate |
| `relational-service` | `execution:service`, `execution:service-test` | basic |
| `relational-service-with-join` | `execution:service`, `execution:service-test`, `grammar:association`, `grammar:derived-property`, `store:relational-inner-join` | intermediate |

### 1.2 `legend-engine-xt-relationalStore-emit` — `relation-emit-models/`

Driven by `RelationEMITTests` over a separate resource root, so a failure is
attributable to one suite. 22 descriptors, 211 dynamic tests, covering
**relation-function** class mappings (`~func` / `~src`) rather than table-backed
relational mappings.

| Descriptor | Capability under test | Complexity |
|---|---|---|
| `relation-shared-domain` | shared classes/enums/associations (parse+compile only) | basic |
| `relation-shared-db` | shared H2 store (parse+compile only) | basic |
| `relation-shared-data` | shared `###Data` element (parse+compile only) | basic |
| `relation-simple` | baseline `~func` mapping + enum transformer | basic |
| `relation-src` | `~src` inline-source form | basic |
| `relation-filter` | `->filter` in the source relation, and stacked query filters | basic |
| `relation-groupBy` | `->groupBy` with `sum`/`average` in the source relation | basic |
| `relation-window-function` | `over(...)`/`->extend` window ranking | basic |
| `relation-expression-rhs` | `$src` expression as a property RHS | basic |
| `relation-embedded` | embedded property mapping `prop ( ... )` | basic |
| `relation-inline-embedded` | inline embedded `prop () Inline [setId]` | basic |
| `relation-enumeration` | `EnumerationMapping` transformer | basic |
| `relation-include` | mapping include composition | basic |
| `relation-join` / `relation-modelJoin` | ModelJoin association (+ local properties) | basic |
| `relation-modelJoin-chained` | multi-hop ModelJoin | basic |
| `relation-mixed-association-chain` | ModelJoin spanning relation *and* relational set impls | basic |
| `relation-union` / `relation-union-enum` | union of relation set impls (+ enum/embedded) | basic |
| `relation-relational-union` | union mixing relation and relational set impls | basic |
| `relation-milestoning` | processing-temporal class, `allVersions()` + `all(%date)` | basic |
| `relation-milestoning-modelJoin-asymmetric` | ModelJoin across business- and processing-temporal | intermediate |

This batch is **substantively strong** — it reaches real capability (milestoning,
window functions, unions, mixed relation/relational chains) that nothing else in
the catalog touches. Its problems are all *metadata* problems, recorded in §2.10;
they matter because the coverage matrix is computed from metadata.

### 1.3 `legend-engine-config/legend-engine-emit-tests` (cross-feature)

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `service-with-binding` | `execution:external-format-binding`, `execution:service`, `execution:service-test`, `grammar:derived-property` | basic |

### 1.4 `legend-engine-xts-persistence/legend-engine-xt-persistence-emit`

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

### 1.5 `legend-engine-core/legend-engine-core-emit-tests` (core-feature)

10 descriptors, 52 dynamic tests, all added by Phase D (§3.4), split across two
independently-runnable suites by subject:

- `grammar-emit-models/` → `GrammarEMITTests` — 7 language-construct models
  (parse + compile; no store, no mapping)
- `m2m-emit-models/` → `M2MEMITTests` — 3 model-to-model mappings whose embedded test
  suites execute against the in-memory store

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `grammar-constraint` | `grammar:constraint` | basic |
| `grammar-class-inheritance` | `grammar:class-inheritance`, `grammar:derived-property` | basic |
| `grammar-function` | `grammar:function` | basic |
| `grammar-measure` | `grammar:measure` | basic |
| `grammar-profile` | `grammar:association`, `grammar:enumeration`, `grammar:function`, `grammar:profile` | basic |
| `grammar-qualified-property` | `grammar:enumeration`, `grammar:qualified-property` | basic |
| `grammar-nested-association` | `grammar:association`, `grammar:derived-property`, `grammar:nested-association` | basic |
| `m2m-transform` | `execution:test-data`, `mapping:m2m-transform`, `mapping:mapping` | basic |
| `m2m-derived-source-property` | `execution:test-data`, `grammar:derived-property`, `grammar:qualified-property`, `mapping:m2m-derived-source-property`, `mapping:mapping` | basic |
| `m2m-enumeration-mapping` | `execution:test-data`, `grammar:enumeration`, `mapping:enumeration-mapping`, `mapping:mapping` | basic |

### 1.6 Test-hosting modules that exist today

Four modules currently host distributed EMIT tests:

- `legend-engine-xt-relationalStore-emit` (two suites: `relational-emit-models/`
  via `RelationalEMITTests`, `relation-emit-models/` via `RelationEMITTests`)
- `legend-engine-xt-persistence-emit`
- `legend-engine-core-emit-tests` (core-feature: Pure language constructs + M2M)
- `legend-engine-emit-tests` (cross-feature)

The authoring guide (`emit-authoring.md` §3.1) references several per-feature
modules that **do not exist yet** and must be stood up (§9 of that guide) before
their tests can land:

- ~~`legend-engine-core-emit/legend-engine-emit-m2m`~~ — stood up in Phase D as
  `legend-engine-core/legend-engine-core-emit-tests` (see §3.4); renamed because the
  batch is majority core-language fixtures rather than M2M, and relocated to a
  sibling of `legend-engine-core-emit` so a catalog module is not mistaken for one of
  the four framework modules inside it
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

### 2.2 Grammar — 10 / 10 covered ✅

| Capability | Status |
|---|---|
| `grammar:association` | ✅ |
| `grammar:class-inheritance` | ✅ Phase A (`relational-{single-table,joined-table}-inheritance`, `relational-{operation-mapping,polymorphic-query}`); also `relation-window-function` (`RankedEmployee extends Employee`); Phase D `grammar-class-inheritance` isolates the language construct |
| `grammar:derived-property` | ✅ |
| `grammar:enumeration` | ✅ |
| `grammar:function` | ✅ relation suite — tag applied to all 19 executable `~func` models in §3.0; Phase D `grammar-function` is the first fixture where the function element is the subject rather than a mapping source |
| `grammar:nested-association` | ✅ `relation-modelJoin-chained` (multi-level `firm.departments.staff` traversal); Phase D `grammar-nested-association` covers the plain-association form |
| `grammar:constraint` | ✅ Phase D (`grammar-constraint`) |
| `grammar:measure` | ✅ Phase D (`grammar-measure`) |
| `grammar:profile` | ✅ Phase D (`grammar-profile`) |
| `grammar:qualified-property` | ✅ Phase D (`grammar-qualified-property`, `m2m-derived-source-property`) |

The grammar domain is closed. Phase D's seven grammar-only fixtures run parse +
compile with no store and no mapping, so a regression in a language construct is
attributable to the construct rather than to the mapping strategy that previously
happened to carry it.

> `grammar:function` was the second instance of the §2.10 pattern: genuinely
> exercised by all 19 executable relation models but claimed by none, so it read
> as a gap. §3.0 applied the tag, and it is now machine-checkable.

### 2.3 Mapping — 34 / 40 covered (4 out-of-scope — §3.1 note 2, §3.4a)

The relational half of this domain went from 1/27 to 17/27 in Phase A; the
relation-function half (13 entries, §6.2 of `emit.md`) is new — §3.0 added
`relation-filter`, `relation-group-by`, and `relation-xstore-association` to it
while normalizing the relation batch.

**Relational + store-agnostic**

| Capability | Status |
|---|---|
| `mapping:aggregation-aware-mapping` | ✅ Phase A |
| `mapping:enumeration-mapping` | ✅ relational + relation; Phase D `m2m-enumeration-mapping` adds the M2M code path (string- and integer-keyed) |
| `mapping:mapping-include` | ✅ Phase A + relation (`relation-include`, mis-tagged — §2.10) |
| `mapping:operation-mapping` | ✅ Phase A |
| `mapping:relational-association-implementation` | ✅ Phase A |
| `mapping:relational-distinct` | ✅ Phase A |
| `mapping:relational-embedded` | ✅ Phase A |
| `mapping:relational-group-by` | ✅ Phase A |
| `mapping:relational-inline-embedded` | ✅ Phase A |
| `mapping:relational-joined-table-inheritance` | ✅ Phase A |
| `mapping:relational-literal` | ✅ Phase A |
| `mapping:relational-main-table-alias` | ✅ Phase A |
| `mapping:relational-otherwise-embedded` | ✅ Phase A |
| `mapping:relational-polymorphic-query` | ✅ Phase A |
| `mapping:relational-primary-key` | ✅ Phase A |
| `mapping:relational-single-table-inheritance` | ✅ Phase A |
| `mapping:relational-table-alias-column` | ✅ Phase A |
| `mapping:router-union` | ✅ Phase A |
| `mapping:store-union` | ✅ Phase A |
| `mapping:mapping` | ✅ Phase D (all three M2M models); previously ▲ framework-only |
| `mapping:m2m-derived-source-property` | ✅ Phase D (`m2m-derived-source-property`) |
| `mapping:m2m-transform` | ✅ Phase D (`m2m-transform`) |
| `mapping:relational-literal-list` | ⛔ blocked by an engine defect — note 2 under §3.1 |
| `mapping:m2m-local-property` | ⛔ not provable under single-connection M2M test data — §3.4a |
| `mapping:operation-mapping-merge` | ⛔ not provable under single-connection M2M test data — §3.4a |
| `mapping:operation-mapping-merge-validation` | ⛔ not provable under single-connection M2M test data — §3.4a |
| `mapping:cross-store` | ❌ |

**Relation-function mappings**

| Capability | Status |
|---|---|
| `mapping:relation-embedded` | ✅ `relation-embedded`, `relation-union-enum` |
| `mapping:relation-expression-rhs` | ✅ `relation-expression-rhs` |
| `mapping:relation-filter` | ✅ `relation-filter` (source-relation `->filter`; §3.0 taxonomy addition) |
| `mapping:relation-group-by` | ✅ `relation-groupBy` (source-relation `->groupBy`; §3.0 taxonomy addition) |
| `mapping:relation-inline-embedded` | ✅ `relation-inline-embedded` |
| `mapping:relation-local-property` | ✅ `relation-{join,modelJoin,modelJoin-chained,window-function,mixed-association-chain}` |
| `mapping:relation-model-join` | ✅ `relation-{modelJoin,modelJoin-chained,window-function,milestoning-modelJoin-asymmetric}` (ModelJoin only) |
| `mapping:relation-src` | ✅ `relation-src` |
| `mapping:relation-union` | ✅ `relation-union`, `relation-union-enum`, `relation-relational-union` |
| `mapping:relation-window-function` | ✅ `relation-window-function` |
| `mapping:relation-xstore-association` | ✅ `relation-join`, `relation-mixed-association-chain` (XStore, distinct from ModelJoin; §3.0 taxonomy addition + finding — see §2.10(b)) |
| `mapping:relation-primary-key` | ✅ `relation-primary-key` (single + composite `~primaryKey: [...]`; Phase B′) |
| `mapping:relation-binding-transformer` | ❌ **real gap** — deferred to §3.7 external-format batch (see §3.1b) |

> Note: `relational-joins` navigates an association via `[db]@Join` property
> mappings but is tagged `store:relational-inner-join` + `grammar:association`,
> **not** `mapping:relational-association-implementation`. Phase A's
> `relational-association-implementation` now covers that capability directly.

### 2.4 Store — 9 / 13 covered (2 out-of-scope — §3.2)

| Capability | Status |
|---|---|
| `store:relational-filter` | ✅ |
| `store:relational-inner-join` | ✅ |
| `store:relational-multi-table` | ✅ |
| `store:relational-cross-schema` | ✅ Phase B (`relational-cross-schema`) |
| `store:relational-cross-table-filter` | ✅ Phase B (`relational-cross-table-filter`) |
| `store:relational-dyna-function` | ✅ Phase B (`relational-dyna-function`) |
| `store:relational-inline-view` | ✅ Phase B (`relational-inline-view`, `relational-dyna-function`) |
| `store:relational-left-outer-join` | ✅ Phase B (`relational-left-outer-join`) |
| `store:relational-nested-join` | ✅ Phase B (`relational-nested-join`) |
| `store:relational-outer-join` | ⛔ not a real classic-store capability — only INNER/OUTER exist (§3.2) |
| `store:relational-right-outer-join` | ⛔ not a real classic-store capability — only INNER/OUTER exist (§3.2) |
| `store:service-store` | ❌ (needs new module — §3.8) |
| `store:flat-data-store` | ❌ (needs new module — §3.8) |

### 2.5 Milestoning — 6 / 7 covered (1 out-of-scope — §3.3)

| Capability | Status |
|---|---|
| `milestoning:business-temporal` | ✅ `relation-milestoning` (`BusinessTemporalEmployee.all(%date)`), `relation-milestoning-modelJoin-asymmetric` |
| `milestoning:processing-temporal` | ✅ `relation-milestoning`, `relation-milestoning-modelJoin-asymmetric` |
| `milestoning:point-in-time-query` | ✅ `relation-milestoning` (`all(%date)`), asymmetric (independent as-of dates per side) |
| `milestoning:all-versions-query` | ✅ `relation-milestoning` (`allVersions()`) |
| `milestoning:milestoning` | ✅ both of the above; also `milestoning-bitemporal` |
| `milestoning:bi-temporal` | ✅ Phase C (`milestoning-bitemporal`, table-backed `<<temporal.bitemporal>>` + `all(processingDate, businessDate)`) |
| `milestoning:all-versions-in-range-query` | ⛔ not supported in the Legend/Studio grammar — `DomainParseTreeWalker.allOrFunction` explicitly rejects `.allVersionsInRange(...)` (§3.3) |

> This is the *class/relational* milestoning domain (temporal classes + temporal
> query functions), distinct from the persistence temporal capabilities in §2.7
> which are fully covered.
>
> **This domain was the sharpest example of the §2.10 metadata problem.** Both
> models declared `grammar:milestoning` — a value not in the taxonomy at all — so
> five genuinely-covered capabilities were invisible to this matrix and the domain
> read "0 / 7, entire domain uncovered". §3.0 split that tag into the specific
> `milestoning:*` values each source actually exercises (confirmed against the
> `.pure`: `relation-milestoning` queries both processing- **and** business-temporal
> classes plus `allVersions()`/`all(%date)`, so it alone earns all five), so the
> coverage above is now machine-checkable. Phase C shrinks accordingly — see §3.3.

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

| Domain | Covered / Total | Was (pre-Phase A) |
|---|---|---|
| Scaffolding | 9 / 9 | 7 / 7 |
| Grammar | 10 / 10 | 3 / 10 |
| Mapping | 34 / 40 (4 out-of-scope — §3.1 note 2, §3.4a) | 1 / 27 |
| Store | 9 / 13 (2 out-of-scope — §3.2) | 3 / 13 |
| Milestoning | 6 / 7 (1 out-of-scope — §3.3) | 0 / 7 |
| Execution | 5 / 17 (1 out-of-scope — §2.9) | 5 / 17 |
| Persistence | 12 / 12 | 12 / 12 |
| **Total** | **85 / 108** | **31 / 93** |

Totals grew because the relation-function work added 12 real capabilities to the
taxonomy (§6.2 of `emit.md`) as well as covering them, and §3.0 added 3 more
(`mapping:relation-filter`, `mapping:relation-group-by`,
`mapping:relation-xstore-association`) while normalizing the relation batch —
all three immediately covered.

The picture has changed substantially. **Mapping is no longer the hole** — it went
from 1/27 to 34/40 across Phase A, the relation batch and Phase D. **Grammar is
closed** at 10/10 and **milestoning is nearly closed** rather than untouched. There
is now exactly one concentration left:

- **Execution — 5 / 17.** Gated on new modules almost entirely (§3.5–§3.9). This is
  the only remaining concentration.
- **Store — 9 / 13 (Phase B done).** All six authorable join/store-shape features
  landed (§3.2); the two remaining real gaps (`service-store`, `flat-data-store`)
  need new modules (§3.8), and right-/full-outer join are out of scope (no
  classic-store grammar).
- **Mapping — 34 / 40.** Phase D closed the executable part of the M2M corner. Of
  the six uncovered, four are out of scope: `mapping:relational-literal-list` (engine
  defect) and the three §3.4a capabilities that single-connection M2M test data
  cannot prove. The two real gaps left are `mapping:cross-store` (§3.10) and
  `mapping:relation-binding-transformer` (§3.7) — and closing the first would also
  give the §3.4a three a home.

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

### 2.10 Metadata debt in the relation batch

The 22 relation descriptors are sound as *tests* — all 211 dynamic tests pass and
the `.pure` sources reach real capability. But their metadata diverged from the
taxonomy in four ways, and because §2 is computed from metadata, the divergence
made covered capabilities read as gaps. **§3.0 has now been applied**; this
subsection records the diagnosis, and the divergences below are resolved.

**(a) Twelve off-taxonomy feature values.** None of these exist in `emit.md` §6.2
as it stood; the "add the entry in the same PR" rule (§6.2, *Evolving the
Taxonomy*) was not applied. Three collide with entries that already existed:

| Used | Should be | Why |
|---|---|---|
| `grammar:enumeration-mapping` | `mapping:enumeration-mapping` | already in the taxonomy; store-agnostic |
| `grammar:mapping-include` | `mapping:mapping-include` | already in the taxonomy; store-agnostic |
| `grammar:milestoning` | `milestoning:{business-temporal,processing-temporal,point-in-time-query,all-versions-query,milestoning}` | milestoning has its own domain; this hid 5 covered capabilities |
| `grammar:embedded-relation` | `mapping:relation-embedded` | mapping-level concern |
| `grammar:embedded-relation-inline` | `mapping:relation-inline-embedded` | mapping-level concern |
| `grammar:relation-expression-rhs` | `mapping:relation-expression-rhs` | mapping-level concern |
| `grammar:relation-src` | `mapping:relation-src` | mapping-level concern |
| `grammar:relation-union` | `mapping:relation-union` | mapping-level concern |
| `grammar:window-function` | `mapping:relation-window-function` | mapping-level concern |
| `store:relation-model-join` | `mapping:relation-model-join` **or** `mapping:relation-xstore-association` | declared in the `Mapping`, not the store; **and** the mechanism differs by model — `relation-{modelJoin,modelJoin-chained,window-function,milestoning-modelJoin-asymmetric}` use `ModelJoin`, while `relation-join` and `relation-mixed-association-chain` use `XStore`. The single old tag conflated the two; §3.0 splits them (see (b)) |
| `scaffolding:relation-function` | *(registered as-is)* | now in the taxonomy |
| `scaffolding:relation-mapping` | *(registered as-is)* | now in the taxonomy |

All twelve are now registered in `emit.md` §6.2, in normalized form.

**(b) Two clusters of exact-duplicate feature sets.** `emit-authoring.md` §11.2
treats an exact sorted feature-set match as a duplicate. Six descriptors form two
3-way collisions:

- `relation-simple` ≡ `relation-filter` ≡ `relation-groupBy`
- `relation-join` ≡ `relation-modelJoin` ≡ `relation-modelJoin-chained`

The tests are genuinely distinct — the collisions were caused by missing tags, not
by redundant tests. §3.0 broke them: `relation-filter` gained `mapping:relation-filter`
and `relation-groupBy` gained `mapping:relation-group-by` (both new §6.2 entries for
source-relation operations). For the second cluster, reading the sources corrected a
mistaken premise in an earlier draft of this plan — the three are **not** three
ModelJoins differing by hop count. `relation-join` uses an `XStore` association,
`relation-modelJoin` a single-hop `ModelJoin`, and `relation-modelJoin-chained` a
two-hop `ModelJoin` chain. So the distinguishers are `mapping:relation-xstore-association`
(new §6.2 entry) for `relation-join`, `mapping:relation-model-join` for the other two,
plus `mapping:relation-local-property` on all three and `grammar:{association,nested-association}`
on the chained one. (Had they all been tagged `mapping:relation-model-join` + local-property,
`relation-join` and `relation-modelJoin` would have stayed identical.)

**(c) Under-tagged sources.** `mapping:relation-local-property` (`+prop: Type[m]:
rhs`) is exercised by five models and claimed by none. `grammar:function` is
exercised by every `~func` model and claimed by none. `relation-window-function`
uses a ModelJoin association but does not tag it (its free-form `tags:` say
`modeljoin`, so the omission is in `features:` only).

**(d) One descriptor was not discovered at all.**
`relational-emit-models/relational-service-with-join.yaml` was missing the `.emit`
infix. `EMITModelDiscovery.EMIT_YAML_SUFFIX` is `".emit.yaml"`, so the file was
skipped silently — its four `.pure` sources under `relational-service-with-join/`
never parsed, compiled, or executed, and its `features:` list still used the
pre-taxonomy unqualified form (`class`, `association`, `service`, …). §3.0 renamed
it to `.emit.yaml` and converted its features to `domain:capability` form. On its
first-ever execution it discovered, parsed, compiled, ran its service test suite,
and generated a plan — all green (the `RelationalEMITTests` count rose 178 → 186,
descriptors 24 → 25), so no source repair was needed.

---

## 3. Proposed New Tests

Each row below is a proposed descriptor: a name, the capability gap(s) it closes,
its non-scaffolding feature set, and its target module. Feature sets were checked
against §1's inventory for exact-match duplicates per `emit-authoring.md` §11.2 —
none duplicate an existing set. Subset/superset relationships are intentional
(they provide distinct regression coverage). Note that §1's inventory itself
contains two exact-match clusters today, in the relation batch — see §2.10(b);
those are tagging omissions to fix in §3.0, not redundant tests. `.pure` authoring follows
`emit-authoring.md` §4; reuse `relational-shared-domain` /
`relational-shared-firm-db` via `dependencies` wherever the domain fits.

Complexity is derived mechanically (distinct non-scaffolding domains: 1–2 basic,
3–4 intermediate, 5+ advanced).

### 3.0 Metadata normalization → `legend-engine-xt-relationalStore-emit` — **DONE**

No new `.pure` sources and no new tests — this was a metadata-only pass that makes
§2 true and machine-checkable. **Applied.** All 22 relation descriptors were
re-tagged, three new §6.2 entries were registered (`mapping:relation-filter`,
`mapping:relation-group-by`, `mapping:relation-xstore-association`), and the dead
descriptor was revived (§2.10(d)). Both suites stayed green — `RelationEMITTests`
211/211 (unchanged), `RelationalEMITTests` 186/186 (up from 178, +8 from the revived
descriptor). Details and findings in §2.10.

| Item | Work |
|---|---|
| Re-tag 12 off-taxonomy values | Apply the §2.10(a) mapping across the 22 relation descriptors |
| Split the milestoning tag | `grammar:milestoning` → the five specific `milestoning:*` values the sources actually exercise |
| Break the duplicate feature sets | §2.10(b) — add the missing distinguishing tags to 6 descriptors |
| Apply missing tags | `mapping:relation-local-property` (5 models), `grammar:function` (all `~func` models), model-join on `relation-window-function` |
| Fix the dead descriptor | Rename `relational-service-with-join.yaml` → `.emit.yaml`, convert its `features:` to `domain:capability` form, then verify it actually passes |
| Re-derive complexity | Several relation models are `basic` only because their tags collapse to two domains; re-score mechanically after re-tagging |

> The dead-descriptor fix is the one item here that may not be purely mechanical —
> `relational-service-with-join` has never been executed, so renaming it will run
> its sources for the first time and they may need repair.

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

### 3.1b Relation-function mapping gaps → `legend-engine-xt-relationalStore-emit`

The relation batch covers 8 of the 10 relation-mapping capabilities. Two grammar
productions in `RelationFunctionMappingParserGrammar.g4` have **no** exercising
model — verified by reading every `.pure` file under `relation-emit-models/`.
Both go in `relation-emit-models/` and can reuse `relation-shared-*`.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `relation-primary-key` — **DONE** | `mapping:relation-primary-key` | `execution:{data-element,test-data}`, `grammar:function`, `mapping:relation-primary-key` |
| `relation-binding-transformer` — **deferred to §3.7** | `mapping:relation-binding-transformer` | `execution:{data-element,test-data,binding,external-format}`, `grammar:function`, `mapping:relation-binding-transformer` |

> `~primaryKey` here is the **relation** form — `~primaryKey: [ID]` or
> `~primaryKey: [ID, LAST_NAME]`, a colon followed by a bracketed list of bare
> column identifiers (grammar rule `primaryKey`), resolved at compile time against
> the `~func`'s RelationType. It is *not* the relational `~primaryKey ([db]Table.COL)`
> form, which `relation-relational-union` and `relation-mixed-association-chain`
> already use on their **relational** set implementations. `relation-primary-key`
> landed with two mappings exercising the single- and composite-column forms
> (RelationEMITTests 211 → 221).
>
> **`relation-binding-transformer` — verified, deferred to §3.7.** The gate was
> whether the module classpath carries a binding/external-format extension. It
> does — `legend-engine-xt-json-model` registers `JsonExternalFormatExtension` via
> SPI, alongside `legend-engine-external-format-core` and the JSON runtime
> functions. But two things push this test to the §3.7 external-format batch
> rather than here: (1) that extension is JSON **Schema** (schema/model generation),
> so a working transformer needs a `SchemaSet` + `Binding` + a JSON-string source
> column + execution-time deserialization — external-format authoring, not a quick
> relation-suite add; and (2) there is **no relation-form (`~func`)
> `bindingTransformer` example anywhere in the repo** to mirror — every
> `Binding <qn>: COLUMN` occurrence is on a *relational* (classic) set
> implementation. Landing it blind here risks a rabbit hole; §3.7 owns the
> external-format dependency footprint and should carry it. Only `relation-primary-key`
> landed in B′.

### 3.2 Relational store features → `legend-engine-xt-relationalStore-emit` — **DONE (6 of 8; 2 not real)**

Also no new module. A shared multi-schema / multi-join store
(`relational-shared-joins` — Employee/Department/Sale domain + a two-schema
`demo::store::JoinsDB` with an intra-schema `Emp_Dept` join, a cross-schema
`Emp_Sale` join, and a `NycDeptFilter`) was added as a reusable dependency (like
`relational-shared-firm-db`) and consumed by the join/filter tests. All six
authorable tests landed and pass (`RelationalEMITTests` 186 → 232).

**Rescoped from 8 tests to 6.** Verifying the grammar first (working rule 1)
showed the classic relational store supports **only `INNER` and `OUTER`** join
types (`RelationalParseTreeWalker.JOIN_TYPES = {"INNER","OUTER"}`); `OUTER` from
the main side is a *left* outer join. There is no `RIGHT` or `FULL` keyword in
the classic store/mapping path — `RIGHT_OUTER`/`FULL_OUTER` exist only in the
relation/TDS `->join(JoinKind…)` function (query-function territory, PCT's job),
not as a `store:relational-*` structural feature. So `store:relational-right-outer-join`
and `store:relational-outer-join` (full) are **not real classic-store capabilities**
— treated like `mapping:relational-literal-list` (⛔ in §2.4), not authored here.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `relational-left-outer-join` | `store:relational-left-outer-join` | `execution:{data-element,test-data}`, `grammar:association`, `store:relational-left-outer-join` |
| ~~`relational-right-outer-join`~~ | — ⛔ not a real classic-store capability (INNER/OUTER only) | — |
| ~~`relational-outer-join`~~ | — ⛔ not a real classic-store capability (INNER/OUTER only) | — |
| `relational-nested-join` | `store:relational-nested-join` | `execution:{data-element,test-data}`, `grammar:nested-association`, `store:relational-nested-join` |
| `relational-cross-schema` | `store:relational-cross-schema` | `execution:{data-element,test-data}`, `store:{relational-cross-schema,relational-inner-join}` |
| `relational-cross-table-filter` | `store:relational-cross-table-filter` | `execution:{data-element,test-data}`, `store:{relational-cross-table-filter,relational-inner-join}` |
| `relational-inline-view` | `store:relational-inline-view` | `execution:{data-element,test-data}`, `store:relational-inline-view` |
| `relational-dyna-function` | `store:relational-dyna-function` | `execution:{data-element,test-data}`, `store:relational-dyna-function` |

### 3.3 Class/relational milestoning → `legend-engine-xt-relationalStore-emit` — **DONE (1 of 2; 1 not supported)**

**Rescoped from 4 tests to 2, then to 1.** The relation batch already covers
business-temporal, processing-temporal, point-in-time query, all-versions query,
and the generic marker (§2.5); §3.0 made that visible. Two capabilities remained,
but verifying the grammar (working rule 1) showed only one is authorable.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `milestoning-bitemporal` — **DONE** | `milestoning:bi-temporal` | `execution:{data-element,test-data}`, `milestoning:{bi-temporal,point-in-time-query,milestoning}` |
| ~~`milestoning-all-versions-in-range`~~ | — ⛔ `.allVersionsInRange(...)` not supported in the Legend grammar | — |

> `milestoning-bitemporal` was authored against a **relational** (table-backed)
> mapping in `relational-emit-models/`, not a relation-function mapping — deliberate
> duplication of concern: milestoning was previously proven only over `~func`
> sources, so this establishes the table-backed milestoning router path. The
> bitemporal PRODUCT table declares `milestoning ( business(...), processing(...) )`
> (comma-separated specs), the class carries `<<temporal.bitemporal>>`, and the
> query is `all(processingDate, businessDate)`. That single table-backed test also
> covers the business/processing/point-in-time path over the classic store, so no
> separate `relational-milestoning-business-temporal` descriptor is needed.
>
> **`milestoning:all-versions-in-range-query` is not EMIT-testable.**
> `.allVersionsInRange(...)` parses in the M3 grammar but the Legend/Studio grammar
> walker (`DomainParseTreeWalker.allOrFunction`) explicitly throws "… is not
> supported" for it (only `all()`, `allVersions()`, `all(%d)`, `all(%d1,%d2)` are
> honoured). Same category as right-/full-outer join (§3.2) and
> `mapping:relational-literal-list` — a taxonomy entry with no expressible
> Legend-grammar construct. Marked ⛔ in §2.5.

### 3.4 Grammar-only + M2M mapping → new `legend-engine-core-emit-tests` — **DONE (10 of 11; 1 not executable)**

These need only the core compiler + M2M classpath. **Applied.** The module was stood
up as `legend-engine-core/legend-engine-core-emit-tests` — a leaf module and a
*sibling* of `legend-engine-core-emit`, not a child of it, so a catalog module is not
mistaken for one of the four framework modules inside that aggregator. All 10
descriptors that could be made to prove something landed and pass: 52 dynamic tests,
checkstyle clean.

The name was changed from the originally-proposed `legend-engine-emit-m2m` because
seven of the ten fixtures are core-language rather than M2M, and the module is
expected to keep accreting store-free fixtures.

The module hosts two independently-runnable suites over two resource roots, split by
subject so each area can be run on its own and a failure is attributable to one of
them (`emit.md` §3.2):

| Root | Suite | Models |
|---|---|---|
| `grammar-emit-models/` | `GrammarEMITTests` | 7 language-construct models (parse + compile) |
| `m2m-emit-models/` | `M2MEMITTests` | 3 model-to-model mappings (executing test suites) |

Both roots are disambiguated, so both needed an explicit `includedRelativeSubpaths`
entry in `legend-engine-server-http-server`'s pom — see the §5.4 caveat and the
updated table there.

> **Dependency note.** The seven grammar-only fixtures need nothing beyond the
> compiler. The four M2M fixtures execute their test suites, which requires the same
> test-scoped profile `legend-engine-emit` uses for its own m2m bootstrap fixtures:
> `legend-engine-executionPlan-execution-store-inMemory`,
> `legend-engine-external-format-core`,
> `legend-engine-pure-runtime-java-extension-compiled-functions-json` (all core),
> plus `legend-engine-xt-json-model`, `legend-engine-xt-javaPlatformBinding-pure` and
> `legend-engine-configuration-plan-generation-serialization` (xts and config). So a
> module in the core tree does invert the documented `config → xts → core` direction
> at test scope — copying, not extending, the precedent `legend-engine-emit` already
> set. A strictly extension-free core module would have to host the grammar fixtures
> alone and exile the M2M half.

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
| `m2m-transform` | `mapping:m2m-transform` | `execution:test-data`, `mapping:{mapping,m2m-transform}` |
| ~~`m2m-local-property`~~ | — ⛔ not executable, see §3.4a | — |
| `m2m-derived-source-property` | `mapping:m2m-derived-source-property` | `execution:test-data`, `grammar:{derived-property,qualified-property}`, `mapping:{mapping,m2m-derived-source-property}` |
| `m2m-enumeration-mapping` | `mapping:enumeration-mapping` (M2M variant) | `execution:test-data`, `grammar:enumeration`, `mapping:{mapping,enumeration-mapping}` |

The three delivered M2M models embed their test data inline as `ExternalFormat` blocks
rather than referencing a `Data` element, so they carry `execution:test-data` but not
`execution:data-element` as originally proposed.

### 3.4a Not testable: capabilities blocked by single-connection M2M test data

Three capabilities intended for Phase D have **no distributed example and are not
gettable one**, for a single shared reason. They are recorded here rather than covered
by a fixture, and are marked ⛔ in §2.3.

**The constraint.** An M2M mapping test suite can supply exactly **one** source
connection. `ModelStoreTestConnectionFactory.buildModelStoreConnectionsForStore`
iterates the `ModelStore` data block's entries but `return`s inside the loop on the
first one, and `buildCloseableConnectionFromExternalFormat` installs a single
`StreamProviderHolder` thread-local stream. So:

1. A `ModelStore: ModelStore #{ ClassA: …, ClassB: … }#` block silently uses only
   `ClassA` — multi-source-class M2M models cannot be driven from mapping test data.
2. Any query whose plan crosses two set implementations produces an
   `InMemoryCrossStoreGraphFetchExecutionNode` with one store read per side. The first
   read consumes the only stream and the second fails with `RuntimeException: Input
   stream was not provided`.

This behaviour is **known and deliberately not being changed** — other code depends on
its particulars, and unpicking it is more involved than it first appears. Treat it as
a fixed property of the harness when planning M2M coverage, not as a defect to route
around.

**What that blocks.**

| Capability | Why it cannot be proven | Where it could be covered |
|---|---|---|
| `mapping:m2m-local-property` | A local mapping property (`+prop: T[m]: $src…`) exists on the set implementation, not the class. In a pure-M2M model its only consumer is a cross-set (XStore) association, and executing that traversal needs a second connection. | §3.10 (Phase J) `mapping:cross-store`, where a real second store supplies the other side |
| `mapping:operation-mapping-merge` | Merge is defined over set implementations reading **different** source classes — three sources in the canonical form. Constraint (1) means only the first is ever populated. | §3.10 (Phase J), or any future harness that admits multiple M2M connections |
| `mapping:operation-mapping-merge-validation` | Same shape as merge, differing only in the cross-set predicate. | as above |

**No compile-only fixtures were added for these.** A model that parses and compiles but
never executes the capability under test would register as coverage in §2 while proving
nothing about the feature's behaviour — a false green is worse than an honest ❌. The
grammar for all three is real and verified (merge round-trips in
`TestMappingGrammarRoundtrip#testMergeModelMapping`; the local-property and XStore forms
appear in `core/store/m2m/tests/simple.pure`), so authoring is not the obstacle —
executability is.

> A `m2m-local-property` fixture was written during Phase D and then removed for exactly
> this reason: with both set implementations reading one flat source class it compiled
> and its non-crossing query executed, but the local property itself was never exercised
> at run time, so the descriptor would have claimed a capability it did not test.

> Note 1 under §3.1 had routed the two merge capabilities to this batch after finding
> them M2M-only rather than relational. That routing was correct about the domain; the
> §3.4 table was never extended with them, and on investigation they are not
> executable here either. They stay ❌ until Phase J.

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
| `relation-binding-transformer` (routed from §3.1b) | `mapping:relation-binding-transformer`, `execution:{binding,external-format}` | `execution:{data-element,test-data,binding,external-format}`, `grammar:function`, `mapping:relation-binding-transformer` |

> `service-with-binding` (existing) uses `execution:external-format-binding`;
> the taxonomy also has the distinct `execution:binding` and
> `execution:external-format` / `execution:schema-set` tags, which are the true
> gaps here.
>
> `relation-binding-transformer` was scoped to B′ (§3.1b) but moved here: the
> relation `Binding <qn>:` transformer needs a `SchemaSet` + `Binding` + a
> JSON-string source column + execution-time deserialization, and no relation-form
> example exists to mirror — external-format work best owned by this batch, on a
> module whose classpath is built for it. It stays in `relation-emit-models/` if
> the chosen host is `legend-engine-xt-relationalStore-emit` with the needed
> external-format deps added; otherwise it lands in the format's own `-emit` module.

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
| **A′** | Relation-function mappings (22 tests — **done**, landed separately) | No | 8 mapping + 5 milestoning | — delivered outside this plan |
| **A″** | §3.0 Metadata normalization (**0 tests**, metadata only — **done**) | No | made 13 already-covered capabilities visible + added 4 newly-covered (`grammar:nested-association`, `mapping:relation-{filter,group-by,xstore-association}`) + revived the dead descriptor | Low — done first |
| **B** | §3.2 Relational store features (6 of 8 tests — **done**; right-/full-outer not real) | No | 6 store capabilities (cross-schema, cross-table-filter, dyna-function, inline-view, left-outer-join, nested-join) | Low |
| **B′** | §3.1b Relation-function gaps (1 of 2 tests — `relation-primary-key` **done**; `relation-binding-transformer` moved to §3.7/Phase G) | No | 1 mapping (`mapping:relation-primary-key`) | Low |
| **C** | §3.3 Milestoning (1 test — **done**; all-versions-in-range not supported in Legend grammar) | No | `milestoning:bi-temporal` + table-backed milestoning path | Low–Med |
| **D** | §3.4 Grammar + M2M (10 of 11 tests — **done**; 1 not executable, §3.4a) | `legend-engine-core-emit-tests` (renamed + relocated — §3.4) | 4 grammar + 3 mapping | Med — 1 module |
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

The relation batch added 12 entries to `emit.md` §6.2 **after** the fact (§2.10);
that retrofit is the cautionary case for this section. Beyond it, no new taxonomy
entries are required to close the §2 gaps — every remaining proposed test maps to
an existing `domain:capability`. Two optional additions to consider in the owning
PRs:

- A `legacy` marker (or `execution:legacy-mapping-test` / `execution:legacy-service-test`)
  to distinguish the legacy Phase-5 runner coverage in §3.5.
- Apply the already-defined `execution:plan-generation` tag to service-bearing
  models (it exists in the taxonomy but is currently applied nowhere).

Any genuinely new capability discovered while authoring must be added to
`emit.md` §6.2 **in the same PR** as the test (`emit-authoring.md` §10).

---

## 5. Summary

- **85 of 108** taxonomy capabilities have a distributed example today, up from
  31 of 93. Phase A closed 19; the separately-landed relation-function batch
  closed 13 more and added 12 capabilities to the taxonomy; §3.0 then added 3 more
  (all immediately covered) and made 14 already-covered-but-mistagged capabilities
  machine-visible; Phase B closed 6 store features, Phase B′ 1 relation-mapping,
  Phase C 1 milestoning, and Phase D 7 (4 grammar + 3 mapping, including promoting
  `mapping:mapping` from framework-only to a distributed example).
- Of the 23 uncovered, **eight are not real targets** —
  `execution:model-generation` has no implementation (§2.9),
  `mapping:relational-literal-list` is blocked by an engine defect (note 2 under
  §3.1), `store:relational-{right-outer,outer}-join` have no classic-store
  grammar (§3.2), `milestoning:all-versions-in-range-query` is rejected by the
  Legend grammar (§3.3), and `mapping:m2m-local-property` /
  `mapping:operation-mapping-merge` / `mapping:operation-mapping-merge-validation`
  cannot be executed under single-connection M2M test data (§3.4a) — leaving
  **15 real gaps**.
- **Grammar is closed (10/10); mapping (35/40) and store (9/13) are no longer weak
  domains.** The one remaining concentration is **execution (5/17, mostly
  module-gated)**; milestoning is 6/7 (all-versions-in-range out of scope).
- **§3.0 (done) added no tests** but was the highest-priority item: fourteen
  capabilities were covered by passing tests yet invisible to §2 because of
  off-taxonomy tags, and one descriptor (`relational-service-with-join.yaml`) did
  not run at all because its filename lacked the `.emit` infix. Both are now fixed.
- **Phases B, B′, C and D are done** (6 store tests + 1 relation-mapping + 1
  milestoning + 10 core-feature tests). Phase D stood up the first new module and
  closed the grammar domain outright. Every remaining real gap needs a new `-emit`
  module (E–I: service shapes, file generation, external format, other stores,
  function activators) or is a cross-feature combo (J).
- **§3.4a is the one place where a batch shipped smaller than planned for a reason
  other than the feature being unreal.** Local properties and merge operations are
  genuine, working engine features; what is missing is a way to *execute* them from
  an M2M mapping test, because `ModelStoreTestConnectionFactory` supplies a single
  source connection and that behaviour is deliberately staying as it is. No
  compile-only fixtures were substituted, so §2 does not overstate coverage. Phase J's
  cross-store work is where these three become provable.
- Every real feature has a distributed example at the end of Phase I; the
  remaining work is combination-level and incremental. Model generation is
  revisited only if a real extension ships.

### 5.1 Structural note — two suites, one module

`legend-engine-xt-relationalStore-emit` now hosts two independent suites over two
resource roots (`relational-emit-models/` + `RelationalEMITTests`,
`relation-emit-models/` + `RelationEMITTests`). This is a good split: relation
mappings are a distinct engine surface with their own grammar
(`RelationFunctionMappingParserGrammar.g4`), and separate roots keep a failure
attributable to one of them.

The two suites should stay **conceptually parallel but not duplicative**. Where a
capability is store-agnostic (mapping include, enumeration mapping, unions,
milestoning), the right pattern is one test per *code path* — not one per suite
by reflex, and not one shared test that leaves the other path unproven. §3.3's
recommendation to add table-backed milestoning coverage is exactly this case:
milestoning is currently proven only over `~func` sources.

`relation-mixed-association-chain` and `relation-relational-union` are worth
calling out as the models that deliberately span both surfaces in one mapping.
They are the most valuable tests in the relation batch and have no counterpart in
the relational suite.
