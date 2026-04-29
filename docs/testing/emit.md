# Engine Model Integration Test (EMIT) — A Legend Engine Test Harness

## 1. Motivation

Today, the canonical way to validate a Legend model end-to-end — parsing, compiling, running
generations, executing tests, and generating execution plans — is through a
**legend-sdlc project build**. This requires a full Maven project structure, SDLC Maven plugins
(`legend-sdlc-generation-model-maven-plugin`, `legend-sdlc-generation-file-maven-plugin`,
`legend-sdlc-test-maven-plugin`, `legend-sdlc-generation-service-execution-maven-plugin`), and
the associated project configuration (GAV coordinates, dependency management, etc.).

There is a need for a **lightweight alternative** that lives entirely inside `legend-engine`.
Given a set of `.pure` files (written in Legend grammar — i.e., the grammar used in Legend Studio,
not the M3/Pure grammar), the harness should:

1. **Parse** the `.pure` files into `PureModelContextData`.
2. **Compile** the `PureModelContextData` into a `PureModel`.
3. **Run model generations** (via `ModelGenerationExtension` SPI).
4. **Run artifact/file generations** (via `GenerationExtension` and `ArtifactGenerationExtension` SPIs).
5. **Run all tests** defined in the model (via the `Testable` infrastructure / `TestableRunner`).
6. **Generate execution plans** for any `Service` elements in the model (via `ServicePlanGenerator`).

Each phase must succeed for the overall test to pass, and failures in any phase should produce
clear, actionable diagnostics.

### Secondary Goal: A Searchable Example Catalog

Beyond validation, EMIT tests should serve as a **living repository of examples** showing how
different Legend features can be used — both individually and in combination. Over time, this
collection should become the canonical reference for "how do I do X with Y?" questions, covering
simple patterns (a class with a constraint) through complex compositions (a service with a
relational mapping, connection, and test suite using shared test data).

To support this, the EMIT framework should from the start:
- Enforce a **structured directory layout** with machine-readable metadata per model.
- Define a **tagging taxonomy** for features, stores, and complexity levels.
- Build a **catalog index** that can be queried programmatically. 

More user-friendly ways of searching or browsing the catalog are future work, but the metadata foundations make them straightforward to add later.

---

## 2. Naming

The name should make clear that these tests operate on **Legend Engine models** — i.e., models
written in Legend grammar (the grammar used in Legend Studio and the Engine protocol layer),
**not** Legend Pure models (M3/Pure grammar used internally by the platform). This distinction
matters because the two grammars are different, and the test harness specifically exercises the
Engine-level pipeline: Legend grammar parsing → protocol (`PureModelContextData`) → compilation
(`PureModel`) → generation → testing → plan generation.

The name also needs to include "Model" to avoid confusion with the build of the Legend Engine
project itself.

The chosen name is **Engine Model Integration Test (EMIT)**:
- **Engine** — signals Legend Engine grammar, not Pure grammar
- **Model** — makes clear we are testing a *model*, not the engine project
- **Integration** — conveys end-to-end pipeline testing (parse → compile → generate → test → plan)
- **EMIT** — the acronym is a real English word, making it memorable and distinct

This fits naturally alongside the existing harnesses:
- **PCT** tests *platform functions* across target runtimes
- **MFT** tests *mapping features* against store adaptors
- **EMIT** tests *Engine models* end-to-end through the full build pipeline

### Alternative Names

| Name | Abbreviation | Notes |
|---|---|---|
| **Engine Model Build Test** | **EMBT** | "Build" mirrors SDLC project build terminology and makes the pipeline analogy very explicit. Less concise than EMIT; the acronym is not a common word. |
| **Legend Model Integration Test** | **LMIT** | Uses "Legend" rather than "Engine" — broader brand alignment, but slightly less precise since Pure is also part of the Legend ecosystem. |
| **Engine Model Lifecycle Test** | **EMLT** | "Lifecycle" accurately captures the full parse → plan pipeline. Less familiar as a testing term than "Integration". |
| **Engine Model Pipeline Test** | **EMPT** | "Pipeline" is technically accurate for the sequential phase structure, but the acronym carries no mnemonic value. |

