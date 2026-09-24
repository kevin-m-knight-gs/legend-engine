# EMIT Coverage Plan — Closing the Feature-Combination Gaps

A companion to [`emit.md`](emit.md) (design/architecture) and
[`emit-authoring.md`](emit-authoring.md) (how to write a test). This document
takes inventory of the EMIT tests that exist **today**, measures that inventory
against the controlled feature taxonomy in `emit.md` §6.2, and lays out a
prioritized plan to add tests for the feature combinations that are not yet
covered.

> **Status (2026-09-24).** Phases A through E are done. Since Phase E, the catalog has
> grown from 82 to 100 distributed descriptors (617 to 901 dynamic tests, all passing),
> and every one of the 18 additions came from an engine feature PR rather than from a
> phase of this plan (§1.8). Taxonomy coverage stands at **102 / 122** (§2.8), and the
> 11 real gaps are the same 11 as at Phase E. This revision adds three things:
>
> - a second metadata pass, because the §2.10 debt has started to re-accumulate (§3.0a);
> - a realism review of the corpus. The matrix is close to full, but the typical fixture
>   exercises one capability on two classes with one assertion. Several completed phases
>   need revisiting, and three fixtures do not prove what they claim (§2.11, §3.11);
> - new work that follows from the review: fixture repairs (Phase K), pipeline paths
>   that no fixture reaches (Phase L, §3.12), and Studio-shaped composite models
>   (Phase M, §3.13). §4 re-sequences all the remaining phases.

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

**One hundred** distributed descriptors exist across five modules — 71 of them in
the relational module and 15 in the core-feature module, both of which host two
independent suites over two resource roots, 9 in persistence, 4 in the service
module added by Phase E, and 1 cross-feature. Shared-dependency bundles
(`relational-shared-domain`, `relational-shared-firm-db`, `relational-shared-joins`,
`relation-shared-domain`, `relation-shared-db`, `relation-shared-data`,
`service-shared-domain`) are reusable and only run parse + compile on their own.

Counts below were taken on 2026-09-24 by running all seven suites against
`4.149.1-SNAPSHOT` (`mvn test -pl` over the five modules). Every dynamic test passed.

| Suite (root → runner) | Module | Descriptors | Dynamic tests | At Phase E |
|---|---|---|---|---|
| `relational-emit-models/` → `RelationalEMITTests` | `legend-engine-xt-relationalStore-emit` | 48 | 485 | 34 / 245 |
| `relation-emit-models/` → `RelationEMITTests` | `legend-engine-xt-relationalStore-emit` | 23 | 221 | 23 / 221 |
| `grammar-emit-models/` → `GrammarEMITTests` | `legend-engine-core-emit-tests` | 7 | 28 | 7 / 28 |
| `m2m-emit-models/` → `M2MEMITTests` | `legend-engine-core-emit-tests` | 8 | 74 | 4 / 30 |
| `emit-models/` → `PersistenceEMITTests` | `legend-engine-xt-persistence-emit` | 9 | 46 | 9 / 46 |
| `emit-models/` → `ServiceEMITTests` | `legend-engine-xt-service-emit` | 4 | 31 | 4 / 31 |
| `emit-models/` → `CrossFeatureEMITTests` | `legend-engine-emit-tests` | 1 | 16 | 1 / 16 |
| **Total** | | **100** | **901** | **82 / 617** |

The per-suite tables below list the descriptors each phase of this plan delivered.
The 18 descriptors added since Phase E are listed separately in §1.8, because they
came from feature PRs rather than from the plan and differ from the phase-delivered
fixtures in character.

### 1.1 `legend-engine-xt-relationalStore-emit` — `relational-emit-models/`

Driven by `RelationalEMITTests`. 48 descriptors, 485 dynamic tests (34 and 245 at
Phase E). The eight below predate the Phase A batch; the 17 added by Phase A are
listed in §3.1; the 7 added by Phase B (a shared two-schema store bundle + 6
store-feature tests) are in §3.2; Phase C added `milestoning-bitemporal` (§3.3);
Phase E added `relational-legacy-mapping-test` (§3.5). The 14 added since Phase E
(thirteen semi-structured models and `relational-connection-time-zone`) are in
§1.8, and they account for most of the dynamic-test growth: the semi-structured
survey models run one suite per function. (`relational-service-with-join`
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
| `relational-legacy-mapping-test` (Phase E) | `execution:legacy-mapping-test`, `execution:test-data` | basic |

### 1.2 `legend-engine-xt-relationalStore-emit` — `relation-emit-models/`

Driven by `RelationEMITTests` over a separate resource root, so a failure is
attributable to one suite. 23 descriptors, 221 dynamic tests (`relation-primary-key`
was added by Phase B′ — §3.1b), covering
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

15 descriptors, 102 dynamic tests — 10 added by Phase D (§3.4),
`m2m-legacy-mapping-test` by Phase E (§3.5), and four M2M models added by feature
PRs since (§1.8) — split across two independently-runnable suites by subject:

- `grammar-emit-models/` → `GrammarEMITTests` — 7 language-construct models
  (parse + compile; no store, no mapping), 28 dynamic tests
- `m2m-emit-models/` → `M2MEMITTests` — 8 model-to-model mappings whose embedded test
  suites execute against the in-memory store, 74 dynamic tests

The table lists the Phase D and Phase E descriptors.

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
| `m2m-legacy-mapping-test` (Phase E) | `execution:legacy-mapping-test`, `execution:test-data`, `mapping:m2m-transform`, `mapping:mapping` | basic |

### 1.6 `legend-engine-xts-service/legend-engine-xt-service-emit` (service shapes)

4 descriptors, 31 dynamic tests, all added by Phase E (§3.5). One suite over the
conventional `emit-models/` root, driven by `ServiceEMITTests`, so no server-pom
`includedRelativeSubpaths` override is needed. `service-shared-domain` is the
reusable bundle (Person class + H2 store + relational mapping) the other three
depend on; it runs parse + compile only.

Every model here has a Service. Phase E's fourth fixture,
`relational-legacy-mapping-test`, exercises the *other* deprecated Phase 5 runner
but involves no Service, so it lives with the relational mappings (§1.1) — see
the placement rule in `emit-authoring.md` §3.1.

| Descriptor | Non-scaffolding features | Complexity |
|---|---|---|
| `service-shared-domain` | `grammar:derived-property` | basic |
| `service-multi-execution` | `execution:{multi-execution-service, plan-generation, service, service-test, test-data}` | basic |
| `service-shared-test-data` | `execution:{data-element, plan-generation, service, service-test, shared-test-data, test-data}` | basic |
| `service-legacy-test` | `execution:{legacy-service-test, plan-generation, service, test-data}` | basic |

### 1.7 Test-hosting modules that exist today

Five modules currently host distributed EMIT tests:

- `legend-engine-xt-relationalStore-emit` (two suites: `relational-emit-models/`
  via `RelationalEMITTests`, `relation-emit-models/` via `RelationEMITTests`)
- `legend-engine-xt-persistence-emit`
- `legend-engine-core-emit-tests` (core-feature: Pure language constructs + M2M)
- `legend-engine-xt-service-emit` (service shapes + the two legacy Phase 5 runners)
- `legend-engine-emit-tests` (cross-feature)

The authoring guide (`emit-authoring.md` §3.1) references several per-feature
modules that **do not exist yet** and must be stood up (§9 of that guide) before
their tests can land:

- ~~`legend-engine-core-emit/legend-engine-emit-m2m`~~ — stood up in Phase D as
  `legend-engine-core/legend-engine-core-emit-tests` (see §3.4); renamed because the
  batch is majority core-language fixtures rather than M2M, and relocated to a
  sibling of `legend-engine-core-emit` so a catalog module is not mistaken for one of
  the four framework modules inside it
- ~~`legend-engine-xts-service/legend-engine-xt-service-emit`~~ — stood up in Phase E
  (see §3.5), one suite over `emit-models/` driven by `ServiceEMITTests`
- `legend-engine-xts-generation/legend-engine-xt-generation-emit` — file/model generation
- `legend-engine-xts-flatdata/legend-engine-xt-flatdata-emit` — flat-data store
- external-format `-emit` modules (e.g. `legend-engine-xts-json/…-jsonSchema-emit`)

No module has been stood up since Phase E. Everything added since then landed in
the relational and M2M suites.

### 1.8 Added since Phase E — feature-driven descriptors

Eighteen descriptors arrived between 2026-08-21 and 2026-09-24. Each came with an
engine change, and each was written to pin that change. None was planned here.

| Descriptor | Suite | Origin | Non-scaffolding features |
|---|---|---|---|
| `relational-semistructured` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-filter,relational-semistructured,relational-semistructured-array-index,relational-semistructured-navigation}` |
| `relational-semistructured-array-operations` | relational | #5097 | `execution:{data-element,external-format-binding,test-data}`, `store:{relational-semistructured,relational-semistructured-array-functions,relational-semistructured-binding,relational-semistructured-flatten}` |
| `relational-semistructured-array-survey` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-semistructured,relational-semistructured-array-functions}` |
| `relational-semistructured-binding` | relational | #5097 | `execution:{data-element,external-format-binding,test-data}`, `store:{relational-semistructured,relational-semistructured-binding,relational-semistructured-navigation}` |
| `relational-semistructured-binding-source` | relational | #5097 | `execution:{data-element,external-format-binding,test-data}`, `mapping:store-union`, `store:{relational-inner-join,relational-multi-table,relational-semistructured,relational-semistructured-binding,relational-semistructured-navigation}` |
| `relational-semistructured-collection` | relational | #5097 | `execution:{data-element,external-format-binding,test-data}`, `store:{relational-semistructured,relational-semistructured-binding,relational-semistructured-flatten}` |
| `relational-semistructured-explode` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-inline-view,relational-inner-join,relational-multi-table,relational-semistructured,relational-semistructured-explode,relational-semistructured-navigation}` |
| `relational-semistructured-flatten-probe` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-semistructured,relational-semistructured-array-functions}` |
| `relational-semistructured-join` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-inner-join,relational-multi-table,relational-semistructured,relational-semistructured-navigation}` |
| `relational-semistructured-store-arrays` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-filter,relational-inline-view,relational-semistructured,relational-semistructured-array-functions}` |
| `relational-semistructured-wildcard` | relational | #5097 | `execution:{data-element,test-data}`, `store:{relational-semistructured,relational-semistructured-wildcard-path}` ⚠ off-taxonomy |
| `relational-semistructured-array-to-many` | relational | #5105 | `execution:{data-element,test-data}`, `store:{relational-semistructured,relational-semistructured-array-to-many-property}` ⚠ off-taxonomy |
| `relational-semistructured-model-chain` | relational | #5220 | `execution:{data-element,service,service-test,shared-test-data}`, `mapping:m2m-chained-relational`, `store:{relational-semistructured,relational-semistructured-binding,relational-semistructured-flatten}` |
| `relational-connection-time-zone` | relational | #5199 | `execution:{connection-time-zone,service,service-test}` |
| `m2m-target-instantiation` | M2M | #5185, #5267 | `execution:test-data`, `grammar:function`, `mapping:{m2m-complex-property-passthrough,m2m-target-instantiation,mapping}` ⚠ off-taxonomy |
| `m2m-target-instantiation-constrained` | M2M | #5185, #5267 | `execution:test-data`, `grammar:{constraint,function}`, `mapping:{m2m-complex-property-passthrough,m2m-target-instantiation,mapping,mapping-include}` ⚠ off-taxonomy |
| `m2m-internalized-dates` | M2M | #5202 | `execution:test-data`, `mapping:mapping` (its subject has no taxonomy entry, §3.0a) |
| `m2m-serialized-date-formats` | M2M | #5222 | `execution:{serialization-date-time-format,test-data}`, `mapping:mapping` |

