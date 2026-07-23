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

**Fifty-six** distributed descriptors exist across three modules — 46 of them in
the relational module, which now hosts two independent suites over two resource
roots. Shared-dependency bundles (`relational-shared-domain`,
`relational-shared-firm-db`, `relation-shared-domain`, `relation-shared-db`,
`relation-shared-data`) are reusable and only run parse + compile on their own.

### 1.1 `legend-engine-xt-relationalStore-emit` — `relational-emit-models/`

Driven by `RelationalEMITTests`. 24 descriptors, 178 dynamic tests. The seven
below predate the Phase A batch; the 17 added by Phase A are listed in §3.1.

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `relational-shared-domain` | `grammar:association`, `grammar:derived-property` | basic |
| `relational-shared-firm-db` | `store:relational-inner-join`, `store:relational-multi-table` | basic |
| `relational-simple` | `execution:data-element`, `execution:test-data` | basic |
| `relational-filter` | `execution:data-element`, `execution:test-data`, `store:relational-filter` | basic |
| `relational-joins` | `execution:data-element`, `execution:test-data`, `grammar:association`, `store:relational-inner-join` | intermediate |
| `relational-enumeration` | `execution:data-element`, `execution:test-data`, `grammar:enumeration`, `mapping:enumeration-mapping` | intermediate |
| `relational-service` | `execution:service`, `execution:service-test` | basic |

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

### 1.5 Test-hosting modules that exist today

Only three modules currently host distributed EMIT tests:

- `legend-engine-xt-relationalStore-emit` (two suites: `relational-emit-models/`
  via `RelationalEMITTests`, `relation-emit-models/` via `RelationEMITTests`)
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

### 2.2 Grammar — 5 / 10 covered

| Capability | Status |
|---|---|
| `grammar:association` | ✅ |
| `grammar:class-inheritance` | ✅ Phase A (`relational-{single-table,joined-table}-inheritance`, `relational-{operation-mapping,polymorphic-query}`) |
| `grammar:derived-property` | ✅ |
| `grammar:enumeration` | ✅ |
| `grammar:function` | ✅ relation suite (every `~func` model declares a standalone Pure function) — **tag not yet applied** |
| `grammar:constraint` | ❌ |
| `grammar:measure` | ❌ |
| `grammar:nested-association` | ❌ |
| `grammar:profile` | ❌ |
| `grammar:qualified-property` | ❌ |

> `grammar:function` is the second instance of the §2.10 pattern: the capability
> is genuinely exercised by all 19 executable relation models, but no descriptor
> claims the tag, so it read as a gap. Counted as covered here on the strength of
> the sources; apply the tag as part of the §3.0 normalization.

### 2.3 Mapping — 27 / 37 covered

The relational half of this domain went from 1/27 to 17/27 in Phase A; the
relation-function half is new (10 entries, §6.2 of `emit.md`).

**Relational + store-agnostic**

| Capability | Status |
|---|---|
| `mapping:aggregation-aware-mapping` | ✅ Phase A |
| `mapping:enumeration-mapping` | ✅ |
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
| `mapping:mapping` | ▲ (framework `m2m-passing` / `m2m-mixed` only) |
| `mapping:relational-literal-list` | ⛔ blocked by an engine defect — note 2 under §3.1 |
| `mapping:cross-store` | ❌ |
| `mapping:m2m-derived-source-property` | ❌ |
| `mapping:m2m-local-property` | ❌ |
| `mapping:m2m-transform` | ❌ |
| `mapping:operation-mapping-merge` | ❌ (M2M-only — note 1 under §3.1) |
| `mapping:operation-mapping-merge-validation` | ❌ (M2M-only — note 1 under §3.1) |

**Relation-function mappings**

| Capability | Status |
|---|---|
| `mapping:relation-embedded` | ✅ `relation-embedded` (tagged `grammar:embedded-relation`) |
| `mapping:relation-expression-rhs` | ✅ `relation-expression-rhs` (tagged `grammar:relation-expression-rhs`) |
| `mapping:relation-inline-embedded` | ✅ `relation-inline-embedded` (tagged `grammar:embedded-relation-inline`) |
| `mapping:relation-local-property` | ✅ `relation-{join,modelJoin,modelJoin-chained,window-function,mixed-association-chain}` — **tag not yet applied** |
| `mapping:relation-model-join` | ✅ `relation-modelJoin*` etc. (tagged `store:relation-model-join`) |
| `mapping:relation-src` | ✅ `relation-src` (tagged `grammar:relation-src`) |
| `mapping:relation-union` | ✅ `relation-union`, `relation-union-enum`, `relation-relational-union` (tagged `grammar:relation-union`) |
| `mapping:relation-window-function` | ✅ `relation-window-function` (tagged `grammar:window-function`) |
| `mapping:relation-binding-transformer` | ❌ **real gap** — see §3.1b |
| `mapping:relation-primary-key` | ❌ **real gap** — see §3.1b |