For the remainder of this document, we use the name **EMIT**.

---

## 3. Comparison with Existing Test Harnesses

### 3.1 PCT (Pure Compatibility Testing)

| Aspect | PCT | EMIT |
|---|---|---|
| **What is tested** | Individual Pure platform functions (e.g., `between`, `timeBucket`) | An entire Legend model, end-to-end |
| **Input** | Pure function definitions, already part of the compiled core | `.pure` files in Legend grammar (user-authored models) |
| **Scope** | Functional correctness of a single function across target runtimes (databases) | Full build pipeline: parse → compile → generate → test → plan. |
| **Target runtimes** | Executes against multiple database adapters (DuckDB, Snowflake, etc.) | Engine-only; no external database targets required (though model tests may use them) |
| **Test discovery** | Tests tagged with `@PCT` stereotypes in Pure code | Tests discovered from `Testable` elements (services, mappings, functions) in the input model |
| **JUnit integration** | Custom `TestSuite` builders (`PureTestBuilderCompiled`) | JUnit 5 integration via a `@TestFactory` yielding granular dynamic tasks |
| **Location** | `legend-engine-core/legend-engine-core-pure` and per-database PCT modules | New module: `legend-engine-core/legend-engine-core-emit` |

### 3.2 MFT (Mapping Feature Testing)

| Aspect | MFT | EMIT |
|---|---|---|
| **What is tested** | Mapping features (e.g., union, filter, groupBy) against store targets | An entire Legend model build pipeline |
| **Input** | Pure functions annotated with `@MFT{testCollection}` stereotypes | `.pure` files in Legend grammar |
| **Scope** | Validates that specific model constructs work correctly against a given store runtime | Validates the full lifecycle: parsing, compilation, generation, testing, plan generation |
| **Evaluator/Adaptor** | Uses evaluator and adaptor functions (e.g., relational adaptor, M2M evaluator) | N/A — runs against the engine directly; store-specific behavior is tested via model-embedded tests |
| **Test structure** | Test collections organized by Pure package hierarchy | One test per model (or per `.pure` file set), with sub-results per phase |
| **Compiled mode only** | Yes (uses `CompiledExecutionSupport`) | Uses the standard `PureModel` compilation path (protocol → PureModel), same as Studio/SDLC |

### 3.3 Legend SDLC Project Build

| Aspect | SDLC Build | EMIT |
|---|---|---|
| **What is tested** | Effectively the same pipeline: compile, generate, test, plan | Same pipeline |
| **Input** | A full Maven project with entity JSON files, `pom.xml`, and SDLC project structure | One or more `.pure` files — no project infrastructure required |
| **Execution mechanism** | Maven plugins (`legend-sdlc-generation-*-maven-plugin`) | Direct Java API calls in `legend-engine` |
| **Dependency** | Requires `legend-sdlc` | No `legend-sdlc` dependency; self-contained in `legend-engine` |
| **Use case** | CI/CD validation of production projects | Rapid testing of model snippets, regression testing, engine development |
| **Where it runs** | Maven build (`mvn install`) | JUnit test execution |