Three observations shape the rest of this revision:

1. **This is the steady state `emit-authoring.md` §1 describes.** The feature owner writes
   the EMIT model in the same PR. It happened without prompting from this plan. It added
   12 taxonomy entries (§2.8), each registered in `emit.md` §6.2 in the PR that first
   used it, which is the rule in `emit.md` §6.2, *Evolving the Taxonomy*. Three more tags,
   from #5097, #5105, and #5267, were not registered (§3.0a).
2. **These are the most realistic models in the catalog.** Each was written to pin a
   specific engine behavior, and the richest were written against reported failures.
   `relational-semistructured-model-chain` reproduces a reported misbehavior of
   embedded arrays through an M2M-over-relational chain. Its one service query crosses
   a model chain, a binding, a flatten, and a `subType` cast, and it pinned defects
   that are recorded in `docs/engineering/architecture/relational-dynafunctions.md`
   §11.8.
   `m2m-target-instantiation-constrained` exists specifically to tell a real fix apart
   from an `instanceof`-guarded cast that would have let its sibling pass. The
   phase-delivered fixtures of §3.1–§3.5 were written to close a taxonomy cell, and most
   of them stop at one capability (§2.11).
3. **They also brought the metadata debt back.** Three tags are off-taxonomy, one
   subject is untagged, one exact feature-set duplicate has appeared, and several
   `complexity` values do not match the mechanical rule. See §3.0a.

Two harness facts surfaced by these models belong alongside §3.4a:

- `RelationalConnectionFactory` provisions **DuckDB when the query returns a `Relation`
  and H2 otherwise**, so the query's return type picks the database a fixture runs on.
  In `relational-semistructured-model-chain`, the TabularDataSet form of the service
  query reaches H2 and fails inside `legend_h2_extension_flatten_array`, which cannot
  read the document the model's `###Data` block loads. The same query passes under
  the PCT harness, whose CSV loading differs. The model records this as an EMIT harness
  gap, tracked separately, and ships only the `Relation` form. Until the gap is
  understood, the TDS form of that shape is not provable in EMIT.
- Graph fetch cannot read through a `Binding`. A binding-mapped property registers no
  class mapping, so plan generation fails. That is an engine limitation, not a harness
  gap. The acceptance tests for it are parked in
  `meta::relational::tests::semistructured::modelChain` with `<<paramTest.Ignore>>`.

---

## 2. Coverage Matrix Against the Taxonomy

Legend: **✅** covered by a distributed test · **✅⚠** tagged by a distributed test
whose data or query cannot tell a correct result from a plausible wrong one (§3.11) ·
**▲** covered only by a framework self-test fixture (no distributed/real-extension
example) · **❌** no coverage · **⛔** not a coverage target: no real implementation
(§2.9), no Legend-grammar form (§3.2, §3.3), blocked by an engine defect (§3.1 note
2), or not executable by EMIT (§3.4a, §3.5).

The matrix is computed from descriptor `features` (checked mechanically against the
`emit.md` §6.2 tables on 2026-09-24). It records **which** capabilities some fixture
claims. It does not record how much of a capability is exercised, what it is combined
with, or whether the test could fail. §2.11 covers that.

### 2.1 Scaffolding (baseline — not the feature under test) — 10 / 10

