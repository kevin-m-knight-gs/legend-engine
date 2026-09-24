# Enriching EMIT Model Descriptors: Derived Model Profiles

**Status:** Proposal, 2026-09-23. Companion to [`emit.md`](emit.md) (design) and
[`emit-authoring.md`](emit-authoring.md) (authoring guide). Section references of the form
"emit.md §6.2" point into those documents.

---

## 1. Summary

The `*.emit.yaml` descriptor records *which* capabilities a model touches (`features`),
but not *how much*, *how intricately*, *in what combination*, or *under what tests*. Two
models that differ by orders of magnitude in real-world complexity carry the same tags.
That is enough for a browsable example catalog. It is not enough to answer the question
the periodic Studio scan has to answer: **is there anything in real Studio projects that
the EMIT corpus does not exercise?**

This proposal does not enrich the descriptor by asking authors to write more into it.
Instead, it adds a **derived model profile**: a generated, committed, verified companion to
each descriptor, computed from the compiled model by a single extractor that the Studio
scanner also runs. The key elements are:

- **Derive, don't author.** Every quantitative or structural fact is computed from the
  model. Authors keep writing `title`, `description`, and the `features` they intend the
  model to prove. The profile records everything else, and CI keeps it honest.
- **One extractor for both sides.** Studio projects and EMIT models are measured by the same
  code, so their profiles are directly comparable. The extractor is extensible through an
  SPI so each extension area owns the facts about its own constructs.
- **Sites and tokens.** A profile is a multiset of *sites* (a constraint, a join, a service
  query, an atomic test, and so on), each described by a set of anonymous, namespaced
  *tokens* such as `constraint.level=Warn`, `fn:meta::pure::functions::date::dateDiff`, or
  `map.join-chain>=4`.
- **Interaction as co-occurrence at a site.** A query site's tokens include everything the
  query *reaches* through the mapping and store. Interaction coverage is then measured as
  t-way tuples of tokens that co-occur at one site, which is the standard technique from
  combinatorial interaction testing.
- **Present, planned, or executed.** Each site carries the strongest evidence EMIT has for
  it. A runtime feature counts as covered only when an executed test reaches it. This makes
  the existing "never compile-only coverage" rule (emit-authoring.md §11.3) mechanical.
- **Open world.** A generic walk of the protocol JSON produces *shape* tokens for every
  construct, known or not. As a result, a construct that no profiler understands yet still
  shows up as new.
- **Anonymous by construction.** Profiles contain no element names, property names,
  literals, or data values, so Studio-project profiles can be stored and compared without
  carrying proprietary content.

> **Reconcile before building.** The downstream harvest already uses a richer model to
> describe Studio projects. I have not seen that model. Phase 0 (§13) should reconcile the
> two so that there is exactly one extractor. The value of this design depends on EMIT
> models and Studio projects being measured the same way.

---

## 2. Why the Current Descriptor Falls Short

### 2.1 Presence, not magnitude

`grammar:constraint` means "some class has a constraint." A class with one
`$this.age >= 0` and a class with nine constraints that use `forAll` over to-many
navigations, `dateDiff`, `Warn` enforcement, and dynamic message lambdas carry the same
tag. `store:relational-multi-table` covers both a two-table store with one join and a
forty-table, three-schema store with sixty joins and composite keys.

### 2.2 Model-level co-occurrence, not interaction

`features` is a flat set for the whole model. The dashboard's feature combinations are
computed from these model-level sets. However, two features in the same model need not
interact. A model can contain an embedded mapping and a milestoned class that no query
ever touches together. Conversely, the interaction that matters in the field is usually
local: *this* graph-fetch query navigates *this* milestoned property through *this*
embedded mapping reached by *this* three-hop join chain.

### 2.3 Tests are invisible

Nothing in the descriptor says which elements have tests, what the test data looks like,
how complex the test queries are, what they assert, or which of the model's features a test
actually reaches. Only the phase in which tests run (TEST_EXECUTION) and a few `execution:*`
tags hint at it. The multi-execution service defect fixed in Phase E (no multi-execution
service test could ever have passed EMIT, because `MultiExecutionServiceTestResult` was
neither `TestExecuted` nor `TestError`) is exactly a test-dimension interaction, multi-execution
service × modern test suite. A test-aware descriptor would have shown that Studio projects
contain it and the catalog did not.

### 2.4 Authored intent is not an inventory

The tagging conventions deliberately tag the *subject* of a model, not everything in it.
For example, the `EmployeeType` enumeration mapping is suite-wide scaffolding and is tagged
only where it is the subject. That is the right call for a catalog, but it makes the tags
useless as an inventory:

- 44 classic-relational, service, and persistence fixtures declare an explicit
  `~primaryKey`, but only 2 descriptors carry `mapping:relational-primary-key`.
- `service-shared-domain` defines the derived property `demo::Person.fullName`, and every
  service fixture in that module depends on it, yet no query in those fixtures navigates
  it. The tag `grammar:derived-property` on the shared domain is true, but no consumer
  *exercises* the derived property.

A coverage comparison needs the inventory *and* the evidence, not the intent.

### 2.5 Hand-maintained vocabulary drifts

The taxonomy exists twice: as tables in emit.md §6.2 and as the hard-coded `FULL_TAXONOMY`
list in `EMIT_to_HTML`. Today the report's list is missing 32 of the 122 documented entries,
including every `persistence:*` entry, every `mapping:relation-*` entry, and both legacy test
runners. Gaps in those areas are therefore invisible on the dashboard's coverage-gaps tab.
Asking authors to hand-maintain counts, function lists, and test characteristics on top of
this would multiply the problem.

---

## 3. Requirements