### Summary

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        Legend Test Harnesses                             │
├────────────┬─────────────────────────────────────────────────────────────┤
│    PCT     │ Tests individual Pure platform functions across target      │
│            │ runtimes (databases). Focus: function-level correctness.    │
├────────────┼─────────────────────────────────────────────────────────────┤
│    MFT     │ Tests mapping features (M2M, relational, etc.) against      │
│            │ store adaptors. Focus: store-target feature correctness.    │
├────────────┼─────────────────────────────────────────────────────────────┤
│  EMIT (new) │ Tests an entire model through the full build pipeline      │
│            │ (parse → compile → generate → test → plan).                 │
│            │ Focus: model-level end-to-end correctness.                  │
├────────────┼─────────────────────────────────────────────────────────────┤
│ SDLC Build │ Same pipeline as EMIT, but via Maven plugins and full       │
│            │ project structure. Focus: production project validation.    │
└────────────┴─────────────────────────────────────────────────────────────┘
```

---

## 4. Architecture

### 4.1 High-Level Pipeline

```
                    ┌─────────────────────────────────────┐
                    │       *.emit.yaml (input)           │
                    │   (descriptor + .pure source set)   │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 0: INITIALIZATION            │
                    │  Load descriptor, resolve files,    │
                    │  validate clashes, segment scope    │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 1: PARSE                     │
                    │  PureGrammarParser.parseModel()     │
                    │  → PureModelContextData             │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 2: COMPILE                   │
                    │  PureModel(pmcd, ...)               │
                    │  → PureModel                        │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 3: MODEL GENERATION          │
                    │  ModelGenerationExtension SPI       │
                    │  (if GenerationSpecification exists)│
                    │  → PureModelContextData (generated) │
                    │  Re-compile with generated model    │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 4: FILE GENERATION           │
                    │  a) GenerationExtension SPI         │
                    │     (FileGenerationSpecification)   │
                    │  b) ArtifactGenerationExtension SPI │
                    │     (per-element artifact gen)      │
                    │  → generated files                  │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 5: TEST EXECUTION            │
                    │  - TestableRunner.doTests()         │
                    │  - Legacy MappingTestRunner         │
                    │  - Legacy ServiceTestRunner         │
                    │  → Test results                     │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │  Phase 6: PLAN GENERATION           │
                    │  For each Service:                  │
                    │  ServicePlanGenerator               │
                    │    .generateServiceExecutionPlan()  │
                    │  → ExecutionPlan                    │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │         RESULT / REPORT             │
                    │  Per-phase pass/fail + diagnostics  │
                    └─────────────────────────────────────┘
```

### 4.2 Module Layout

#### Framework Module

The EMIT framework (runner, result models, catalog infrastructure) lives in a single core module:

```
legend-engine-core/
  legend-engine-core-emit/                ← NEW MODULE (framework)
    pom.xml
    src/
      main/java/.../emit/
        EMITRunner.java                   ← Core orchestrator
        EMITResult.java                   ← Result model
        EMITPhase.java                    ← Phase enum
        EMITPhaseResult.java              ← Per-phase result
        EMITModelLoader.java              ← .pure file loading utility
        catalog/
          EMITModelDescriptor.java        ← Parsed emit.yaml metadata
          EMITCatalogIndex.java           ← In-memory catalog & query API
          EMITCatalogBuilder.java         ← Scans models, builds index
      test/java/.../emit/
        TestEMITRunner.java               ← Self-test: verifies the framework itself
        TestEMITCatalog.java              ← Tests for catalog indexing
      test/resources/
        emit-models/                      ← Bootstrap models for self-testing
          basic/
            class-simple.emit.yaml        ← Test descriptor
            class-simple/                 ← Source files referenced by YAML
              model.pure
```

The module sits directly under `legend-engine-core/` (as a sibling of `legend-engine-core-testable`,
`legend-engine-core-pure`, etc.) because EMIT spans the full engine pipeline — parsing, compilation,
generation, test execution, and plan generation — rather than being scoped to the `Testable`
metamodel concept alone.

#### Distributed Test Locations

Actual EMIT tests are **spread throughout the codebase**, living in the modules that own the
features being tested. Each module that contributes EMIT tests adds a test-scoped dependency
on `legend-engine-core-emit` and places its models under `src/test/resources/emit-models/`:

```
legend-engine-xts-relationalStore/
  legend-engine-xt-relationalStore-*-test/  ← Existing or new test module
    src/test/resources/
      emit-models/
        relational-simple.emit.yaml         ← Test descriptor configures sources
        relational-simple/                  ← Model sources (paths resolved relative to YAML)
          model/types.pure
          store/db.pure
          mapping/mapping.pure
          connection/connection.pure
          runtime/runtime.pure
        relational-joins.emit.yaml
        relational-joins/
          ...