| Capability | Status | Where |
|---|---|---|
| `scaffolding:class` | ✅ | ubiquitous (97 descriptors) |
| `scaffolding:relational-store` | ✅ | relational, service, persistence, and cross-feature suites; in the relation suite, only `relation-shared-db` |
| `scaffolding:relational-connection` | ✅ | 8 service-bearing descriptors |
| `scaffolding:relational-mapping` | ✅ | relational suite |
| `scaffolding:relation-function` | ✅ | relation suite |
| `scaffolding:relation-mapping` | ✅ | relation suite |
| `scaffolding:m2m-mapping` | ✅ | M2M suite, `persistence-graphfetch-output`, `relational-semistructured-model-chain` |
| `scaffolding:runtime` | ✅ | 9 service-bearing descriptors |
| `scaffolding:model-connection` | ✅ | `persistence-graphfetch-output` only |
| `scaffolding:model-chain-connection` | ✅ | `relational-semistructured-model-chain` only (#5220) |

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
| `grammar:constraint` | ✅ Phase D (`grammar-constraint`, compile only); `m2m-target-instantiation-constrained` (#5185) is the only fixture that **evaluates** a constraint at run time |
| `grammar:measure` | ✅ Phase D (`grammar-measure`) |
| `grammar:profile` | ✅ Phase D (`grammar-profile`) |
| `grammar:qualified-property` | ✅ Phase D (`grammar-qualified-property`, `m2m-derived-source-property`) |

The grammar domain is closed. Phase D's seven grammar-only fixtures run parse +
compile with no store and no mapping, so a regression in a language construct is
attributable to the construct rather than to the mapping strategy that previously
happened to carry it.

The domain is closed at the tag level only. Outside `grammar-emit-models/`, grammar
constructs are mostly present rather than used. No model outside `grammar-profile`
uses a profile, apart from the built-in `temporal` stereotypes. No model uses the
`'''…'''` multi-line string (#4998) or the documentation sugar over `doc.doc` (#5008).
A derived property reaches SQL in exactly one fixture (`relational-service-with-join`
projects `fullName()`), and the derived properties on `service-shared-domain` and
`relation-shared-domain` are never navigated by a consuming query. See §2.11 and the
Phase D entry in §3.11.

> `grammar:function` was the second instance of the §2.10 pattern: genuinely
> exercised by all 19 executable relation models but claimed by none, so it read
> as a gap. §3.0 applied the tag, and it is now machine-checkable.

### 2.3 Mapping — 36 / 42 covered (4 out-of-scope — §3.1 note 2, §3.4a)

The relational half of this domain went from 1/27 to 17/27 in Phase A; the
relation-function half (13 entries, §6.2 of `emit.md`) is new — §3.0 added
`relation-filter`, `relation-group-by`, and `relation-xstore-association` to it
while normalizing the relation batch. Since Phase E, feature PRs added and covered
`mapping:m2m-chained-relational` and `mapping:m2m-target-instantiation`, which moved
the domain from 34 / 40 to 36 / 42.

**Relational + store-agnostic**

| Capability | Status |
|---|---|
| `mapping:aggregation-aware-mapping` | ✅⚠ Phase A — the aggregate table holds exactly the sums of the main table, so the grouped query cannot show which table answered it (§3.11) |
| `mapping:enumeration-mapping` | ✅ relational + relation; Phase D `m2m-enumeration-mapping` adds the M2M code path (string- and integer-keyed) |
| `mapping:mapping-include` | ✅ Phase A + relation (`relation-include`, mis-tagged — §2.10); `m2m-target-instantiation-constrained` adds the M2M form |
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
| `mapping:store-union` | ✅ Phase A; `relational-semistructured-binding-source` unions two tables with incompatible document layouts |
| `mapping:mapping` | ✅ Phase D (all M2M models, 8 now); previously ▲ framework-only |
| `mapping:m2m-derived-source-property` | ✅ Phase D (`m2m-derived-source-property`) |
| `mapping:m2m-transform` | ✅ Phase D (`m2m-transform`) |
| `mapping:m2m-chained-relational` | ✅ `relational-semistructured-model-chain` (#5220) — the only M2M-over-relational chain in the catalog |
| `mapping:m2m-target-instantiation` | ✅ `m2m-target-instantiation`, `m2m-target-instantiation-constrained` (#5185) |
| *(off-taxonomy)* `mapping:m2m-complex-property-passthrough` | used by both `m2m-target-instantiation` fixtures (#5267) but not in `emit.md` §6.2, so it is invisible to this matrix and to the dashboard — §3.0a |
| `mapping:relational-literal-list` | ⛔ blocked by an engine defect — note 2 under §3.1 |
| `mapping:m2m-local-property` | ⛔ not provable under single-connection M2M test data — §3.4a |
| `mapping:operation-mapping-merge` | ⛔ not provable under single-connection M2M test data — §3.4a; a service-runtime route that avoids that factory is proposed there for verification |
| `mapping:operation-mapping-merge-validation` | ⛔ as above |
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

### 2.4 Store — 16 / 20 covered (2 out-of-scope — §3.2)

#5097 registered and covered seven `store:relational-semistructured*` entries, which
moved the domain from 9 / 13 to 16 / 20. Two more semi-structured tags are in use but
unregistered (§3.0a).

| Capability | Status |
|---|---|
| `store:relational-filter` | ✅ |
| `store:relational-inner-join` | ✅ |
| `store:relational-multi-table` | ✅ |
| `store:relational-cross-schema` | ✅ Phase B (`relational-cross-schema`) |
| `store:relational-cross-table-filter` | ✅ Phase B (`relational-cross-table-filter`) |
| `store:relational-dyna-function` | ✅ Phase B (`relational-dyna-function`) |
| `store:relational-inline-view` | ✅ Phase B (`relational-inline-view`, `relational-dyna-function`); also `relational-semistructured-{explode,store-arrays}` |
| `store:relational-left-outer-join` | ✅⚠ Phase B (`relational-left-outer-join`) — proves the **default** outer semantics of navigating a `[0..1]` join in a projection; its mapping carries no join type and is identical to `relational-nested-join`'s. No fixture writes the `(INNER)` / `(OUTER)` join-type keyword (§3.11) |
| `store:relational-nested-join` | ✅ Phase B (`relational-nested-join`) |
| `store:relational-semistructured` | ✅ all 13 semi-structured models (#5097, #5105, #5220) |
| `store:relational-semistructured-array-functions` | ✅ `relational-semistructured-{array-operations,array-survey,flatten-probe,store-arrays}` |
| `store:relational-semistructured-array-index` | ✅ `relational-semistructured` |
| `store:relational-semistructured-binding` | ✅ `relational-semistructured-{array-operations,binding,binding-source,collection,model-chain}` |
| `store:relational-semistructured-explode` | ✅ `relational-semistructured-explode` |
| `store:relational-semistructured-flatten` | ✅ `relational-semistructured-{array-operations,collection,model-chain}` |
| `store:relational-semistructured-navigation` | ✅ `relational-semistructured`, `relational-semistructured-{binding,binding-source,explode,join}` |
| *(off-taxonomy)* `store:relational-semistructured-array-to-many-property` | `relational-semistructured-array-to-many` (#5105) — §3.0a |
| *(off-taxonomy)* `store:relational-semistructured-wildcard-path` | `relational-semistructured-wildcard` (#5097) — §3.0a |
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
| `milestoning:bi-temporal` | ✅⚠ Phase C (`milestoning-bitemporal`, table-backed `<<temporal.bitemporal>>` + `all(processingDate, businessDate)`). Both dimensions change on the same date and the query passes one date for both, so a plan that drops or swaps a dimension returns the same row (§3.11) |
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

### 2.6 Execution — 12 / 21 covered (2 out-of-scope — §2.9, §3.5)

Phase E added two taxonomy entries (`execution:legacy-{mapping,service}-test`) and
covered five capabilities, so the domain's total grew from 17 to 19. Since then
#5199 and #5222 each added and covered one more, for 21.

| Capability | Status |
|---|---|
| `execution:data-element` | ✅ (63 descriptors) |
| `execution:external-format-binding` | ✅ `service-with-binding`; `relational-semistructured-{array-operations,binding,binding-source,collection}`. Every `Binding` in the catalog is model-based (`modelIncludes`); none references a schema set |
| `execution:service` | ✅ (17 descriptors) |
| `execution:service-test` | ✅ 7 descriptors, all written in the block form (`testSuites: [ … { data: … tests: … } ]`); none passes test parameters, and none uses the flat form #4990 added (§3.12) |
| `execution:test-data` | ✅ |
| `execution:connection-time-zone` | ✅ `relational-connection-time-zone` (#5199) |
| `execution:serialization-date-time-format` | ✅ `m2m-serialized-date-formats` (#5222) |
| `execution:multi-execution-service` | ✅ Phase E (`service-multi-execution`, per-key routing proven by keyed test data) |
| `execution:shared-test-data` | ✅ Phase E (`service-shared-test-data`, one `Data` element feeding two services) |
| `execution:legacy-mapping-test` | ✅ Phase E — `relational-legacy-mapping-test` (relational suite) and `m2m-legacy-mapping-test` (M2M suite); the runner's two input-data paths are materially different, so both are covered. §6.2 taxonomy addition |
| `execution:legacy-service-test` | ✅ Phase E (`service-legacy-test`; §6.2 taxonomy addition) |
| `execution:plan-generation` | ✅ Phase E — the tag existed but was applied nowhere; the three service-bearing Phase E descriptors now carry it. It is still on only 3 of the 17 descriptors tagged `execution:service`: the pre-Phase E service models and both service-bearing models added since lack it. The retro-application pass is in §3.0a |
| `execution:file-generation` | ❌ (real generators exist — Avro/Protobuf/JSON Schema/…; only the fake-SPI framework fixture exercises the path) |
| `execution:model-generation` | ⛔ (no real `ModelGenerationExtension` SPI in legend-engine — §2.9) |
| `execution:post-validation` | ⛔ not executable by EMIT — §3.5 |
| `execution:binding` | ❌ — but `emit.md` §6.2 describes it in the same words as `execution:external-format-binding` ("External format binding"), so whether this is a gap at all is a taxonomy decision (§4.2) |
| `execution:external-format` | ❌ |
| `execution:schema-set` | ❌ — no `SchemaSet` element appears anywhere in the catalog |
| `execution:hosted-service` | ❌ |
| `execution:snowflake-app` | ❌ |
| `execution:bigquery-function` | ❌ |

### 2.7 Persistence — 12 / 12 covered ✅

The entire persistence domain is covered at the per-capability level. Remaining
opportunities here are combination-level only (see §3.4) and are low priority.

### 2.8 Coverage headline

| Domain | Covered / Total (2026-09-24) | At Phase E (2026-07-31) | Pre-Phase A |
|---|---|---|---|
| Scaffolding | 10 / 10 | 9 / 9 | 7 / 7 |
| Grammar | 10 / 10 | 10 / 10 | 3 / 10 |
| Mapping | 36 / 42 (4 out-of-scope — §3.1 note 2, §3.4a) | 34 / 40 | 1 / 27 |
| Store | 16 / 20 (2 out-of-scope — §3.2) | 9 / 13 | 3 / 13 |
| Milestoning | 6 / 7 (1 out-of-scope — §3.3) | 6 / 7 | 0 / 7 |
| Execution | 12 / 21 (2 out-of-scope — §2.9, §3.5) | 10 / 19 | 5 / 17 |
| Persistence | 12 / 12 | 12 / 12 | 12 / 12 |
| **Total** | **102 / 122** | **90 / 110** | **31 / 93** |

Totals grew because the relation-function work added 12 real capabilities to the
taxonomy (§6.2 of `emit.md`) as well as covering them, §3.0 added 3 more
(`mapping:relation-filter`, `mapping:relation-group-by`,
`mapping:relation-xstore-association`) while normalizing the relation batch, and
Phase E added 2 (`execution:legacy-mapping-test`,
`execution:legacy-service-test`) — all five immediately covered. Since Phase E, the
feature PRs in §1.8 added 12 more, all covered by the model that introduced them:
`scaffolding:model-chain-connection`, `mapping:m2m-chained-relational`,
`mapping:m2m-target-instantiation`, seven `store:relational-semistructured*` entries,
`execution:connection-time-zone`, and `execution:serialization-date-time-format`.

**None of the growth since Phase E closed a gap this plan was tracking.** The 20
uncovered entries are the same 20 as at Phase E: 9 are ⛔, and **11 are real gaps**.
All of the new coverage is new taxonomy entries that arrived already covered. That
does not mean the feature PRs did less than the phases. It means taxonomy growth now
follows engine growth, and the matrix is not measuring what the feature PRs added:
depth and interaction (§2.11).

Status by domain:

- **Execution — 12 / 21.** The 7 remaining real gaps are file generation (§3.6),
  the three external-format capabilities (§3.7, one of them possibly a taxonomy
  duplicate — §4.2), and the three function activators (§3.9); each is gated on
  standing up that feature's `-emit` module, not on any missing engine capability.
  The domain also has pipeline paths that no taxonomy entry names and no fixture
  reaches: function test suites, the #4990 service-test resolvers, and parameterized
  service tests (§3.12).
- **Store — 16 / 20.** All six authorable join/store-shape features landed in
  Phase B (§3.2), and #5097 added the semi-structured family. The two remaining real
  gaps (`service-store`, `flat-data-store`) need new modules (§3.8), and
  right-/full-outer join are out of scope (no classic-store grammar). The
  left-outer-join cell is weaker than it looks (§3.11).
- **Mapping — 36 / 42.** Of the six uncovered, four are out of scope:
  `mapping:relational-literal-list` (engine defect, re-checked 2026-09-24:
  `RelationalParseTreeWalker.visitFunctionOperationArgument` still double-wraps each
  element `Literal`) and the three §3.4a capabilities. The two real gaps left are
  `mapping:cross-store` (§3.10) and `mapping:relation-binding-transformer` (§3.7).
  §3.4a now proposes a route for the merge pair that does not wait for cross-store.

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

**The pattern is recurring.** None of the four divergences above has come back in the
relation batch, but the same kinds have appeared in the feature-driven additions of
§1.8: off-taxonomy values ((a)), an exact feature-set duplicate ((b)), and an
untagged subject ((c)). The report's own copy of the taxonomy has also drifted. §3.0a
is the second pass. The model-profile proposal (§2.11) is the structural fix.

### 2.11 What the matrix cannot see: depth, interaction, and assertion strength

A cell turns ✅ when one descriptor claims the capability, so at 102 / 122 the matrix
has little left to say. It cannot say whether the catalog looks like the models Studio
users actually write. The measurements below cover the 93 descriptors that are not
shared bundles, counting each model's primary sources plus its dependencies. They are
regex-level counts over the `.pure` text taken on 2026-09-24, so individual numbers
are approximate, but the overall picture is not in doubt.

| Measure | Catalog |
|---|---|
| Size | median 121 non-blank lines, 2 classes, 1 table, 2 class mappings, 1 test suite, 1 assertion |
| Small models | 63 of 93 have three or fewer classes; 68 have two or fewer tables |
| Assertions | 48 of 93 have exactly one; 77 have at most two. Every assertion is `EqualToJson` (178 uses) — no `EqualTo`, no `EqualToRelation` |
| Query shape | TDS `->project` dominates. Graph fetch appears in 9 models, **all over M2M mappings — none over a relational mapping**. No query uses `->take`/`->limit`/`->slice`, `->distinct()`, a date function, `%latest`, or `graphFetchChecked` |
| Services | 8 models declare a Service in primary scope. One service query in the catalog takes a parameter (`persistence-graphfetch-output`, whose `testSuites` is empty); **no service test passes a parameter** |
| Test styles | No function test suite (`FunctionTestRunner`). No flat-form service test or data resolver (#4990). Every mapping and service suite uses the block form |
| Store shape | One compound join condition (the explode join in `relational-semistructured-explode`), no composite-key join, one self-join, no explicit `(INNER)` / `(OUTER)` join type. Every model with three or more joins inherits them from `relation-shared-db` |
| Grammar in use | Constraints in 2 models; a user-defined profile in 1 (`grammar-profile`); `doc.doc` in 0; `'''…'''` strings in 0; a derived property translated to SQL in 1 |

Four conclusions follow.

1. **Present is not exercised.** The relation suite's shared domain gives 20 models up
   to 19 classes with inheritance, milestoning, enumerations, and associations, but
   each of those models runs one to three projections over a small part of it. The
   `service-shared-domain` derived property is defined in every service fixture's
   dependency and navigated by none. The richness is in the dependency, not in the
   test.
2. **Isolation was the design, and it stays. Interaction is what is missing.** The
   phase-delivered fixtures isolate one capability so that a failure is attributable,
   and that remains valuable. But `emit-authoring.md` §1 names the other half of
   EMIT's purpose: interactions that "unit tests in each module won't catch." A
   typical Studio service navigates a milestoned association through an inherited or
   embedded mapping, takes parameters, and returns a graph fetch. The catalog has some
   of these pieces: milestoned navigation over `ModelJoin` in the relation suite, and
   inheritance and embedding in Phase A. No Service combines them, and no fixture has
   service parameters or a relational graph fetch at all.
3. **Some fixtures cannot fail.** Three tagged fixtures run data or queries that give
   the same answer whether the claimed behavior works or not (§3.11). A false green
   from weak data does the same harm that `emit-authoring.md` §11.3 forbids for
   compile-only fixtures: it stops anyone from looking again.
4. **The defects are at the junctions.** Each defect EMIT work has surfaced or pinned
   since this plan began sat where two capabilities meet, each already covered on its
   own: a multi-execution service × the modern test-result type (Phase E), embedded
   arrays × a model chain × a `subType` cast (#5220), a complex-property pass-through ×
   a colliding class mapping (#5267), and a `new`-constructed target × a constraint ×
   graph fetch (#5185).

The matrix cannot measure any of this, and hand-authored tags should not be stretched
to try. The designed answer is the model-profile proposal
(`docs/emit/emit-model-profile-proposal.md`, in review on the `emit/model_descriptor`
branch). It proposes profiles derived from the compiled model, with sites, tokens,
t-way interaction tuples, and evidence levels, computed by the same extractor that
will scan Studio projects. Until it lands, §3.11–§3.13 work from a hand checklist
(§3.13), and each fixture they add should later be verified against its generated
profile.

---

## 3. Proposed New Tests

Each row below is a proposed descriptor: a name, the capability gap(s) it closes,
its non-scaffolding feature set, and its target module. Feature sets were checked
against §1's inventory for exact-match duplicates per `emit-authoring.md` §11.2 —
none duplicate an existing set. Subset/superset relationships are intentional
(they provide distinct regression coverage). The two exact-match clusters that §2.10(b)
found in the relation batch were fixed in §3.0; one new pair has appeared since
(§3.0a). §3.0–§3.10 are the taxonomy-driven batches; §3.11–§3.13 are the realism
batches that follow from §2.11. `.pure` authoring follows
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

### 3.0a Metadata debt since Phase E → Phase A‴ (no new tests)

The same kind of pass as §3.0, over the §1.8 additions and the items Phase E deferred.
Every item is metadata or report code; no `.pure` changes. Nothing here changes a
dynamic-test count.

| Item | Work |
|---|---|
| Register or fold three off-taxonomy tags | `mapping:m2m-complex-property-passthrough` (#5267) names the code path that the #5257 fix changed, so register it. Before registering `store:relational-semistructured-array-to-many-property` (#5105) and `store:relational-semistructured-wildcard-path` (#5097), read their sources against `-flatten` and `-navigation`. Register each only if it names a code path the existing entry does not; otherwise re-tag it to that entry |
| Tag the untagged subject | `m2m-internalized-dates` (#5202) carries only `execution:test-data` and `mapping:mapping`. Its subject, dates surviving the internalizing of external-format test data into an M2M source, has no entry, so the matrix cannot tell it apart from `m2m-transform`. Add an entry in the same PR that tags it |
| Break the new duplicate | `relational-semistructured-array-survey` ≡ `relational-semistructured-flatten-probe` (identical sorted feature sets). They are different tests — one suite per `array_*` function versus `array_flatten` over each array shape — so the collision is a missing tag, as in §2.10(b), not redundancy |
| Re-derive complexity | 8 descriptors disagree with the mechanical rule of `emit-authoring.md` §11.1. Four M2M models say `basic` for three domains (`m2m-derived-source-property`, `m2m-enumeration-mapping`, `m2m-target-instantiation`, `-constrained`). Three say `intermediate` for two (`relational-cross-table-filter`, `relational-semistructured-array-to-many`, `relational-semistructured-wildcard`). `relational-semistructured-model-chain` says `advanced` for three. Several of these are authors signaling that a model is harder than its domain count says, which is the §2.11 point. Apply the rule, and let the description carry the rest until the profile proposal makes complexity derived |
| Retro-apply `execution:plan-generation` | Deferred from Phase E (§3.5). 14 of the 17 descriptors tagged `execution:service` lack it: `relational-service`, `relational-service-with-join`, `service-with-binding`, `relational-connection-time-zone`, `relational-semistructured-model-chain`, and the nine persistence descriptors. Phase 6 plans only primary-scope Services, and the persistence descriptors reach theirs through the `persistence-shared` dependency root, so check each one |
| Sync the report's taxonomy | `EMIT_to_HTML.FULL_TAXONOMY` lists 90 of the 122 documented entries. It is missing every `persistence:*` entry (12), every `mapping:relation-*` entry (13), `scaffolding:relation-{function,mapping}`, `mapping:m2m-target-instantiation`, `execution:legacy-{mapping,service}-test`, `execution:connection-time-zone`, and `execution:serialization-date-time-format`, so the dashboard's coverage-gaps tab cannot show a gap in any of them. Sync the list, and add the check `emit.md` §9 calls "catalog completeness": every descriptor feature must be in the list, and the list must match §6.2. With that check, the next unregistered tag fails the build. The proposal's machine-readable taxonomy (its §10.4) later replaces both copies |

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
> honored when the query projects first and sorts the TDS afterwards
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
> honored). Same category as right-/full-outer join (§3.2) and
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

This behavior is **known and deliberately not being changed** — other code depends on
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
nothing about the feature's behavior — a false green is worse than an honest ❌. The
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

**Revisit (2026-09-24): a route that does not go through the test-data factory.** The
ruling above stands. `ModelStoreTestConnectionFactory` is not to be changed, and it
still returns inside its loop on the first entry (re-checked on this date). But the
constraint is a property of *test data*, and there is a path to execution that uses
none:

- The engine's own merge tests supply each source class through its own
  `JsonModelConnection` in one runtime. `core/store/m2m/tests/legend/merge.pure` does
  this with three connections, and the service-execution resource
  `multiParamM2MServiceMerge.pure` does it in Legend grammar: a `ModelStore` runtime
  with one `JsonModelConnection` per source class, and a merge `Operation` over two
  Pure set implementations.
- The Testable service runner uses the runtime as declared when a suite supplies no
  connection data. In `ServiceTestRunner`'s single-execution `initialize`, the runtime
  is replaced only when the suite has `connectionsTestData` (the `legacyConnections`
  branch) or `serviceTestData` (the #4990 `newResolvers` branch). With neither, the
  plan is built against the Service's own runtime.

So a Service whose runtime carries one `JsonModelConnection` per source class, each
with an inline `data:application/json,…` URL, and whose test suite has asserts but no
data, should execute a merge end to end without touching the factory. The same shape
would give `mapping:m2m-local-property` its crossing query, because each side of the
XStore association reads from its own connection. **This is a lead, not a result.**
It has been read from the code, not run. The first step is a probe fixture that
asserts on the merged output, to confirm three things: the suite runs with no data
block, the plan reads every source connection, and the assertion fails when one
source's data is removed. If the probe passes, the three ⛔ entries become authorable
in the service suite (Phase L, §3.12). If it fails, record why here and leave them for
Phase J.

### 3.5 Service shapes → new `legend-engine-xt-service-emit` — **DONE (4 of 5; 1 not executable)**

**Applied.** `legend-engine-xts-service/legend-engine-xt-service-emit` was stood
up with one suite (`emit-models/` → `ServiceEMITTests`), a
`service-shared-domain` bundle, and 3 service fixtures (31 dynamic tests); the
fourth fixture landed in the **relational** suite (245 dynamic tests, up from
239). All passing, checkstyle clean. Every model is backed by H2 rather than a
model store, so each fixture executes rather than only compiles.

| Proposed test | Closes | Feature set (non-scaffolding) |
|---|---|---|
| `service-multi-execution` | `execution:multi-execution-service` | `execution:{multi-execution-service,plan-generation,service,service-test,test-data}` |
| ~~`service-post-validation`~~ | — ⛔ not executable by EMIT (see below) | — |
| `service-shared-test-data` | `execution:shared-test-data` | `execution:{data-element,plan-generation,service,service-test,shared-test-data,test-data}` |
| `service-legacy-test` | legacy `ServiceTest` path (Phase 5) | `execution:{legacy-service-test,plan-generation,service,test-data}` |
| `relational-legacy-mapping-test` → `relational-emit-models/` | legacy `MappingTests` path (Phase 5), relational input data | `execution:{legacy-mapping-test,test-data}` |
| `m2m-legacy-mapping-test` → `m2m-emit-models/` | legacy `MappingTests` path (Phase 5), Object/JSON input data | `execution:{legacy-mapping-test,test-data}`, `mapping:{m2m-transform,mapping}` |

> **The legacy mapping fixtures belong to the suites that own their mappings, not
> to this module.** The relational one was first written here as
> `mapping-legacy-test`, on the reasoning that the two deprecated Phase 5 runners are
> siblings and should sit together. That was wrong: it contains no Service, and the
> runner being deprecated is a property of the *test style*, not a feature area —
> grouping by it scatters mappings away from the suites that own them. It now lives
> beside the other relational mappings as `relational-legacy-mapping-test`, reusing
> `relational-shared-domain`. The general rule, added to `emit-authoring.md` §3.1: a
> legacy `MappingTests` block goes wherever its mapping kind goes — relational to
> `relational-emit-models/`, M2M to `m2m-emit-models/`, multi-area to the
> cross-feature module.

> **Both of the runner's input-data paths are now covered.** `MappingTestRunner`
> handles `<Object, JSON, …>` and the store-specific forms through completely
> different code:
> `buildTestConnection` turns `ObjectInputData` into a `JsonModelConnection` whose
> url is a base64 `data:` URL, while relational input data goes out to
> `ConnectionFactoryExtension` and ends up as H2 setup SQL generated by
> `meta::relational::functions::database::setUpData`. Covering one proves nothing
> about the other, so `m2m-legacy-mapping-test` was added alongside — it is the
> M2M suite's only `MappingTests` block, and before it the whole
> `ObjectInputData` branch was unexercised anywhere in the catalog. Note that its
> single-source-class limit is a distinct one from the §3.4a constraint: it comes
> from an explicit `inputData.size() != 1` check in `buildTestConnection`, not from
> `ModelStoreTestConnectionFactory`'s thread-local stream.

> **Two taxonomy entries were added** (`emit.md` §6.2, Execution):
> `execution:legacy-mapping-test` and `execution:legacy-service-test`. §4.2 had
> flagged a `legacy` marker as optional; it turned out to be necessary. The legacy
> runners are a **pipeline** gap — EMIT Phase 5 drives three runners (Testable,
> legacy mapping, legacy service — `emit.md` §4.6) and only the Testable path had a
> distributed example — and overloading `execution:service-test` for them would have
> made the modern and deprecated paths indistinguishable in the matrix.

> **`execution:plan-generation` is now applied**, on the three service-bearing
> Phase E descriptors. §4.2 had noted the tag existed but was used nowhere.
> Retro-applying it to the pre-existing service models (`relational-service`,
> `relational-service-with-join`, `service-with-binding`, and whichever persistence
> descriptors define a Service in *primary* scope) was deliberately left out of this
> phase: Phase 6 only runs for primary-scope Services, and several persistence
> descriptors reach their Service through the `persistence-shared` dependency, so
> the sweep needs per-descriptor checking rather than a blanket edit.

> **`service-multi-execution` needed a framework fix.** A test on a multi-execution
> service yields a `MultiExecutionServiceTestResult` — a bundle of one result per
> execution key, and neither a `TestExecuted` nor a `TestError`. `EMITTasks.assertTestPassed`
> fell through to "Unexpected test result type" and `EMITRunner`'s tally counted it as a
> failure, so no multi-execution service could ever have passed EMIT. Both now unwrap
> the bundle and assert every key (`EMITTasks.isTestFailure` is the shared predicate),
> naming the failing key in the message. A bundle with **no** per-key results is treated
> as a failure too — without that, an atomic test whose `keys` match none of the
> service's execution keys would pass while executing nothing. Verified by pointing the
> fixture's `keys` at a non-existent env and confirming the guard fires.

> **`service-post-validation` was not authored.** EMIT cannot execute post-validations:
> the assertions are evaluated only by `ServicePostValidationRunner`, a REST-driven
> runner with no hook in the Testable path or in Phase 5. A fixture would have proved
> that the block parses, that the compiler's checks fire (duplicate assertion ids,
> assertion parameter type vs. service execution return type), and that a plan still
> generates — but never that an assertion evaluates. Under `emit-authoring.md` §11.3
> that is a compile-only claim on a run-time capability, so the capability stays ⛔ in
> §2.6 rather than being tagged. It becomes authorable if Phase 5 ever gains a
> post-validation hook.

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
> **Update (2026-09-24).** Five semi-structured models (§1.8) now use a `Binding` too,
> four of them tagging `execution:external-format-binding`, so model-based bindings are
> well covered: `modelIncludes` only, JSON content type, typing a document during
> execution. What no fixture has is a **schema-backed**
> binding (`schemaSet` + `schemaId`) or a `SchemaSet` element at all. That is the real
> content of this batch. `emit.md` §6.2 describes `execution:binding` and
> `execution:external-format-binding` with the same words, so settle the taxonomy
> question in §4.2 before authoring `external-format-binding`. Otherwise the batch
> may close a cell that is a duplicate of one already covered.
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
>
> Two additions, from §2.11. First, under `emit-authoring.md` §11.3 an activator
> fixture must reach at least Phase 4b artifact generation. One that only parses and
> compiles is a compile-only claim, and the capability should stay ❌. Second, the
> function an activator wraps can carry its own function test suite. Giving it one
> makes each activator fixture also exercise the function-test runner (§3.12), which
> is how a Studio user tests the query behind an activator.

### 3.10 High-value cross-feature combinations → `legend-engine-emit-tests`

Reserve the cross-feature module for combinations no single per-feature module's
classpath can host (`emit-authoring.md` §3.2). Add sparingly, only after the
per-feature gaps above are closed.

| Proposed test | Purpose | Feature set (non-scaffolding, abbreviated) |
|---|---|---|
| `cross-store-m2m-relational` | `mapping:cross-store` | `mapping:cross-store`, `execution:{service,service-test}` |
| ~~`service-relational-multi-execution`~~ | — already delivered: Phase E's `service-multi-execution` runs over the relational `service-shared-domain` mapping on H2 | — |
| `service-relational-with-generation` | service + relational + file generation (the `emit.md` §6.1 exemplar) | `execution:{service,service-test,file-generation}`, `grammar:association` |

> §3.13's composite models are also cross-feature, but most of them fit a per-feature
> module's classpath (relational + service + M2M), so they go there, not here, under
> the placement rule. This module keeps only the combinations that need the
> collection classpath. `cross-store-m2m-relational` is one of them, and
> `relational-semistructured-model-chain` now shows that an M2M-over-relational model
> runs in the relational module. So check whether `mapping:cross-store` needs this
> module at all before authoring it here.

### 3.11 Revisiting the completed phases → Phase K (fixture repairs)

Every completed step was re-read against §2.11. Each gets one of three verdicts:

- **holds**: the fixtures prove what they claim; leave them alone.
- **repair**: a fixture's data or query cannot fail when the claimed behavior is
  wrong. Fix it in Phase K, because a false green outranks any new coverage.
- **extend**: the fixtures are right, but the capability is only ever exercised on
  its own. Carry it into a composite in Phase M (§3.13).

No completed fixture is to be deleted or folded into a composite. The isolation
fixtures are what make a composite's failure attributable.

| Completed step | Verdict | Finding (re-checked 2026-09-24) | Action |
|---|---|---|---|
| §3.0 metadata normalization (A″) | holds | Its fixes are intact, but the same kinds of debt have re-accumulated in the §1.8 additions | §3.0a (A‴) |
| §3.1 relational mapping (A) | **repair 1**, extend | `relational-aggregation-aware` cannot tell which table answered the grouped query (K1). The rest discriminate for their one capability: `relational-left-outer-join` has an unmatched row, `relational-distinct` has duplicates, and both union fixtures draw rows from each leg. Every fixture is 1–3 classes, one projection, and one or two assertions. `relational-otherwise-embedded`'s first suite reads a value that is identical in both tables, so only its second suite distinguishes the embedded branch from the otherwise join. That is acceptable, because the second suite exists | K1; M |
| Relation batch (A′) | holds, extend | The richest domain in the catalog, through `relation-shared-*`, but each model runs one to three projections over a small part of it (§2.11, point 1) | M (relation leg) |
| §3.2 relational store (B) | **repair 1** | `relational-left-outer-join` proves the default outer semantics of projecting a `[0..1]` navigation. Its mapping line `department: [demo::store::JoinsDB]@Emp_Dept` is identical to `relational-nested-join`'s. The grammar accepts an explicit join type on the first join and between chained joins (`RelationalParserGrammar.g4`: `joinSequence`, `joinPointerFull`; `RelationalParseTreeWalker.JOIN_TYPES`), and no fixture writes one (K2) | K2 |
| §3.1b relation primary key (B′) | holds | Single and composite `~primaryKey` both exercised | — |
| §3.3 milestoning (C) | **repair 1**, extend | `milestoning-bitemporal`: one class, one property, two rows. Both dimensions change on 2021-01-01, and the query passes `%2020-06-01` for both dates, so dropping either dimension or swapping the two returns the same row (K3). Beyond that, table-backed milestoning is never navigated across a join (date propagation), never queried with `%latest`, never graph-fetched, and never inside a Service | K3; M |
| §3.4 grammar (D) | holds, extend | Parse + compile proves what these fixtures claim. But constructs are rarely *used* in executed queries elsewhere (§2.2, §2.11), and the two grammar additions since Phase D, `'''…'''` strings (#4998) and documentation sugar over `doc.doc` (#5008), have no fixture | Add one grammar-suite fixture for #4998 / #5008 (below); M uses constructs in queries |
| §3.4 M2M (D) | holds | The suite has grown realistically through feature PRs (§1.8). Apart from `persistence-graphfetch-output`, also over M2M, it is the only place graph fetch runs | — |
| §3.4a single-connection M2M | revisit the route, not the ruling | A service-runtime route may make merge and local-property executable without changing the factory | Probe in L (§3.12) |
| §3.5 service shapes (E) | holds, extend | Every service reads one class from one table. No parameters, block-form suites only, no graph fetch | L (§3.12) |

**K1 — `relational-aggregation-aware`.** The aggregate table
`SalesByPersonTable` holds `John,30` and `Jane,5`, exactly the sums of `SalesTable`
(10 + 20 and 5). So `groupedQueryUsesAggregateTable` would also pass if the router
ignored the aggregate view and grouped the main table. Make the two tables disagree
(for example, `John,31`), so that the grouped assertion passes only if the aggregate
table answered it and the row-level assertion passes only if the main table did. Say
in a comment that the inconsistency is deliberate. Consider a third suite that groups
by a column the view does not offer, which must fall back to the main table and so
pins the other half of the routing decision.

**K2 — explicit join types.** Re-describe `relational-left-outer-join` as what it is:
the default outer semantics of navigating a `[0..1]` association in a projection.
Then add `relational-join-type`, which writes `(INNER)` on the first join of a
property mapping and `> (OUTER)` / `> (INNER)` between the joins of a chain, over data
in which each choice changes the rows. What an explicit `(INNER)` does to a projected
`[0..1]` navigation is exactly what the fixture has to establish (working rule 1:
verify before authoring). If it turns out to make no difference, that is a finding to
record, not a fixture to force. The keyword is written in the mapping, so if it proves
to be a distinct code path, register a `mapping:` entry for it in the same PR.

**K3 — `milestoning-bitemporal`.** Re-author the data so the business and processing
boundaries fall on different dates. Use three or more versions, and query with two
different dates, at least one pair of which selects a different row if the dates are
swapped. Keep the single-class shape: the fixture's job is to isolate bitemporal
selection. The interaction work (propagation through joins, `%latest`, graph fetch,
services) belongs to Phase M.

**Grammar addendum (with K).** Add `grammar-documentation`: `'''…'''` multi-line
string literals (#4998) and the documentation sugar over `doc.doc` (#5008), on
classes, properties, and a function. These are Studio-visible grammar additions with
no fixture. Parse + compile is the right scope for them, and the round trip is part
of what they must prove.

### 3.12 Pipeline paths no fixture reaches → Phase L

These are not feature combinations. Each is a whole runner branch or execution path
that the catalog never enters. That is the class of gap that hid the Phase E defect,
where no multi-execution service could ever have passed. None needs a new module, but
the function tests need a pom change first.

> **Precondition for function tests: a missing runner is a silent skip.** None of the
> five hosting modules has `legend-engine-test-runner-function` on its test classpath
> (checked with `dependency:tree`, 2026-09-24). EMIT selects Testables through
> `TestableRunnerExtensionLoader.isTestable`, which only recognizes an element whose
> runner extension is loaded. So on today's classpaths, a function's test suite would
> not be run, and would not be reported as failing either. The fixture would go green
> having tested nothing. Add the dependency, test-scoped, to each host module. Then
> confirm that the TEST_EXECUTION phase reports the function tests by count, and that
> breaking an assertion turns the fixture red (§4.1 step 6). The same trap applies to
> any Testable kind whose runner lives in its own module, so check for it in Phases
> F–I as well.

| Proposed test | Path it reaches | Placement | Taxonomy |
|---|---|---|---|
| `relational-function-test` | `FunctionTestRunner`: a function with a test suite, over a relational mapping, with a shared `Data` element and parameters. Function test suites are Studio's current way to test a query directly. Function is one of the three core Testable kinds, alongside Mapping and Service, and the only one no fixture reaches | `relational-emit-models/` | new `execution:function-test` |
| `m2m-function-test` | The same runner, with M2M (`ModelStore`) test data. As with the legacy mapping runner (§3.5), the store-specific data paths differ, so one fixture does not prove the other | `m2m-emit-models/` | `execution:function-test` |
| `service-test-resolvers` | The #4990 flat form: suite-level data statements (`baseDataResolver` / `referenceDataResolver`) and atomic tests `id (params) => expected;`. These drive `ServiceTestDataResolverSetup`, the `newResolvers` branch of `ServiceTestRunner`, which builds the test runtime differently from `connectionsTestData`. Include a `[keys]` multi-execution test, since the branch has its own multi-execution handling | service suite | new entry, name to settle (§4.2) |
| `service-parameterized` | A Service whose query takes typed parameters, including a `[0..1]`, an enumeration, a date, and a `[*]`, with block-form tests that pass `parameters:`. No test in the catalog passes a parameter today | service suite | `execution:{service,service-test}` plus a distinguishing entry (§4.2), or it risks an exact feature-set duplicate |
| `relational-graph-fetch` | `graphFetch(...)->serialize(...)` over a **relational** mapping, through a to-many association, in a mapping test suite and in a Service. Graph-fetch services over relational mappings are a staple of Studio projects. Every graph fetch in the catalog today is M2M, so the relational graph-fetch execution nodes and their parent/child key handling have no fixture | `relational-emit-models/` | candidate `execution:graph-fetch`, retro-tagged onto the 9 M2M graph-fetch models in the same PR |
| `service-m2m-merge` (probe) | The §3.4a lead: a Service over a merge `Operation`, with one `JsonModelConnection` per source class in its runtime and no suite data | service suite | `mapping:operation-mapping-merge`, only once the probe proves execution |

If the probe succeeds, follow it with `service-m2m-merge-validation` and a
local-property fixture over an XStore association in the same shape, and move all
three §3.4a entries from ⛔ to ✅.

### 3.13 Studio-shaped composite models → Phase M

The purpose is interaction coverage: models written the way a Studio project is
written, around a small but realistic domain, in which **one executed query or test
crosses several capabilities at once**. Co-occurring in the same model is not enough.

**Start with a shared realistic domain.** Build one bundle in the relational suite
(working name `relational-shared-trading`), as `relational-shared-joins` was built for
Phase B. It should have roughly 10–15 classes and cover:

- an inheritance hierarchy, enumerations, associations including to-many and a
  self-association, derived and qualified properties that queries will use, and
  constraints with more than one enforcement level;
- documentation and profile usage on most elements;
- a two-schema database with a composite key, a multi-column join, a self-join, a
  business-temporal table, a view, and a filter;
- `Data` elements with nulls, unmatched rows, and version boundaries.

The composites are then thin mapping, service, and test layers over the bundle.

**Candidate composites.** Each row names the interactions its main query must cross.
Finalize the list when the bundle exists. Some combinations may turn up engine
defects; record those as findings, and do not force the fixture.

| Candidate | Interactions at one site | Suite |
|---|---|---|
| Parameterized graph-fetch service | service parameters (one optional) × relational graph fetch × subtype navigation (inheritance) × milestoned association with the date propagated from a parameter × enumeration mapping | relational |
| Aggregating projection | derived property translated to SQL inside `groupBy` × store union with an embedded property on each leg × filter on a qualified property × `sort` + `take` | relational |
| Constraint-checked graph fetch | `graphFetchChecked` × class constraints at two enforcement levels × a to-many navigation, with data that violates one constraint, so the defects are asserted | relational |
| M2M over relational | model chain × M2M enumeration mapping × derived source property × `new` operator × graph fetch. This is the non-semi-structured counterpart of `relational-semistructured-model-chain` | relational |
| Relation-function service | `~func` source with a window function and a filter × `ModelJoin` to a milestoned class × union × parameterized function test (§3.12) | relation |

**The checklist.** Every composite, and every repaired fixture, meets these until the
model-profile proposal can check them mechanically:

1. **Executed, not present.** A capability counts toward a composite only if an
   executed test's query reaches it. This is `emit-authoring.md` §11.3 applied per
   query rather than per model.
2. **Data that can fail.** For each capability the query crosses, the data holds a row
   that a plausible wrong implementation would treat differently: an unmatched join
   row, a null in a `[0..1]`, a version outside the as-of date, a row from each union
   leg, or an aggregate that disagrees with its detail. K1 and K3 are what happens
   without this rule.
3. **Tested the way Studio users test.** Several atomic tests per suite, parameters
   where the query has them, shared `Data` elements, and both test forms (block and
   flat) across the phase.
4. **Sized to the interaction.** 5–15 classes over the shared bundle, not a dump of a
   real project. A composite that needs 40 classes is two composites.
5. **Named for the scenario, described by the interaction.** The `description`
   states which capabilities meet at which query, in the style of
   `relational-semistructured-model-chain`.

**Where this is heading.** Hand-designed composites are a stopgap. Once the downstream
Studio harvest and the model-profile proposal's scan loop (its §9) exist, derive
composites from the uncovered tuples that real projects report, not from this table.

---

## 4. Prioritization & Sequencing

Originally ordered by value per unit of effort: Phases A–C needed **no new modules**
and closed the largest gaps, and later phases were gated on standing up modules. A–E
are done. The rows below A‴ are the remaining work, and the recommended order follows
the table.

| Phase | Batch | New module? | Gaps closed | Effort |
|---|---|---|---|---|
| **A** | §3.1 Relational mapping features (17 tests — **done**) | No | 19 capabilities | Low — existing classpath, reuse shared domain |
| **A′** | Relation-function mappings (22 tests — **done**, landed separately) | No | 8 mapping + 5 milestoning | — delivered outside this plan |
| **A″** | §3.0 Metadata normalization (**0 tests**, metadata only — **done**) | No | made 13 already-covered capabilities visible + added 4 newly-covered (`grammar:nested-association`, `mapping:relation-{filter,group-by,xstore-association}`) + revived the dead descriptor | Low — done first |
| **B** | §3.2 Relational store features (6 of 8 tests — **done**; right-/full-outer not real) | No | 6 store capabilities (cross-schema, cross-table-filter, dyna-function, inline-view, left-outer-join, nested-join) | Low |
| **B′** | §3.1b Relation-function gaps (1 of 2 tests — `relation-primary-key` **done**; `relation-binding-transformer` moved to §3.7/Phase G) | No | 1 mapping (`mapping:relation-primary-key`) | Low |
| **C** | §3.3 Milestoning (1 test — **done**; all-versions-in-range not supported in Legend grammar) | No | `milestoning:bi-temporal` + table-backed milestoning path | Low–Med |
| **D** | §3.4 Grammar + M2M (10 of 11 tests — **done**; 1 not executable, §3.4a) | `legend-engine-core-emit-tests` (renamed + relocated — §3.4) | 4 grammar + 3 mapping | Med — 1 module |
| **E** | §3.5 Service shapes (4 of 5 tests — **done**; post-validation not executable; the legacy *mapping* fixture landed in the relational suite, not this module) | `legend-engine-xt-service-emit` | 5 execution (multi-execution, shared-test-data, both legacy Phase 5 runners, plan-generation) | Med — 1 module |
| — | §1.8 Feature-driven additions (18 tests, **landed outside this plan**) | No | 12 new taxonomy entries, each covered on arrival | — |
| **A‴** | §3.0a Metadata debt since Phase E (**0 tests**) | No | makes 3 in-use capabilities visible, fixes 8 complexity values and 14 missing `plan-generation` tags, and makes the dashboard's gap list true (32 entries missing from `FULL_TAXONOMY`) | Low |
| **K** | §3.11 Fixture repairs (2 data rewrites, 1 description fix, 2 new fixtures: `relational-join-type`, `grammar-documentation`) | No | turns the three ✅⚠ cells into real ✅ | Low |
| **L** | §3.12 Pipeline paths (5 tests + the §3.4a probe) | No (relational, M2M, service suites); add `legend-engine-test-runner-function` to two poms | function tests, the #4990 resolver path, parameterized service tests, relational graph fetch; possibly the three §3.4a ⛔ | Med |
| **F** | §3.6 File generation (3 tests) | `legend-engine-xt-generation-emit` | real file generation (Avro/Protobuf/JSON Schema) | Med |
| **G** | §3.7 External format (3 tests) | format `-emit` module(s) | 3 execution capabilities (fewer if §4.2 retires `execution:binding`) | Med |
| **H** | §3.8 Other stores (2 tests) | flatdata + serviceStore `-emit` | 2 store capabilities | Med |
| **I** | §3.9 Function activators (3 tests) | 3 activator `-emit` modules | 3 execution capabilities | Higher — 3 modules, verify classpaths |
| **J** | §3.10 Cross-feature combos (2 tests; the third was delivered by Phase E) | No (existing) | `mapping:cross-store` + combination coverage | Low, do last |
| **M** | §3.13 Studio-shaped composites (1 shared bundle + ~5 composites, then ongoing) | No | interaction coverage — not measured by §2 | Med–High, ongoing |

**Recommended order: A‴ → K → L, then M in parallel with F–I, then J.**

- **A‴ and K first.** Both are cheap, and both correct what the catalog *says*.
  `emit-authoring.md` §11.3 ranks a false green as worse than an honest gap, and K
  removes three. A‴ makes the dashboard's gap list true, which every later phase
  reports against.
- **L next.** L needs no new module, and each of its tests enters a runner branch or
  execution path the catalog has never entered, which is where Phase E found a
  defect that had made a whole path unpassable. Function tests, parameterized
  services, and relational graph fetch are also among the most common constructs in
  Studio projects, which is more than can be said of most F–I capabilities.
- **M alongside F–I.** F–I close taxonomy cells, and each is gated on standing up a
  module. M closes no cell and is gated on nothing, but it is the phase that addresses
  §2.11. Start M's shared bundle after K, since K3 and the checklist feed its design.
  Once the model-profile proposal reaches its Phase 1, regenerate profiles for every
  M fixture and use them to confirm each composite is doing what it claims.
- **J last**, as before, and only for combinations that genuinely need the collection
  classpath (§3.10).

**Milestone: every real feature covered at end of Phase I.** Every
`domain:capability` in `emit.md` §6.2 that maps to a real legend-engine feature
has at least one distributed example. The exceptions are
`execution:model-generation`, which has no real implementation (§2.9), and
`execution:post-validation`, which EMIT has no way to execute (§3.5) — both out of
scope. Phase J and the persistence combination extras (§3.4 of
`emit-authoring.md` dedup rules apply) are then incremental combination coverage
rather than gap-closing.

**Milestone: every common Studio test style and query shape executed, at the end of
Phase L.** Function tests, both service-test forms, parameterized service tests, and
relational graph fetch each reach at least one executed fixture. No catalog cell
reads ✅ on data that cannot fail.

**Phase M has no end state.** It is measured by interaction coverage against real
projects once the profile scan exists, not by a count of composites.

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
6. **Make it fail once.** Break the capability under test and confirm the fixture goes
   red. Break the data (remove the discriminating row), the query (drop the navigation
   or filter), or the mapping (remove the keyword). This is how Phase E verified the
   multi-execution empty-bundle guard, and it is the check that would have caught K1
   and K3. It costs a minute per fixture. Record the check in the commit message, not
   in the descriptor.

Step 6 is not yet in `emit-authoring.md`. It should go there as a new §11.4, "Make the
data able to fail", next to the §11.3 rule it generalizes, once Phase K has tried it
in practice.

### 4.2 Taxonomy maintenance

The relation batch added 12 entries to `emit.md` §6.2 **after** the fact (§2.10);
that retrofit is the cautionary case for this section. Beyond it, no new taxonomy
entries are required to close the §2 gaps — every remaining proposed test maps to
an existing `domain:capability`. Both of the additions this section once listed as
optional were made by Phase E, in the same PR as the tests:

- `execution:legacy-mapping-test` / `execution:legacy-service-test` — **added**,
  distinguishing the legacy Phase-5 runner coverage from the modern Testable path
  rather than overloading `execution:service-test` for both (§3.5).
- `execution:plan-generation` — **applied** to the three service-bearing Phase E
  descriptors, so the tag is no longer defined-but-unused. Retro-applying it to the
  pre-existing service models is a separate metadata pass; see the note in §3.5 for
  why it needs per-descriptor checking. (Now scheduled as part of §3.0a.)

Any genuinely new capability discovered while authoring must be added to
`emit.md` §6.2 **in the same PR** as the test (`emit-authoring.md` §10).

**Decisions now open (2026-09-24).** The statement above, that no new entries are
needed to close the §2 gaps, still holds for F–J. It does not hold for the realism
work, and one existing pair needs a ruling first:

1. **`execution:binding` vs. `execution:external-format-binding`.** §6.2 describes both
   as "External format binding". Five descriptors claim the second and none claims the
   first. Recommendation: retire `execution:binding` and let `execution:schema-set`
   carry the schema-backed case. That shrinks Phase G by one cell, rather than closing
   a cell that duplicates one already covered. The alternative is to redefine
   `execution:binding` as "schema-backed Binding (`schemaSet` / `schemaId`)", which
   keeps the count but makes the two entries overlap by design.
2. **Entries for Phase L.** `execution:function-test` (the `FunctionTestRunner`
   path); an entry for the #4990 resolver path (`execution:service-test-data-resolver`
   is a working name); an entry that distinguishes a parameterized service test; and
   `execution:graph-fetch`, retro-applied to the M2M graph-fetch models in the same
   PR. Each of these names a path whose absence §2.11 found. `execution:graph-fetch`
   is the only query-shape entry proposed, and it earns its place because graph fetch
   runs through execution nodes of its own. A general query-shape vocabulary belongs
   to the model-profile proposal, not to hand-authored tags.
3. **The three off-taxonomy tags and one untagged subject** from §3.0a.

Items 2 and 3 follow the same-PR rule. Item 1 should be settled before Phase G
starts.

---

## 5. Summary

**Where things stand (2026-09-24)**

- **100 distributed descriptors and 901 dynamic tests, all passing. 102 of 122**
  taxonomy capabilities have a distributed example. The 18 descriptors added since
  Phase E all came from engine feature PRs (§1.8). They brought 12 new taxonomy
  entries, each covered on arrival, and closed none of the gaps this plan tracks. The
  20 uncovered entries are the same 20 as at Phase E: 9 ⛔ and **11 real gaps**, each
  one `-emit` module away (F–I) or a cross-feature combination (J).
- **The matrix is nearly full, and that makes it nearly uninformative (§2.11).** The
  median model has 2 classes, 1 table, and 1 assertion. No fixture graph-fetches over
  a relational mapping, passes a parameter to a service test, or has a function test
  suite. No fixture uses the #4990 service-test form. And three ✅ cells rest on data
  or queries that cannot fail. The feature-driven models of §1.8 are the exception.
  Each was written to pin a specific engine behavior, and the richest of them pinned
  defects at the junctions between features.
- **Revisit verdicts (§3.11).** §3.0 holds, but its kind of debt has returned (§3.0a).
  Phase A needs one repair (aggregation-aware), Phase B one (explicit join types), and
  Phase C one (bitemporal data). Phase D's grammar fixtures hold, but the grammar they
  cover is rarely used anywhere that executes. Phase E holds but stops at the simplest
  service shape. The §3.4a ruling stands, and a route around it is proposed for
  verification.
- **Next: A‴ → K → L, then M alongside F–I, then J (§4).** Correct what the catalog
  says, then reach the paths it never enters, then build Studio-shaped composites
  while the module-gated taxonomy work proceeds. Measure the composites with the
  model-profile proposal once it exists.

**How it got here (Phases A–E, figures as of 2026-07-31)**

- **90 of 110** taxonomy capabilities had a distributed example at the end of
  Phase E, up from 31 of 93. Phase A closed 19; the separately-landed relation-function batch
  closed 13 more and added 12 capabilities to the taxonomy; §3.0 then added 3 more
  (all immediately covered) and made 14 already-covered-but-mistagged capabilities
  machine-visible; Phase B closed 6 store features, Phase B′ 1 relation-mapping,
  Phase C 1 milestoning, Phase D 7 (4 grammar + 3 mapping, including promoting
  `mapping:mapping` from framework-only to a distributed example), and Phase E 5
  execution capabilities (2 of them taxonomy additions of its own).
- Of the 20 uncovered, **nine are not real targets** —
  `execution:model-generation` has no implementation (§2.9),
  `execution:post-validation` cannot be executed by EMIT (§3.5),
  `mapping:relational-literal-list` is blocked by an engine defect (note 2 under
  §3.1), `store:relational-{right-outer,outer}-join` have no classic-store
  grammar (§3.2), `milestoning:all-versions-in-range-query` is rejected by the
  Legend grammar (§3.3), and `mapping:m2m-local-property` /
  `mapping:operation-mapping-merge` / `mapping:operation-mapping-merge-validation`
  cannot be executed under single-connection M2M test data (§3.4a) — leaving
  **11 real gaps**.
- **Grammar is closed (10/10); mapping (34/40), store (9/13) and now execution
  (10/19) are no longer weak domains.** There is no remaining concentration:
  milestoning is 6/7 (all-versions-in-range out of scope), and each of the 11 real
  gaps is a single named `-emit` module away (§3.6–§3.9) or a cross-feature combo
  (§3.10).
- **§3.0 (done) added no tests** but was the highest-priority item: fourteen
  capabilities were covered by passing tests yet invisible to §2 because of
  off-taxonomy tags, and one descriptor (`relational-service-with-join.yaml`) did
  not run at all because its filename lacked the `.emit` infix. Both are now fixed.
- **Phases B, B′, C, D and E are done** (6 store tests + 1 relation-mapping + 1
  milestoning + 10 core-feature tests + 3 service-shape tests + 2 legacy mapping
  tests, one relational and one M2M). Phase D stood up the
  first new module and closed the grammar domain outright; Phase E stood up the
  second and took execution from the sole remaining concentration to 10/19. Every
  remaining real gap needs a new `-emit` module (F–I: file generation, external
  format, other stores, function activators) or is a cross-feature combo (J).
- **Phase E was the first batch to require a framework change.** No multi-execution
  service could ever have passed EMIT: its test results arrive as a
  `MultiExecutionServiceTestResult` bundle, which `EMITTasks.assertTestPassed` rejected
  as an unexpected type and `EMITRunner` tallied as a failure. Both now unwrap the
  bundle per execution key, and an empty bundle — an atomic test whose `keys` match no
  execution key — is a failure rather than a silent pass. See §3.5.
- **§3.4a is the one place where a batch shipped smaller than planned for a reason
  other than the feature being unreal.** Local properties and merge operations are
  genuine, working engine features; what is missing is a way to *execute* them from
  an M2M mapping test, because `ModelStoreTestConnectionFactory` supplies a single
  source connection and that behavior is deliberately staying as it is. No
  compile-only fixtures were substituted, so §2 does not overstate coverage. Phase J's
  cross-store work was where these three were expected to become provable. The
  service-runtime route proposed in §3.4a (2026-09-24) may get there sooner.
- Every real feature has a distributed example at the end of Phase I. Model
  generation is revisited only if a real extension ships, and post-validation only if
  EMIT Phase 5 gains a hook that runs the assertions. The claim this plan used to make
  here, that the work after Phase I is "combination-level and incremental", was too
  modest: §2.11 shows that combination-level coverage is most of what is missing, and
  Phase M is the plan for it.

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
recommendation to add table-backed milestoning coverage was exactly this case:
milestoning had been proven only over `~func` sources, and Phase C's
`milestoning-bitemporal` added the table-backed path. It needs the K3 repair
(§3.11) before it fully proves that path.

`relation-mixed-association-chain` and `relation-relational-union` are worth
calling out as the models that deliberately span both surfaces in one mapping.
They are the most valuable tests in the relation batch and have no counterpart in
the relational suite.