| # | Requirement | Consequence |
|---|---|---|
| R1 | **Comparable.** The same facts must be extracted from a Studio project and from an EMIT model. | One extractor, used by both EMIT and the scanner. No hand-authored facts in comparisons. |
| R2 | **Quantitative.** Capture magnitude as well as presence (counts, depths, sizes). | Numeric metrics with an encoding that supports "at least as large." |
| R3 | **Interaction-aware.** Capture which features combine *at the same place*, including across element boundaries (query → mapping → store). | Sites plus reach analysis. |
| R4 | **Test-aware.** Capture testables, suites, data, parameters, queries, assertions, and what each test reaches. | A first-class `test` site kind. |
| R5 | **Honest.** Distinguish constructs that are merely present from constructs that are planned or executed. | Evidence levels on every site. |
| R6 | **Open world.** Detect constructs nobody has written a profiler for yet. | A generic shape fingerprint. |
| R7 | **Anonymous.** Leak no business content from Studio projects. | Tokens never carry names, literals, or data values. |
| R8 | **Maintainable.** Adding a model must not add hand bookkeeping; adding a construct to the engine must have one obvious place to describe it. | Generated profiles, an SPI per extension area, and one machine-readable taxonomy. |
| R9 | **Stable.** Profiles must not churn on unrelated engine changes. | A versioned vocabulary, static analysis rather than raw plan or SQL text in committed files. |

---

## 4. Design Decisions

- **D1: Derive, don't author.** Every fact in the profile is computed. Authored fields keep
  only what cannot be computed: the model's name, prose, the capabilities it is *meant* to
  prove, free-form tags, and provenance.
- **D2: One extractor, both sides.** The extractor is published from legend-engine and runs
  unchanged in EMIT and in the downstream scanner. Its version is the profile's
  `profileVersion`. Profiles are compared only at equal versions.
- **D3: Authored descriptor, generated profile.** The profile lives in a sibling file
  `<stem>.emit-profile.yaml`, not inside `*.emit.yaml`. Rewriting a hand-authored YAML file
  loses its comments and mixes generated diffs with human edits.
- **D4: Sites and tokens are the universal unit.** Every fact is a token on a site. The
  comparator needs no knowledge of what any token means.
- **D5: Interaction is t-way co-occurrence at a site.** See §7.
- **D6: Evidence levels.** Each site is `present`, `planned`, or `executed`, and each token
  family declares the minimum level that counts as coverage.
- **D7: Anonymous by construction.** No names, literals, or data values appear in tokens.
  EMIT profiles may add an optional, non-anonymous `origins` section for debugging, which the
  comparator ignores.
- **D8: Open world.** The shape family is generated by a generic walk that needs no
  per-construct code. Semantic profilers add meaning on top, and elements or sub-objects that
  no profiler claims are reported as `unhandled:` tokens.

---

## 5. The Model Profile

### 5.1 Sites and tokens

A **site** is any place in a model where a feature can occur. Sites are one of two sorts:

- **Structural sites** describe declared structure: `class`, `association`, `enumeration`,
  `database`, `table`, `view`, `join`, `class-mapping`, `mapping`, `connection`, `runtime`,
  `service`, `data`, `test-suite`, and so on.
- **Behavioral sites** describe code that runs: `constraint`, `derived-property`, `query`
  (a service query, mapping-test query, function body, or data-space executable),
  `m2m-property` (an M2M transform expression), `relation-func` (a `~func` body), and
  `test` (one per atomic test).

A **token** is an anonymous, namespaced string describing one fact about a site. A site's
**record** is its kind, its evidence level, and its sorted token set. Identical records are
collapsed into one entry with a `count`. The profile is therefore a multiset of records.
This preserves the correlations *within* a site (the class with twelve constraints is also
the one with three qualified properties) while staying compact and anonymous.

### 5.2 Token grammar and numeric encoding

| Form | Example | Meaning |
|---|---|---|
| `family.attr` | `constraint.external-id` | Boolean fact |
| `family.attr=value` | `constraint.level=Warn` | Categorical fact |
| `family.attr>=N` | `map.join-chain>=4` | Threshold fact (monotone metric) |
| `fn:<id>` | `fn:meta::pure::functions::date::dateDiff` | Resolved platform function used at this site |
| `shape:<path>` | `shape:Mapping>classMappings[relational]>propertyMappings[embedded]` | Protocol shape (§6.1) |
| `unhandled:<type>` | `unhandled:Mapping>classMappings[someNewKind]` | A protocol subtype that no profiler claims |

**Threshold encoding.** Monotone metrics use power-of-two thresholds (1, 2, 4, 8, 16,
32, ...). A value of 5 implies `>=1`, `>=2`, and `>=4`. The profile writes only the highest
threshold, and the comparator expands it. This thermometer encoding means that "an EMIT site
at least as large as the Studio site" falls out of plain set containment (§9.2) with no
special-casing. Metrics whose specific values select different code paths (primary-key
arity, for example) use categorical encoding instead: `rel.table.pk=none|single|composite`.
Each metric declares its encoding in the vocabulary.

**Functions.** `fn:` tokens carry the resolved function's package path, the identity PCT
reports use. They do not carry the unresolved name in the protocol lambda. This separates
legacy TDS `project` from relation `project`, for example. Calls to user-defined functions
are recorded as `fn:user`. Their bodies are profiled at the call site (with
`fn.user-depth>=N`), because what executes is the inlined body.

### 5.3 Evidence levels

| Level | Meaning | Source in the EMIT pipeline |
|---|---|---|
| `present` | Declared in the primary scope and compiled. | COMPILE (emit.md §4.3) |
| `planned` | Reached by a primary-scope service query for which a plan is generated. | PLAN_GENERATION (emit.md §4.7) |
| `executed` | Reached by an in-scope test that EMIT runs. EMIT CI requires that test to pass. | TEST_EXECUTION (emit.md §4.6) |