```

The same pattern applies to other extension modules (`legend-engine-xts-service`, `legend-engine-xts-generation`, etc.) — each owns the EMIT models for its feature area, with one `*.emit.yaml` per test and a sibling source-root directory.

The `EMITRunner` automatically discovers all `*.emit.yaml` files. The YAML file explicitly configures which directories and files comprise the test.

This distribution model has several advantages:
- **Ownership**: Tests live near the code they exercise, so feature owners maintain them.
- **Dependencies**: Each test module has the right classpath for its feature (e.g., a relational
  EMIT test module naturally has relational store dependencies on its classpath).
- **Parallelism**: Tests run as part of each module's build, not as a single bottleneck module.
- **Catalog aggregation**: The `EMITCatalogBuilder` can scan across multiple classpath roots
  to assemble a unified catalog from all distributed models.

### 4.3 Core API

```java
public class EMITRunner
{
  /**
   * Run the full build pipeline against an already-loaded descriptor.
   * The descriptor carries the resolved primary model files and the
   * dependency files (already segmented by scope).
   */
  public EMITResult run(EMITModelDescriptor descriptor);

  /**
   * Convenience: load the descriptor at the given *.emit.yaml path
   * (resolving its model and dependencies) and run the full pipeline.
   */
  public EMITResult runFromYaml(Path emitYaml);
}
```

```java
public class EMITResult
{
    public List<EMITPhaseResult> phaseResults;