> Note: `relational-joins` navigates an association via `[db]@Join` property
> mappings but is tagged `store:relational-inner-join` + `grammar:association`,
> **not** `mapping:relational-association-implementation`. Phase A's
> `relational-association-implementation` now covers that capability directly.

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

### 2.5 Milestoning — 5 / 7 covered

| Capability | Status |
|---|---|
| `milestoning:business-temporal` | ✅ `relation-milestoning-modelJoin-asymmetric` |
| `milestoning:processing-temporal` | ✅ `relation-milestoning`, `relation-milestoning-modelJoin-asymmetric` |
| `milestoning:point-in-time-query` | ✅ `relation-milestoning` (`all(%date)`), asymmetric (independent as-of dates per side) |
| `milestoning:all-versions-query` | ✅ `relation-milestoning` (`allVersions()`) |
| `milestoning:milestoning` | ✅ both of the above |
| `milestoning:bi-temporal` | ❌ |
| `milestoning:all-versions-in-range-query` | ❌ |

> This is the *class/relational* milestoning domain (temporal classes + temporal
> query functions), distinct from the persistence temporal capabilities in §2.7
> which are fully covered.
>
> **This domain is the sharpest example of the §2.10 metadata problem.** Both
> models declare `grammar:milestoning` — a value that is not in the taxonomy at
> all — so five genuinely-covered capabilities were invisible to this matrix and
> the domain still read "0 / 7, entire domain uncovered". The coverage above is
> asserted from the `.pure` sources, and becomes machine-checkable once the tags
> are normalized (§3.0). Phase C shrinks accordingly — see §3.3.

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
| Grammar | 5 / 10 | 3 / 10 |
| Mapping | 27 / 37 | 1 / 27 |
| Store | 3 / 13 | 3 / 13 |
| Milestoning | 5 / 7 | 0 / 7 |
| Execution | 5 / 17 (1 out-of-scope — §2.9) | 5 / 17 |
| Persistence | 12 / 12 | 12 / 12 |
| **Total** | **66 / 105** | **31 / 93** |

Totals grew because the relation-function work added 12 real capabilities to the
taxonomy (§6.2 of `emit.md`) as well as covering them.

The picture has changed substantially. **Mapping is no longer the hole** — it went
from 1/27 to 27/37 across Phase A and the relation batch. **Milestoning is nearly
closed** rather than untouched. The remaining concentrations are now:

- **Store — 3 / 13, entirely untouched.** The join-flavour and store-shape long
  tail (§3.2) is the single largest contiguous gap left, and it needs no new
  module. This is the highest-value next batch.
- **Execution — 5 / 17.** Gated on new modules for the most part (§3.5–§3.9).
- **Grammar — 5 / 10** and the M2M corner of mapping, both waiting on the
  `legend-engine-emit-m2m` module (§3.4).

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
the `.pure` sources reach real capability. But their metadata diverges from the
taxonomy in four ways, and because §2 is computed from metadata, the divergence
made covered capabilities read as gaps. Fixing it is §3.0.

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
| `store:relation-model-join` | `mapping:relation-model-join` | declared in the `Mapping`, not the store |
| `scaffolding:relation-function` | *(registered as-is)* | now in the taxonomy |
| `scaffolding:relation-mapping` | *(registered as-is)* | now in the taxonomy |

All twelve are now registered in `emit.md` §6.2, in normalized form.

**(b) Two clusters of exact-duplicate feature sets.** `emit-authoring.md` §11.2
treats an exact sorted feature-set match as a duplicate. Six descriptors form two
3-way collisions:

- `relation-simple` ≡ `relation-filter` ≡ `relation-groupBy`
- `relation-join` ≡ `relation-modelJoin` ≡ `relation-modelJoin-chained`

The tests are genuinely distinct — the collisions are caused by missing tags, not
by redundant tests. `relation-filter` needs a filter tag, `relation-groupBy` an
aggregation tag, and the modelJoin trio needs `mapping:relation-local-property`
plus something to separate single-hop from chained.

**(c) Under-tagged sources.** `mapping:relation-local-property` (`+prop: Type[m]:
rhs`) is exercised by five models and claimed by none. `grammar:function` is
exercised by every `~func` model and claimed by none. `relation-window-function`
uses a ModelJoin association but does not tag it (its free-form `tags:` say
`modeljoin`, so the omission is in `features:` only).

**(d) One descriptor is not discovered at all.**
`relational-emit-models/relational-service-with-join.yaml` is missing the `.emit`
infix. `EMITModelDiscovery.EMIT_YAML_SUFFIX` is `".emit.yaml"`, so the file is
skipped silently — its four `.pure` sources under
`relational-service-with-join/` never parse, compile, or execute. Its `features:`
list also uses the pre-taxonomy unqualified form (`class`, `association`,
`service`, …) rather than `domain:capability`, which is consistent with it never
having been loaded. This is a dead test, not a passing one.

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