Each token family declares the minimum level at which it counts as coverage. Pure language
structure (`class.*`, `enum.*`, and shapes) counts at `present`. Anything whose meaning is
runtime behavior (`query.*`, `map.*`, `rel.join.*`, `test.*`, and `fn:` at query sites)
counts only at `executed`. The authoring rule "don't tag what the fixture cannot execute"
thus becomes a check the Profile task enforces (§10.3).

### 5.4 Scope: primary versus reached

The **inventory** counts only primary-scope elements (emit.md §4.1). Otherwise every consumer
of `relational-shared-domain` would claim that domain's contents. **Reach** (§7.1) follows
references into dependencies. When a primary-scope test executes a mapping defined in a
dependency, that mapping's tokens appear on the test site at `executed`. The same rule
applies to Studio projects and their upstream project dependencies.

### 5.5 Document layout

```yaml
# <stem>.emit-profile.yaml -- generated by the EMIT Profile task; do not edit.
profileVersion: 1
model: <descriptor name>
tiers: [shape, semantic, reach]       # which extractor tiers ran (§10.1)

inventory:          # primary-scope element counts by protocol type
  <ElementType>: <n>
reached:            # dependency-scope elements reached from primary-scope sites
  <ElementType>: <n>

summary:            # raw human-facing metrics (§6.9); never used for comparison
  ...

features:           # taxonomy features derived from the records (§10.4)
  detected:         # each with its strongest evidence level
    - id: <domain:capability>
      evidence: present|planned|executed
  notReached:       # detected in reached dependency content, but no primary-scope site reaches it
    - <domain:capability>

shapes:             # sorted shape tokens (§6.1)
  - <shape path>

records:            # the site multiset
  - kind: <site kind>
    evidence: present|planned|executed
    count: <n>
    tokens: [<sorted tokens>]

origins:            # optional, EMIT only, ignored by the comparator
  - record: <index>
    elements: [<element paths>]
```

---

## 6. What the Profile Captures

The tables below are illustrative, not exhaustive. Each extension area's profiler owns and
documents its own vocabulary (§10.2). What matters here is the coverage of the questions in
§6.10.

### 6.1 Shape fingerprint (open world)

The shape tier serializes each primary-scope element with the protocol `ObjectMapper` and
walks the JSON. At each object carrying a `_type`, it records the path of discriminators
from the element root, for example
`Mapping>classMappings[relational]>propertyMappings[embeddedPropertyMapping]`. It also
records each non-default field it passes (`Service.postValidations`, `Service.mcpServer`).
Inside lambdas, it records only `classInstance` types (`rootGraphFetchTree`, `colSpec`,
`relationStoreAccessor`, and so on), because those are the grammar islands. The rest of a
lambda belongs to the expression tier.

The shape tier needs no compilation and no per-construct code. It therefore works on Studio
projects that fail to compile. It catches a construct the moment it first appears in a
Studio project, even before anyone has written a semantic profiler for it.

### 6.2 Expressions (shared vocabulary)

Every lambda-bearing site (`constraint`, `derived-property`, `query`, `m2m-property`,
`relation-func`, and the queries inside `test`) uses one expression vocabulary:

| Tokens | Captures |
|---|---|
| `expr.nodes>=N`, `expr.depth>=N`, `expr.lambda-nesting>=N`, `expr.lets>=N` | Size and shape of the expression |
| `expr.control=if\|match` | Control flow |
| `lit=string\|integer\|float\|decimal\|strictdate\|datetime\|latest\|boolean\|enum\|collection` | Literal kinds (never values) |
| `fn:<id>`, `fn:user`, `fn.user-depth>=N` | Functions used, resolved against the compiled graph |
| `nav.depth>=N`, `nav.to-many`, `nav.qualified`, `nav.derived`, `nav.subtype` | Property navigation |
| `nav.milestoned=with-date\|latest\|propagated`, `nav.all-versions` | Temporal navigation |
| `query.kind=graph-fetch\|tds-project\|relation\|instances\|scalar` | Query result shape |
| `query.op=filter\|sort\|take\|drop\|slice\|distinct\|group-by\|olap\|join\|union\|extend\|pivot` | Normalized operations, independent of TDS, relation, or class form |
| `query.cols>=N`, `query.params>=N`, `query.param.type=...` | Projection width and parameters |
| `gf.depth>=N`, `gf.breadth>=N`, `gf.subtype-tree`, `gf.qualified`, `gf.checked` | Graph-fetch trees |
| `query.from`, `query.serialize=<option>`, `query.externalize=<content type>` | Inline mapping or runtime, and output handling |

`query.kind` and `query.op` line up with the MFT feature-matrix profiles
(`meta::pure::testCoverage::featureMatrix::QueryType`, `QueryFeature`, and
`MappingFeature`). EMIT and MFT coverage can therefore be read against one vocabulary later.

### 6.3 Core language

| Site | Tokens (examples) |
|---|---|
| `class` | `class.properties>=N`, `class.prop.mult=0..1\|1\|*\|1..*\|range`, `class.prop.type=primitive:<name>\|enum\|class\|measure\|unit`, `class.derived>=N`, `class.qualified>=N`, `class.constraints>=N`, `class.supertypes>=N`, `class.hierarchy-depth>=N`, `class.milestoning=business\|processing\|bitemporal`, `class.defaults`, `class.stereotype=<platform profile.stereotype>\|user`, `class.tagged-values>=N` |
| `constraint` | `constraint.level=Error\|Warn`, `constraint.external-id`, `constraint.message=static\|dynamic`, `constraint.owner`, plus the expression vocabulary (§6.2) |
| `derived-property` | `derived.params>=N`, `derived.return.mult=...`, plus the expression vocabulary |
| `association` | `assoc.mult=1-1\|1-*\|*-*`, `assoc.derived`, `assoc.milestoned-end` |
| `enumeration` | `enum.values>=N`, `enum.tagged` |
| `query` (function) | `fn-def.params>=N`, `fn-def.returns=relation\|tds\|class\|primitive`, `fn-def.pre-constraints`, `fn-def.post-constraints`, `fn-def.tests`, plus the expression vocabulary |