    public boolean isSuccess();          // true iff all phases passed
    public EMITPhaseResult getPhase(EMITPhase phase);
}
```

```java
public enum EMITPhase
{
    INITIALIZATION,    // descriptor load, file resolution, clash validation
    PARSE,
    COMPILE,
    MODEL_GENERATION,
    FILE_GENERATION,
    TEST_EXECUTION,
    PLAN_GENERATION
}
```

```java
public class EMITPhaseResult
{
    public EMITPhase phase;
    public boolean success;
    public long durationMs;
    public String errorMessage;         // null if success
    public Exception exception;         // null if success
    // Phase-specific output data is also available (e.g., PureModelContextData,
    // PureModel, GenerationOutput, test results, ExecutionPlan objects).
}
```

---

## 5. Phase Details

### 5.1 Phase 0: Initialization

- Parse the `*.emit.yaml` file to read the explicit source configuration, which includes `model` (the primary model files) and optionally `dependencies`.
- `model`: Specified using a `root` directory and a list of `files`, which are resolved relative to the root.
- `dependencies`: Each dependency can be specified in one of two ways:
  - An `*.emit.yaml` file path (using `source`) with an optional `excludes` list supporting `*` and `**` wildcards.
  - A `root` directory and a list of `files`, exactly like the `model` specification.
- **Scope Segmentation**: Maintain a distinction between files loaded from the primary `model` and those loaded via `dependencies`. Dependencies are not in scope for generations, tests, etc.
- **Clash Validation**: Assert that no two files resolve to the same virtual path.
- **Success criteria**: Descriptor is well-formed and all source files resolve uniquely.
- **Failure mode**: An `EMITPhaseResult` for `INITIALIZATION` is produced with the error; subsequent phases are skipped.

### 5.2 Phase 1: Parse

- Read the content of each discovered file (from both the primary model and dependencies).
- Call `PureGrammarParser.newInstance().parseModel(content)` for each file.
- Combine the resulting `PureModelContextData` objects using the builder's `addPureModelContextData()`.
- **Success criteria**: No grammar parse exceptions.
- **Failure mode**: `EngineException` with source location information.

### 5.3 Phase 2: Compile

- Construct a `PureModel` from the merged `PureModelContextData`.
- Use `Compiler.compile(pmcd, DeploymentMode.TEST, ...)` or the standard `new PureModel(pmcd, ...)` path.
- **Success criteria**: No compilation errors.
- **Failure mode**: `EngineException` or `CompilationException` with details.

### 5.4 Phase 3: Model Generation

- Discover `GenerationSpecification` elements in the `PureModelContextData`.
- If present, use the `ModelGenerationExtension` SPI (loaded via `ServiceLoader`) to produce
  additional `PureModelContextData`.
- Merge the generated data with the original data and re-compile to produce an enriched
  `PureModel`.
- This is functionally equivalent to what the SDLC model generation Maven plugin does.
- **Success criteria**: Generation completes without exceptions; re-compilation succeeds.
- **Skipped if**: No `GenerationSpecification` elements exist in the model.

### 5.5 Phase 4: File Generation

This phase covers both types of file generation:

#### 5.5a Specification-Driven File Generations

- Discover the `GenerationSpecification` element and iterate over its `fileGenerations` list.
- For each `FileGenerationSpecification` referenced, find the corresponding `GenerationExtension` SPI (loaded via `ServiceLoader`) matching the specification's type.
- Execute the generation extension to produce a list of `GenerationOutput` (e.g., Avro schemas, JSON Schemas, Protobuf definitions).
- **Skipped if**: No `GenerationSpecification` exists, or its `fileGenerations` list is empty.

#### 5.5b Element-Driven Artifact Generations

- Load all registered `ArtifactGenerationExtension` SPIs via `ServiceLoader`.
- For each packageable element, iterate over extensions and call `canGenerate(element)` to check applicability.
- For applicable elements, call `extension.generate(element, pureModel, pmcd, clientVersion)` to produce a list of `Artifact` objects.
- **Skipped if**: No elements are candidates for any registered artifact generation extension.

**Success criteria**: Both sub-phases complete without exceptions.

### 5.6 Phase 5: Test Execution

- **Run Testable tests**: Find all `Testable` elements in the compiled `PureModel` (e.g., services, mappings, functions with test suites).
- **Run Legacy Mapping tests**: Find `Mapping` elements with legacy `MappingTest` / `MappingTestSuite` elements.
- **Run Legacy Service tests**: Find `Service` elements with legacy `ServiceTest` elements.
- **Dependency Exclusion**: Only execute tests for elements defined in the primary `model`. Any test defined in an element loaded via `dependencies` MUST be ignored.
- Use `TestableRunner.doTests(...)`, the legacy `MappingTestRunner`, and the legacy `ServiceTestRunner` to execute the in-scope tests, producing their respective result objects.
- **Success criteria**: All in-scope tests (Testable and legacy) pass.
- **Failure mode**: Failed/error `TestResult`, `RichMappingTestResult`, or `RichServiceTestResult` entries.
- **Skipped if**: No test elements (Testable or legacy) exist in the primary model.

### 5.7 Phase 6: Plan Generation

- Find all `Service` elements in the `PureModelContextData`.
- For each service, call
  `ServicePlanGenerator.generateServiceExecutionPlan(service, context, pureModel, ...)`.
- This handles both `PureSingleExecution` (producing a `SingleExecutionPlan`) and
  `PureMultiExecution` (producing a `CompositeExecutionPlan`).
- Collect and report the generated `ExecutionPlan` objects.
- **Success criteria**: Plan generation completes without exceptions for all services.
- **Skipped if**: No `Service` elements exist.

---

## 6. Execution and Reporting

EMIT models can be executed both programmatically (standalone) and through JUnit.

### 6.1 Standalone Execution

The `EMITRunner` can be invoked directly to execute the full pipeline for a given model descriptor:

```java
EMITRunner runner = new EMITRunner();
EMITResult result = runner.run(descriptor);