### 3.0 Metadata normalization → `legend-engine-xt-relationalStore-emit`

No new `.pure` sources and no new tests — this is a metadata-only pass that makes
§2 true and machine-checkable. It should land **before** the next authoring batch,
while there are only 22 relation descriptors to touch. Details in §2.10.

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
| `relation-primary-key` | `mapping:relation-primary-key` | `execution:{data-element,test-data}`, `grammar:function`, `mapping:relation-primary-key` |
| `relation-binding-transformer` | `mapping:relation-binding-transformer` | `execution:{data-element,test-data,binding,external-format}`, `grammar:function`, `mapping:relation-binding-transformer` |

> `~primaryKey` here is the **relation** form — `~primaryKey: ID` or
> `~primaryKey: [ID, NAME]`, a colon followed by one or more bare column
> identifiers (grammar rule `primaryKey`). It is *not* the relational
> `~primaryKey ([db]Table.COL)` form, which `relation-relational-union` and
> `relation-mixed-association-chain` already use on their **relational** set
> implementations. Those two models are why a naive grep suggests coverage.
>
> `relation-binding-transformer` (grammar rule `bindingTransformer`,
> `Binding <qualifiedName>:` before a property RHS) doubles as external-format
> coverage — it would close `execution:binding` and `execution:external-format`
> from §2.6 as well, if the relational module's classpath carries the binding
> extension. **Verify that first**; if it does not, this test belongs with the
> §3.7 external-format batch instead, and only the `~primaryKey` test lands here.

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

**Rescoped from 4 tests to 2.** The relation batch already covers
business-temporal, processing-temporal, point-in-time query, all-versions query,
and the generic marker (§2.5) — it just tags them `grammar:milestoning`, so §3.0
must land for that coverage to be visible. Only two capabilities remain.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `milestoning-bitemporal` | `milestoning:bi-temporal` | `execution:{data-element,test-data}`, `milestoning:{bi-temporal,point-in-time-query,milestoning}` |
| `milestoning-all-versions-in-range` | `milestoning:all-versions-in-range-query` | `execution:{data-element,test-data}`, `milestoning:{business-temporal,all-versions-in-range-query,milestoning}` |

> Both should be authored against **relational** (table-backed) mappings in
> `relational-emit-models/`, not relation-function mappings. That is deliberate
> duplication of concern rather than redundancy: milestoning is currently proven
> only over `~func` sources, so the table-backed milestoning path — a different
> code path through the router — has no EMIT coverage at all. Consider a third
> descriptor, `relational-milestoning-business-temporal`, purely to establish
> that path, even though the *capability* tags would duplicate the relation
> models' (subset/superset overlap is explicitly allowed by
> `emit-authoring.md` §11.2; only exact set matches are duplicates, and the
> `stores`/scaffolding tags differ here anyway).

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
| **A′** | Relation-function mappings (22 tests — **done**, landed separately) | No | 8 mapping + 5 milestoning | — delivered outside this plan |
| **A″** | §3.0 Metadata normalization (**0 tests**, metadata only) | No | makes 13 already-covered capabilities visible | Low — but do it **first** |
| **B** | §3.2 Relational store features (8 tests) | No | 8 capabilities | Low |
| **B′** | §3.1b Relation-function gaps (2 tests) | No | 2 mapping (+2 execution if binding is on the classpath) | Low |
| **C** | §3.3 Milestoning (2–3 tests, rescoped from 4) | No | 2 capabilities + table-backed milestoning path | Low–Med — needs temporal query authoring |
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

- **66 of 105** taxonomy capabilities have a distributed example today, up from
  31 of 93. Phase A closed 19; the separately-landed relation-function batch
  closed 13 more and added 12 capabilities to the taxonomy in the process.
- Of the 39 uncovered, **two are not real targets** —
  `execution:model-generation` has no implementation (§2.9) and
  `mapping:relational-literal-list` is blocked by an engine defect (note 2 under
  §3.1) — leaving **37 real gaps**.
- **Mapping is no longer the weak domain** (27/37). The concentrations are now
  **store (3/13, untouched)** and **execution (5/17, mostly module-gated)**.
  Milestoning is 5/7 rather than 0/7.
- **§3.0 is the highest-priority item and adds no tests.** Thirteen capabilities
  are covered by passing tests but invisible to §2 because of off-taxonomy tags;
  one descriptor (`relational-service-with-join.yaml`) does not run at all
  because its filename lacks the `.emit` infix. Normalizing now, at 46
  descriptors, is far cheaper than after Phases B–J.
- **Phases B, B′, and C (12–13 tests) need no new modules** and should land next
  — §3.2 relational store features is the largest remaining no-new-module batch.
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