Constraints are the motivating example. The profile emits one `class` record with
`class.constraints>=8` and one `constraint` record per constraint. Two models tagged
`grammar:constraint` then look like this:

```yaml
# Model A: one basic constraint
- kind: constraint
  evidence: present
  count: 1
  tokens: [constraint.level=Error, expr.depth>=2, expr.nodes>=4,
           fn:meta::pure::functions::math::greaterThanEqual, lit=integer, nav.depth>=1]

# Model B: nine constraints; seven share this record
- kind: constraint
  evidence: executed          # a checked graph fetch in a test reaches the class
  count: 7
  tokens: [constraint.external-id, constraint.level=Warn, constraint.message=dynamic,
           expr.depth>=8, expr.lambda-nesting>=2, expr.nodes>=32,
           fn:meta::pure::functions::boolean::and, fn:meta::pure::functions::boolean::or,
           fn:meta::pure::functions::collection::exists, fn:meta::pure::functions::collection::forAll,
           fn:meta::pure::functions::date::dateDiff, lit=strictdate,
           nav.depth>=2, nav.milestoned=with-date, nav.qualified, nav.to-many]
```

### 6.4 Mappings

| Site | Tokens (examples) |
|---|---|
| `mapping` | `mapping.class-mappings>=N`, `mapping.includes>=N`, `mapping.store-substitution`, `mapping.enum-mappings>=N`, `mapping.assoc-mapping=relational\|xstore`, `mapping.test-suites>=N`, `mapping.legacy-tests>=N` |
| `class-mapping` | `map.class=relational\|pure\|operation:union\|operation:merge\|operation:router\|aggregation-aware\|relation-func\|flat-data\|service-store\|...`, `map.root`, `map.set-id`, `map.extends`, `map.props>=N`, `map.prop=column\|join\|embedded\|inline-embedded\|otherwise-embedded\|dyna\|enum-transform\|local\|xstore\|binding\|literal\|expression`, `map.join-chain>=N`, `map.join.type=INNER\|LEFT_OUTER\|...`, `map.filter`, `map.filter.via-join`, `map.distinct`, `map.group-by`, `map.pk=explicit`, `map.main-table` |
| `m2m-property` | `m2m.source=model-connection\|chained\|...`, plus the expression vocabulary |
| `relation-func` | The expression vocabulary, plus `map.relation.window`, `map.relation.group-by` |

### 6.5 Relational stores, connections, and runtimes

| Site | Tokens (examples) |
|---|---|
| `database` | `rel.schemas>=N`, `rel.tables>=N`, `rel.views>=N`, `rel.joins>=N`, `rel.filters>=N`, `rel.multigrain-filter`, `rel.includes>=N`, `rel.join-graph.max-degree>=N`, `rel.join-graph.components>=N`, `rel.join-graph.cycle` |
| `table` | `rel.table.columns>=N`, `rel.col.type=VARCHAR\|INTEGER\|TIMESTAMP\|DECIMAL\|SEMISTRUCTURED\|...`, `rel.table.pk=none\|single\|composite`, `rel.table.milestoning=business\|processing\|bitemporal`, `rel.table.milestoning.infinity-date` |
| `join` | `rel.join.conjuncts>=N`, `rel.join.self`, `rel.join.dyna`, `rel.join.literal`, `rel.join.cross-schema` |
| `view` | `rel.view.cols>=N`, `rel.view.filter`, `rel.view.group-by`, `rel.view.distinct`, `rel.view.joins>=N` |
| `connection` | `conn.type=relational:<database type>\|json-model\|xml-model\|model-chain\|...`, `conn.spec=<datasource spec type>`, `conn.auth=<auth strategy type>`, `conn.post-processor=<type>`, `conn.timezone`, `conn.quote-identifiers` |
| `runtime` | `runtime.kind=engine\|single-connection\|...`, `runtime.mappings>=N`, `runtime.connections>=N`, `runtime.stores>=N`, `runtime.inline` |

Join type lives on the mapping's `JoinPointer`, not on the store's join, so it is a
`map.join.type` token. The same join can be used as `INNER` in one property mapping and as
`LEFT_OUTER` in another.

### 6.6 Services

| Site | Tokens (examples) |
|---|---|
| `service` | `svc.exec=single\|multi`, `svc.exec.keys>=N`, `svc.runtime=inline\|ref`, `svc.params>=N`, `svc.param.type=...`, `svc.pattern.params>=N`, `svc.post-validations>=N`, `svc.test-suites>=N`, `svc.legacy-test=single\|multi`, `svc.ownership=<kind>` |
| `query` | Service query (one per execution key). The expression vocabulary plus `route=<store kind>:<detail>` and reach tokens (§7.1). |

### 6.7 Tests

Tests are a first-class site kind, one `test` record per atomic test (including legacy
mapping and service tests), plus a `test-suite` record per suite.