if (!result.isSuccess())
{
    System.err.println(result.getSummary());
}
```

This is useful for scripting, CI pipelines, or any context where JUnit is not the execution harness.

### 6.2 JUnit Integration

To achieve granular pass/fail reporting without forcing developers to write individual tests by hand, EMIT uses JUnit 5's `@TestFactory` mechanism. The framework provides a builder that discovers models, parses and compiles them upfront, and flattens their downstream operations into discrete `DynamicTest` tasks.

To test EMIT models in a module, create a single test class:

```java
public class MyModuleEMITTestSuite
{
    @TestFactory
    Stream<DynamicTest> emit()
    {
        // Discovers models, performs initialization + parse + compile, and
        // returns a flattened stream of granular dynamic tests for generation,
        // testing, and plan creation.
        return EMITTestSuiteBuilder.buildTasks("emit-models/");
    }
}
```

When JUnit invokes the factory method, `EMITTestSuiteBuilder` scans for `*.emit.yaml` files. For each model, it performs Phase 0 (Initialization), Phase 1 (Parse), and Phase 2 (Compile). By inspecting the compiled model, it identifies every model-generation specification, every file-generation specification, every artifact-generation candidate, every test, and every service.

It then yields one `DynamicTest` per granular operation, with descriptive names:

- `[service-simple] Initialization (Init, Parse & Compile)`
- `[service-simple] Model Generation: demo::MyModelGenSpec`
- `[service-simple] File Generation: demo::MyAvroGenerationSpec`
- `[service-simple] Artifact Generation: demo::PersonService (ServiceArtifactExtension)`
- `[service-simple] Test: demo::PersonService / testSuite_1 / test_1`
- `[service-simple] Test: demo::PersonService / testSuite_1 / test_2`
- `[service-simple] Plan: demo::PersonService`

Because each `DynamicTest` is a distinct JUnit test, IDEs and build servers (like Maven Surefire) report granular pass/fail statuses, durations, and diffs natively.

To optimize performance, the `PureModel` compiled during discovery is cached and shared among all tasks belonging to the same model. If a model fails initialization, parse, or compile during discovery, `EMITTestSuiteBuilder` yields a single `Initialization` task that is guaranteed to fail upon execution, skipping discovery of downstream tasks.

### 6.3 Result Model and Reporting

Each phase captures:
- **Success/failure status**
- **Duration** (milliseconds)
- **Exception** with full stack trace (on failure)
- **Error message** with source location (for parse/compile failures)
- **Phase-specific artifacts** (on success)

The `EMITResult.getSummary()` method produces a human-readable report:

```
EMIT Result: FAILED
  ✓ INITIALIZATION  (8ms)     — 4 model files, 1 dependency file
  ✓ PARSE           (42ms)    — 3 files, 12 elements
  ✓ COMPILE         (318ms)   — PureModel built successfully
  ✓ MODEL_GENERATION(15ms)    — skipped (no GenerationSpecification)
  ✓ FILE_GENERATION (87ms)    — 3 file generations, 4 artifact extensions
  ✗ TEST_EXECUTION  (203ms)   — 1 of 3 tests failed
      FAIL: demo::PersonService / testSuite_1 / test_1 / assert_1
            Expected: [] Actual: [{"firstName":"John","lastName":"Doe"}]
  — PLAN_GENERATION           — not run (prior phase failed)
```

---

## 7. Example Catalog Design

The EMIT model collection doubles as a **searchable catalog of feature examples**. This section
defines the metadata conventions and tooling foundations that make this possible.

### 7.1 Model Metadata (`*.emit.yaml`)

Every EMIT test is defined by a `*.emit.yaml` file with structured metadata and source configurations:

```yaml
# service-relational-with-generation.emit.yaml
name: service-relational-with-generation
title: "Relational Service with File Generation"
description: |
  Demonstrates a service backed by a relational mapping with a
  Relational-to-H2 connection, including test data, test suites,
  and an Avro file generation specification.

# Explicit source configuration. The primary model is specified with a root
# directory and an explicit list of files relative to that root.
# Dependencies can be specified either as another emit.yaml file with exclusions
# or as a root and list of files similar to the primary model.
modelSources:
  model:
    root: emit-models/complex/service-relational-with-generation
    files:
      - model/types.pure
      - store/db.pure
      - mapping/mapping.pure
      - service/myService.pure
  dependencies:
    - root: emit-models/shared-types
      files:
        - model/common.pure
    - source: emit-models/core-api.emit.yaml
      excludes:
        - emit-models/core-api/**/*_experimental.pure

# Features exercised by this model.
# Uses a controlled taxonomy (see §7.2).
features:
  - class
  - association
  - relational-mapping
  - relational-store
  - relational-connection
  - service
  - service-test
  - file-generation

# Store types involved.
stores:
  - relational

# Complexity level: basic, intermediate, advanced
complexity: advanced

# Optional: free-form tags for additional discoverability.
tags:
  - avro
  - h2
  - test-data
```

### 7.2 Feature Taxonomy

The `features` field uses values from a controlled vocabulary. The taxonomy is extensible;
new features can be added as the catalog grows. The initial set:

| Category | Feature Tags |
|---|---|
| **Types** | `class`, `enumeration`, `association`, `profile`, `measure`, `function` |
| **Constraints** | `constraint`, `derived-property`, `qualified-property` |
| **Mapping** | `mapping`, `m2m-mapping`, `relational-mapping`, `enumeration-mapping`, `aggregation-aware-mapping`, `operation-mapping` |
| **Store** | `relational-store`, `service-store`, `flat-data-store` |
| **Connection** | `relational-connection`, `model-connection`, `service-store-connection` |
| **Runtime** | `runtime`, `embedded-runtime` |
| **Service** | `service`, `service-test`, `multi-execution-service`, `post-validation` |
| **Generation** | `file-generation`, `model-generation`, `generation-specification` |
| **Data** | `data-element`, `test-data`, `shared-test-data` |
| **External Format** | `external-format`, `binding`, `schema-set` |
| **Function Activator** | `hosted-service`, `snowflake-app`, `bigquery-function` |

### 7.3 Directory Organization

Models are organized hierarchically by primary concern:

```
emit-models/
  basic/                    ← Simple, single-concept examples
    class-simple/
    class-with-constraint/
    enumeration/
    function-simple/
  mapping/                  ← Mapping-focused examples
    m2m-basic/
    m2m-chained/
    relational-simple/
    relational-joins/
    aggregation-aware/
  service/                  ← Service-focused examples
    service-simple/
    service-with-tests/
    multi-execution/
  generation/               ← Generation-focused examples
    avro-generation/
    json-schema-generation/
  complex/                  ← Multi-feature compositions
    service-relational-with-generation/
    m2m-with-external-format/
```

This hierarchy aids browsing, but the primary discovery mechanism is the metadata index,
not the directory tree.

### 7.4 Catalog Index

The `EMITCatalogBuilder` scans classpath roots for `*.emit.yaml` files and builds an in-memory `EMITCatalogIndex`. The index supports querying models by feature, store type, complexity, and free-text search over titles, descriptions, and tags. The `EMITModelDescriptor` captures all fields from the `emit.yaml` file plus the resolved file paths.

The index is designed so that a future search tool can simply serialize it to JSON and provide a search interface over it.

---

## 8. Sample `.pure` Model Input

A minimal example model for EMIT testing, with its accompanying `emit.yaml`:

**`service-simple.emit.yaml`**:
```yaml
name: service-simple
title: "Simple Service with M2M Mapping"
description: |
  A basic service using a model-to-model mapping. Demonstrates
  class definition, empty mapping, service definition with a
  query and test suite.

modelSources:
  model:
    root: emit-models/basic/service-simple
    files:
      - model/model.pure
  dependencies:
    - source: emit-models/basic/base-types.emit.yaml

features:
  - class
  - derived-property
  - mapping
  - service
  - service-test
complexity: basic
tags:
  - m2m
  - getting-started
```

**`model/model.pure`**:
```pure
###Pure
Class demo::Person
{
  firstName: String[1];
  lastName: String[1];
  fullName() { $this.firstName + ' ' + $this.lastName }: String[1];
}