| Aspect | Tokens (examples) |
|---|---|
| Which elements have tests | `test.testable=mapping\|service\|function\|persistence\|data-quality\|legacy-mapping\|legacy-service\|<other>` |
| Suite structure | `test-suite.tests>=N`, `test-suite.data=suite\|per-test`, `test-suite.stores>=N` |
| Test data kind | `test.data=relational-csv\|external-format:<content type>\|model-store\|relation-elements\|service-store\|reference\|<other>`, `test.data.target=<kind>` for a `reference` |
| Test data sharing | `test.data.ref.shared>=N` (how many suites or testables consume the same `Data` element) |
| Test data size | `test.data.stores>=N`, `test.data.tables>=N`, `test.data.rows>=N`, `test.data.bytes>=N`, `test.data.json.depth>=N`, `test.data.model-store.classes>=N` |
| Test data character | `test.data.nulls`, `test.data.empty-table`, `test.data.unicode`, `test.data.value=date\|datetime\|tz-datetime\|decimal\|large-int\|boolean`. These are computed by parsing the data. Only the characteristics are recorded, never the values. |
| Parameters | `test.params>=N`, `test.param.type=...`, `test.keys>=N` (multi-execution keys) |
| Query | The full expression vocabulary of the query the test runs: the service query for service tests, the test's own query for mapping tests, and the function body for function tests |
| Assertions | `test.asserts>=N`, `test.assert=EqualTo\|EqualToJson\|EqualToRelation\|legacy-lambda`, `test.expected.format=json\|xml\|csv\|value`, `test.expected.records>=N`, `test.expected.depth>=N`, `test.expected.empty`, `test.serialization=<format>` |
| What is tested | Reach tokens (§7.1): every mapping, store, class, and constraint token the test's query reaches |

`test.expected.empty` deserves a note. The `service-simple` example in emit.md §7 asserts
`'[]'`. A test that expects nothing proves very little about a mapping, and the profile makes
that visible. Legacy service-test assertions are lambdas, so they are profiled with the
expression vocabulary.

### 6.8 Other areas

Each area contributes its profiler and vocabulary through the SPI (§10.2). The first cut
follows the existing taxonomy. Persistence (`pers.dataset=snapshot|delta`,
`pers.target=nontemporal|unitemporal|bitemporal`, `pers.updates=...`,
`pers.delete-indicator`, `pers.audit`, `pers.notifier=...`, and `pers.output=graph-fetch|tds`),
external formats (`xf.binding.content-type`, `xf.schema-set.format`, `xf.schemas>=N`), generation
(`gen.model=<type>`, `gen.file=<type>`), function activators (`act.kind=...`,
`act.deployment=...`), data spaces (`ds.exec-contexts>=N`, and each executable as a `query`
site), data quality, the service store, and flat data are covered the same way. Element
types with no engine semantics, such as diagrams and text, appear only in `inventory` and
`shapes`.

### 6.9 Summary metrics

`summary` holds raw, human-facing numbers. It answers "how big is this?" at a glance in a
PR diff or on the dashboard, for example: largest class by constraint count, widest table,
longest join chain, largest query, and totals of tests, assertions, and test-data rows. The
comparator never reads it. Comparisons use records only.

### 6.10 Where each question is answered

| Question | Answered by |
|---|---|
| How many constraints, and how complex are they? | `class.constraints>=N` on `class` records; `expr.*` and `nav.*` on `constraint` records; the constraint maxima in `summary` |
| What functions do constraints use? | `fn:` tokens on `constraint` records |
| How many tables, columns, and joins are there? | `database`, `table`, and `join` records; `summary.relational` |
| How do features interact? | Reach-expanded behavioral records and their t-way tuples (§7) |
| What kinds of elements have tests? | `test.testable=...` |
| What is the test data like? | `test.data*` tokens |
| How complex are test queries, and which functions do they use? | Expression tokens on `test` records |
| Which features are tested? | Reach tokens on `test` records; `features.detected` at `executed` |
| What kinds of assertions are there? | `test.assert=...` and `test.expected.*` |
| Has anything new appeared? | `shape:` and `unhandled:` tokens absent from every EMIT profile |

---

## 7. Interaction: Reach and T-Way Tuples

### 7.1 Reach

A behavioral site's record includes the tokens of everything it reaches. For a query:

1. **Property tree.** Compute the classes and property paths the query touches: `getAll`
   roots, projection and filter lambdas, graph-fetch trees, and user-function bodies. Core
   already provides this as `meta::pure::lineage::scanProperties::scanProperties`, which
   lineage and mapping analytics use today.
2. **Set implementations.** For each class node, take the set implementations the query's
   mapping provides. For operations and unions, take all of them. For each property edge,
   take the property mappings in those set implementations, including embedded, local,
   xstore, and M2M transform mappings.
3. **Store objects.** Follow join pointers to joins and tables, and follow the runtime to
   connections.
4. **Class behavior.** Include `constraint` sites when the query checks them (`gf.checked`,
   or an M2M target), and `derived-property` sites when the query navigates them. Recurse
   into their expressions.

Reach deliberately over-approximates. For example, it takes all set implementations for a
class when the router would pick one. The profile states that, and the optional dynamic tier
(§13, Phase 5) can confirm it against real plans later. Relation queries over
`#>{db.table}#` reach tables directly. M2M reach continues from a target property through its
transform into the source class's own set implementations, which is how chained mappings
surface.

### 7.2 Tuples

For each behavioral record, the comparator forms every **t-subset** of its tokens (excluding
`shape:` tokens). A t-subset is one **interaction**. Empirical work on combinatorial
interaction testing (NIST's studies of real-world failures) finds that most failures are
triggered by one or two factors, and nearly all by three or fewer. Coverage at t = 2 or 3
is therefore the practical target. The proposed defaults are t = 2 for every site kind and
t = 3 for `query` and `test` sites.

A Studio graph-fetch service over a milestoned class, whose embedded mapping sits behind a
join chain, with constraint checking, yields tokens such as
`{query.kind=graph-fetch, gf.checked, nav.milestoned=with-date, map.prop=embedded,
map.join-chain>=2, ...}`. Even if every token appears *somewhere* in the EMIT corpus, the pair
`(gf.checked, map.prop=embedded)` may appear on no single EMIT site. That pair is the gap,
stated precisely enough to write a fixture for.

Because the thermometer encoding writes only the highest threshold, the Studio side needs
only the tuples that use the highest threshold. The EMIT side keeps every threshold, so
containment still holds.

### 7.3 Why not whole-site signatures

- **Exact match** is sound but hopeless. Almost every real query site is unique, so almost
  everything reads as a gap.
- **Superset dominance** (an EMIT site whose tokens are a superset of the Studio site's) is unsound. Extra
  features change code paths: an embedded mapping under a union is not the same path as one
  without it. It is also still sparse.
- **T-way tuples** sit between the two. They are the unit that the testing literature and
  the existing MFT feature matrix (which enumerates query type × query feature × mapping
  feature) already use.

---

## 8. Changes to `*.emit.yaml`

The authored descriptor changes very little.

| Field | Change |
|---|---|
| `name`, `title`, `description`, `modelSources`, `tags` | Unchanged. |
| `features` | Syntax unchanged. The meaning is tightened to the capabilities the model is **meant to prove**. The Profile task verifies that each claimed feature is detected at or above that feature's required evidence level. Scaffolding entries become optional, because they are derived anyway, and can be dropped in a later mechanical cleanup. |
| `stores`, `complexity` | Derived. During a transition, the Profile task checks that the authored values agree with the derived ones. Afterward, remove them from the descriptors. The report reads the derived values. |
| `provenance` (new, optional) | Links a model to the scan gaps it was written to close, so the next scan can confirm closure: `provenance: {source: studio-scan, scan: 2026-10, gaps: [<tuple ids>]}`. Tuple ids are anonymous hashes. |

`EMITModelDescriptor` gains `provenance`. It already ignores unknown properties, so older
readers are unaffected.

A new generated sibling, `<stem>.emit-profile.yaml`, sits next to each descriptor. It does
not match the `.emit.yaml` discovery suffix, so discovery is unaffected.

---

## 9. Comparing Against Studio Projects

### 9.1 The scan loop

1. **Profile.** The scanner profiles every Studio project with the same extractor version
   as the platform projects' committed profiles. It uses the full extension collection on
   its classpath. The shape tier runs even when a project fails to compile.
2. **Aggregate.** Build the Studio corpus: for each shape, token, and tuple, the number of
   projects and sites that contain it. Private side tables can map tuple ids back to
   project and element for triage. Those tables are never part of the profile.
3. **Union the EMIT corpus.** Read the committed profiles of every platform project that
   hosts EMIT models, legend-engine and others alike.
4. **Diff.** Report new shapes and `unhandled:` types, tokens absent from EMIT, and uncovered
   tuples. Remove everything in the exclusions registry (§9.3), rank the rest by prevalence,
   and show the trend since the previous scan.
5. **Close.** Author or extend EMIT models. Their `provenance` names the closed gaps. The
   next scan confirms the closure.

### 9.2 Coverage rule

A Studio tuple τ from a site of kind *k* is **covered** when some EMIT record of the same
kind *k*, at or above τ's required evidence level, contains τ after threshold expansion.
The site kind is part of the rule because `fn:...::dateDiff` in a constraint and
`fn:...::dateDiff` in a relational query go through different code paths.

### 9.3 Exclusions registry

Several capabilities appear in Studio projects but cannot be proven by EMIT. The
not-provable rulings from the coverage work are examples:

- A multi-class `ModelStore` test-data block, because `ModelStoreTestConnectionFactory`
  supplies one source connection per block.
- Service post-validations, which only `ServicePostValidationRunner` evaluates.

A small machine-readable registry (`emit-coverage-exclusions.yaml` in `legend-engine-emit`)
lists each such token or tuple with its reason and ruling date, in the same spirit as PCT's
expected-failure manifests. The scanner reports excluded items as *known unprovable* instead
of raising them as new gaps on every run. It also records the rulings in the repository
rather than in prose.

### 9.4 Harvest fidelity

The same machinery measures the problem that motivated this proposal: EMIT models generated
from Studio projects came out too simple. Profile a Studio project and the EMIT model
harvested from it. The fraction of the project's tuples that the harvested model retains is
a direct, quantitative measure of harvest fidelity.

---

## 10. Architecture

### 10.1 Modules and tiers

| Module | Contents |
|---|---|
| `legend-engine-core-emit/legend-engine-emit-profile` (new) | Token and record model; profile document serialization; shape fingerprinter; expression profiler over compiled lambdas; reach analyzer; `EMITProfileExtension` SPI; profilers for core-language constructs, M2M mappings, core embedded data, and assertions; tuple generator and comparator; taxonomy registry loader. No JUnit dependency. |
| One small profiler module per extension area (new), for example `legend-engine-xt-relationalStore-emit-profile` | That area's `EMITProfileExtension` implementation and its taxonomy file. These are published main artifacts, so the downstream scanner and other platform projects can depend on them. |
| `legend-engine-emit` | New Profile task (§10.3). `EMITModelDescriptor` gains `provenance`. |
| `legend-engine-emit-report` and `legend-engine-emit-maven-plugin` | Read committed profiles and the taxonomy files. Neither needs an extension classpath. |

The extractor runs in three tiers. **Shape** needs only the PMCD. **Semantic** (records and
function resolution) needs the compiled `PureModel`. **Reach** needs both. EMIT always has
all three: `EMITModel` already carries the PMCD, the `PureModel`, and the primary-scope set.
The scanner records which tiers succeeded for each project.

Per-area profilers get their own modules for two reasons. They cannot live in core, because
core must not depend on `xts-*` protocol classes. They should not live in the areas'
`-compiler` modules either, because that would make production modules depend on a test
harness. If new modules are unwelcome, the alternative is to give each existing `-emit`
module a main source set for its profiler. However, emit-authoring.md §9 currently forbids
production code in those modules.

### 10.2 SPI

```java
public interface EMITProfileExtension
{
    // Structural records for the protocol element and sub-object types this area owns.
    Iterable<? extends SiteProfiler<?>> getSiteProfilers();

    // Contributions to reach: for example, how a relational property mapping reaches joins and tables.
    Iterable<? extends ReachContributor<?>> getReachContributors();

    // Vocabulary declarations: token families, encodings, and required evidence levels.
    Iterable<TokenFamily> getTokenFamilies();
}
```

Implementations are loaded with `ServiceLoader`, like `CompilerExtension` and the other
engine extensions. Any protocol subtype that no profiler claims yields an `unhandled:`
token. Missing profilers are thus visible in every scan instead of silently producing
thinner profiles.

### 10.3 The Profile task

`EMITTestSuiteBuilder` adds one task per model after model generation:
`[model] Catalog: Profile`. It uses the enriched PMCD, so generated elements are profiled.
The task:

1. Computes the profile.
2. Compares it with the committed `<stem>.emit-profile.yaml`. On a mismatch, it fails with a
   readable diff and the regeneration command. With `-Demit.profile.update=true`, it
   rewrites the file instead.
3. Verifies that the claimed `features` are detected at the required evidence level, that
   the authored `stores` and `complexity` agree with the derived values (during the
   transition), and that every token family is declared.

Committing the profile makes every coverage change reviewable in the PR that causes it.
It also lets the report and the scanner read profiles without compiling anything. Churn is
limited to deliberate vocabulary changes, which bump `profileVersion` and regenerate every
profile with one command. Nothing that varies with unrelated engine changes, such as SQL
text or plan node layout, goes into the committed file.

### 10.4 One taxonomy, machine-readable

Each `domain:capability` entry moves out of prose and code into a taxonomy file, one per
profiler module, merged at load time:

```yaml
- id: store:relational-left-outer-join
  domain: store
  description: Left outer join
  detect: {sites: [query, test], anyToken: [map.join.type=LEFT_OUTER]}
  evidence: executed
```

The taxonomy file becomes the single source for detection, claim validation, the report's
coverage-gaps list (replacing `FULL_TAXONOMY`, and with it the 32-entry drift in §2.5),
and the emit.md §6.2 tables. Those tables can be generated or replaced by a reference to the
file. The existing `domain:capability` ids are kept unchanged.

### 10.5 Report

The dashboard reads the committed profiles and gains the following views:

- Scale per model, from `summary`.
- A function-in-context matrix (function × site kind).
- A test matrix (testable kind × data kind × assertion kind).
- An interaction heatmap for chosen token families.
- A derived-versus-claimed discrepancy list.
- The exclusions registry.

The complexity badge is derived. No single composite "complexity score" is proposed:
coverage is a set question, and a scalar would hide exactly the differences this proposal
exists to expose.

---

## 11. Effects on Existing Guidance

| Guidance | Change |
|---|---|
| emit.md §6.1 (descriptor) | Document `provenance` and the generated profile. Mark `stores` and `complexity` as derived. |
| emit.md §6.2 (taxonomy) | Point to the taxonomy files. Keep the domain descriptions and the "reuse before you invent" rules. |
| emit.md §6.3 (catalog index) | The profiles are the catalog index's data. |
| emit.md §9 (future extensions) | "Auto-derived metadata" and "Catalog completeness CI check" are delivered by this proposal. |
| emit-authoring.md §4, step 4 | Authors write `features` as claims and regenerate the profile. |
| emit-authoring.md §11.1 (classification) | Complexity is derived, not counted by hand. |
| emit-authoring.md §11.2 (deduplication) | Replace "skip only on an exact feature-set match" with: a candidate adds value if it contributes at least one new exercised shape, token, or tuple relative to the corpus. The comparator computes this. |
| emit-authoring.md §11.3 (don't tag what you cannot execute) | Enforced by the Profile task. Rulings go into the exclusions registry. |

---

## 12. Worked Example: `service-shared-test-data`

The model has two services over the shared H2 person mapping. One lists all people, and the
other filters by last name. Both test suites prime their connection from one `Data` element
by `Reference`. The mapping, store, and class come from the `service-shared-domain`
dependency. Function ids are abbreviated below; the profile records full package paths.

```yaml
profileVersion: 1
model: service-shared-test-data
tiers: [shape, semantic, reach]

inventory:
  DataElement: 1
  PackageableConnection: 1
  PackageableRuntime: 1
  Service: 2
reached:
  Class: 1
  Database: 1
  Mapping: 1

summary:
  queries: {count: 2, distinctFunctions: 4}
  tests: {testables: 2, suites: 2, tests: 2, assertions: 2}
  testData: {dataElements: 1, references: 2, tables: 1, rows: 2}
  relational: {tables: 1, columns: 3, joins: 0}

features:
  detected:
    - {id: execution:data-element, evidence: executed}
    - {id: execution:plan-generation, evidence: planned}
    - {id: execution:service, evidence: executed}
    - {id: execution:service-test, evidence: executed}
    - {id: execution:shared-test-data, evidence: executed}
    - {id: execution:test-data, evidence: executed}
    - {id: mapping:relational-primary-key, evidence: executed}   # detected, not claimed
  notReached:
    - grammar:derived-property    # demo::Person.fullName: defined in the dependency, never navigated

records:
  - kind: test
    evidence: executed
    count: 1
    tokens: [fn:getAll, fn:tds::project, map.class=relational, map.main-table, map.pk=explicit,
             map.prop=column, nav.depth>=1, query.cols>=2, query.kind=tds-project,
             rel.table.pk=single, route=relational:H2, test.assert=EqualToJson, test.asserts>=1,
             test.data=reference, test.data.ref.shared>=2, test.data.rows>=2,
             test.data.target=relational-csv, test.expected.format=json,
             test.expected.records>=2, test.serialization=PURE_TDSOBJECT, test.testable=service]
  - kind: test
    evidence: executed
    count: 1
    tokens: [fn:equal, fn:filter, fn:getAll, fn:tds::project, lit=string, map.class=relational,
             map.main-table, map.pk=explicit, map.prop=column, nav.depth>=1, query.cols>=1,
             query.kind=tds-project, query.op=filter, rel.table.pk=single, route=relational:H2,
             test.assert=EqualToJson, test.asserts>=1, test.data=reference,
             test.data.ref.shared>=2, test.data.rows>=2, test.data.target=relational-csv,
             test.expected.format=json, test.expected.records>=1,
             test.serialization=PURE_TDSOBJECT, test.testable=service]
  - kind: service
    evidence: executed
    count: 2
    tokens: [svc.exec=single, svc.runtime=ref, svc.test-suites>=1]
  - kind: connection
    evidence: executed
    count: 1
    tokens: [conn.auth=DefaultH2, conn.spec=LocalH2, conn.type=relational:H2]
  # ... query, runtime, data, class-mapping, and table records elided
```

The profile surfaces three things the current descriptor cannot:

1. The primary key is exercised here, but only a catalog reader of the whole corpus would
   know that. The descriptor's authors correctly left it untagged, because it is not the
   subject.
2. The derived property rides along in the dependency and is never tested.
3. Both tests assert small, non-empty results against two rows of relational CSV. A Studio
   project whose service tests use multi-table CSV with nulls, or `EqualToRelation`, would
   show up as specific uncovered tuples, such as
   `(test.data.nulls, test.testable=service)`.

---

## 13. Phasing

| Phase | Scope | Unlocks |
|---|---|---|
| **0: Agree** | Reconcile with the downstream model. Fix the token conventions, site kinds, evidence levels, and document layout. Decide on committed profiles and module placement (§15). | One vocabulary for both sides. |
| **1: Skeleton, shapes, and structure** | `legend-engine-emit-profile`, the SPI, and the shape tier. Structural records for core language, relational, service, and tests (structure, data, and assertions, without reach). The Profile task with update mode. Commit profiles for all current descriptors. | Novelty detection, scale comparison, and a test inventory. The downstream scan can begin. |
| **2: Expressions** | Compiled-lambda profiler and function resolution. Behavioral records for constraints, derived properties, queries, M2M transforms, relation functions, and test queries. | Function-in-context coverage, and constraint and query complexity. |
| **3: Reach, evidence, and tuples** | Reach analysis, evidence levels, t-way tuples, and the comparator. Taxonomy files with detectors, claim validation, and derived `stores` and `complexity`. The exclusions registry and report views. | Interaction coverage, and enforcement of the honesty rule. |
| **4: Remaining areas** | Persistence, external formats and bindings, generation, function activators, data spaces, data quality, the service store, flat data, and other stores. | Full-breadth scans. |
| **5: Dynamic evidence (optional)** | Plan-shape tokens (execution node types and result types) from the plans EMIT already generates, written as build output only and never committed. | Confirms reach against what the router actually chose. |

---

## 14. Alternatives Considered

- **Hand-authored enrichment** (counts, function lists, and test details written into
  `*.emit.yaml`). Rejected. It drifts (§2.5), it costs every author, and the Studio side
  cannot be hand-authored at all, so the two sides would never be comparable.
- **Model-level aggregate metrics only.** Aggregates capture scale but not interaction.
  They are retained as `summary`, but they play no part in comparisons.
- **Whole-site signature matching.** Rejected in §7.3: exact matching is too strict, and
  superset dominance is unsound.
- **Build-output-only profiles.** These are never stale, but they are not reviewable in PRs,
  and the report and scanner would need each module's classpath to read them. The proposal
  keeps build output only for the dynamic tier (Phase 5), whose content legitimately varies
  with the engine build.
- **Adopting the MFT feature matrix as is.** MFT's matrix is a hand-declared, four-dimension
  vocabulary attached to stereotyped test functions. The profile generalizes it (derived and
  many-dimensional), and §6.2 aligns the query and mapping tokens so that the two can be
  compared later.

---

## 15. Open Questions

1. **The downstream model.** Can the richer Studio-project model be shared, so that Phase 0
   can reconcile it with this vocabulary? Ideally the scanner adopts this extractor, and
   anything the downstream model captures that this one misses is added here.
2. **Committed or build-output profiles.** This proposal recommends committed and verified
   (§10.3). The cost is one regeneration command per vocabulary change.
3. **Module placement.** Should there be a dedicated profiler module per extension area
   (recommended), or a main source set in the existing `-emit` modules?
4. **Tuple order.** Are t = 2 everywhere and t = 3 for `query` and `test` sites the right
   defaults for the scan's volume?
5. **Anonymity.** Is "no names, literals, or values" sufficient for storing Studio profiles,
   or are further restrictions needed (on database types or content types, for example)?
   Should committed EMIT profiles carry `origins`?
6. **`stores` and `complexity`.** Should they be removed after the transition, or kept
   forever as derived-and-checked fields?
7. **Reach implementation.** Should reach call the Pure `scanProperties` lineage machinery,
   or should it be a Java port over the compiled graph? Reusing the Pure code is less work;
   a Java port avoids coupling the profile to lineage's evolution.