###Mapping
Mapping demo::PersonMapping
(
)

###Service
Service demo::PersonService
{
  pattern: '/api/person';
  documentation: 'A simple person service';
  autoActivateUpdates: true;
  execution: Single
  {
    query: |demo::Person.all()->project([x|$x.firstName, x|$x.lastName], ['First Name', 'Last Name']);
    mapping: demo::PersonMapping;
  }
  testSuites:
  [
    testSuite_1:
    {
      tests:
      [
        test_1:
        {
          asserts:
          [
            assert_1:
              EqualToJson
              #{
                expected:
                  ExternalFormat
                  #{
                    contentType: 'application/json';
                    data: '[]';
                  }#;
              }#
          ]
        }
      ]
    }
  ]
}
```

---

## 9. Dependencies

The EMIT module depends only on existing `legend-engine` modules — there is no dependency on `legend-sdlc`. Its dependencies fall into a small set of categories, each contributing one stage of the pipeline:

- **Protocol & grammar** — `PureModelContextData`, version registry, and the `PureGrammarParser` that turns `.pure` source into protocol POJOs.
- **Compiler** — `PureModel` construction from a `PureModelContextData`.
- **Generation SPIs** — `ModelGenerationExtension`, `GenerationExtension`, and `ArtifactGenerationExtension`, plus their shared base modules.
- **Test runners** — `TestableRunner` for the modern `Testable` infrastructure, plus the legacy `MappingTestRunner` and `ServiceTestRunner` for backward compatibility.
- **Plan generation** — `ServicePlanGenerator` (for `Service` elements) and the underlying `PlanGenerator`.
- **Service DSL** — protocol/compiler artifacts for the `Service` element type, since `Service` is special-cased by Phase 6.

Concrete artifact coordinates are kept in `pom.xml`; listing them here would drift.

---

## 10. Future Extensions

- **Store implementation variation**: Run a model against alternative implementations of
  its stores (e.g., a relational model against DuckDB, Postgres, Snowflake) without
  duplicating the model. The available variations should be **discovered from the model
  and the classpath**, not declared in `emit.yaml` — keeping the test descriptor focused
  on what the model is, and letting an external tool decide which variations to actually
  run.
- **Catalog search tool**: A web UI or CLI tool for searching and browsing the catalog index
  (e.g., `emit search --feature relational-mapping --complexity basic`).
- **Auto-derived metadata**: Introspect `PureModelContextData` to supplement `emit.yaml` tags.
- **Catalog completeness CI check**: Validate that every model has `emit.yaml`, required fields
  are present, and feature tags come from the controlled vocabulary.
- **Model diff testing**: Compare EMIT results between two model versions.
- **Performance benchmarking**: Track phase durations over time.
- **Model coverage**: Track which model elements are exercised by tests.
- **Feature coverage matrix**: Auto-generate a matrix showing which features are covered by
  examples and which lack coverage, helping guide the creation of new examples.

---

## 11. Implementation Roadmap

| Milestone | Description |
|---|---|
| 1 | Create the `legend-engine-core-emit` module. Implement the core data model (`EMITResult`, `EMITPhase`, `EMITPhaseResult`), `emit.yaml` parsing (`EMITModelDescriptor`), and file loading (`EMITModelLoader`). |
| 2 | Implement `EMITRunner` with all six pipeline phases (Parse, Compile, Model Generation, Artifact Generation, Test Execution, Plan Generation). |
| 3 | Implement `EMITTestSuiteBuilder` for JUnit integration and granular task discovery. |
| 4 | Implement `EMITCatalogIndex` and `EMITCatalogBuilder`. Write an initial catalog of 5-10 example models with `emit.yaml` metadata. |
| 5 | *(Future)* Catalog web UI / CLI search tool. |
| 6 | *(Future)* Store implementation variation (external tool that discovers variations from the model and runs the pipeline against them). |
