# Engine Model Integration Test (EMIT) — A Legend Engine Test Harness

## 1. Motivation

Today, the canonical way to validate a Legend model end-to-end — parsing, compiling, running
generations, executing tests, and generating execution plans — is through an
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
- More user friendly ways of searching or browsing the catalog are future work, but the metadata foundations make them straightforward to add later.

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
| **Scope** | Functional correctness of a single function across target runtimes (databases) | Full build pipeline: parse → compile → generate → test → plan. Optionally varies store implementations (e.g., DuckDB vs Snowflake for relational stores). |
| **Target runtimes** | Executes against multiple database adapters (DuckDB, Snowflake, etc.) | Engine-only; no external database targets required (though model tests may use them) |
| **Test discovery** | Tests tagged with `@PCT` stereotypes in Pure code | Tests discovered from `Testable` elements (services, mappings, functions) in the input model |
| **JUnit integration** | Custom `TestSuite` builders (`PureTestBuilderCompiled`) | JUnit 4/5 integration via a custom runner or parameterized test |
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
                    │         .pure files (input)         │
                    │   (Legend grammar, user-authored)   │
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
                    │  → generated files                  |
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
legend-engine-xts-relational/
  legend-engine-xt-relational-*-test/     ← Existing or new test module
    src/test/resources/
      emit-models/
        relational-simple.emit.yaml       ← Test descriptor configures sources
        relational-simple/                ← Model sources (paths resolved relative to YAML)
          model/
            types.pure
          store/
            db.pure
          mapping/
            mapping.pure
          connection/
            connection.pure
          runtime/
            runtime.pure
        relational-joins.emit.yaml
        relational-joins/
          model/
            types.pure
            associations.pure
          store/
            db.pure
          mapping/
            mapping.pure
          ...

legend-engine-xts-service/
  legend-engine-xt-service-*-test/
    src/test/resources/
      emit-models/
        service-with-tests.emit.yaml
        service-with-tests/
          model/
            types.pure
          mapping/
            mapping.pure
          service/
            myService.pure
          store/
            db.pure
          connection/
            connection.pure
          runtime/
            runtime.pure
        multi-execution.emit.yaml
        multi-execution/
          ...

legend-engine-xts-generation/
  ...
    src/test/resources/
      emit-models/
        avro-generation.emit.yaml
        avro-generation/
          model/
            types.pure
          generation/
            genSpec.pure
```

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
   * Run the full build pipeline for the given .pure file content strings.
   * Each string is the content of one .pure file (Legend grammar).
   */
  public EMITResult run(List<String> pureFileContents);

  /**
   * Run the full build pipeline, loading .pure files from the given paths.
   */
  public EMITResult runFromFiles(List<Path> pureFilePaths);

  /**
   * Run the full build pipeline, loading all .pure files under the given directory.
   */
  public EMITResult runFromDirectory(Path directory);
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

### 5.0 Initialization (Pre-Pipeline)

- Parse the `*.emit.yaml` file to read the explicit source configuration, which includes `model` (the primary model files) and optionally `dependencies`.
- `model`: Specified using a `root` directory and a list of `files`, which are resolved relative to the root.
- `dependencies`: Each dependency can be specified in one of two ways:
  - An `*.emit.yaml` file path (using `source`) with an optional `excludes` list supporting `*` and `**` wildcards.
  - A `root` directory and a list of `files`, exactly like the `model` specification.
- **Scope Segmentation**: Maintain a distinction between files loaded from the primary `model` and those loaded via `dependencies`. Dependencies are not in scope for generations, tests, etc.
- **Clash Validation**: Assert that no two files resolve to the same virtual path. If a clash occurs, test initialization fails before any phases run.

### 5.1 Phase 1: Parse

- Read the content of each discovered file (from both the primary model and dependencies).
- Call `PureGrammarParser.newInstance().parseModel(content)` for each file.
- Combine the resulting `PureModelContextData` objects using the builder's `addPureModelContextData()`.
- **Success criteria**: No grammar parse exceptions.
- **Failure mode**: `EngineException` with source location information.

### 5.2 Phase 2: Compile

- Construct a `PureModel` from the merged `PureModelContextData`.
- Use `Compiler.compile(pmcd, DeploymentMode.TEST, ...)` or the standard `new PureModel(pmcd, ...)` path.
- **Success criteria**: No compilation errors.
- **Failure mode**: `EngineException` or `CompilationException` with details.

### 5.3 Phase 3: Model Generation

- Discover `GenerationSpecification` elements in the `PureModelContextData`.
- If present, use the `ModelGenerationExtension` SPI (loaded via `ServiceLoader`) to produce
  additional `PureModelContextData`.
- Merge the generated data with the original data and re-compile to produce an enriched
  `PureModel`.
- This is functionally equivalent to what the SDLC model generation Maven plugin does.
- **Success criteria**: Generation completes without exceptions; re-compilation succeeds.
- **Skipped if**: No `GenerationSpecification` elements exist in the model.

### 5.4 Phase 4: File Generation

This phase covers both types of file generation:

#### 5.4a Specification-Driven File Generations

- Discover the `GenerationSpecification` element and iterate over its `fileGenerations` list.
- For each `FileGenerationSpecification` referenced, find the corresponding `GenerationExtension` SPI (loaded via `ServiceLoader`) matching the specification's type.
- Execute the generation extension to produce a list of `GenerationOutput` (e.g., Avro schemas, JSON Schemas, Protobuf definitions).
- **Skipped if**: No `GenerationSpecification` exists, or its `fileGenerations` list is empty.

#### 5.4b Element-Driven Artifact Generations

- Load all registered `ArtifactGenerationExtension` SPIs via `ServiceLoader`.
- For each packageable element, iterate over extensions and call `canGenerate(element)` to check applicability.
- For applicable elements, call `extension.generate(element, pureModel, pmcd, clientVersion)` to produce a list of `Artifact` objects.
- **Skipped if**: No elements are candidates for any registered artifact generation extension.

**Success criteria**: Both sub-phases complete without exceptions.

### 5.5 Phase 5: Test Execution

- **Run Testable tests**: Find all `Testable` elements in the compiled `PureModel` (e.g., services, mappings, functions with test suites).
- **Run Legacy Mapping tests**: Find `Mapping` elements with legacy `MappingTest` / `MappingTestSuite` elements.
- **Run Legacy Service tests**: Find `Service` elements with legacy `ServiceTest` elements.
- **Dependency Exclusion**: Only execute tests for elements defined in the primary `model`. Any test defined in an element loaded via `dependencies` MUST be ignored.
- Use `TestableRunner.doTests(...)`, the legacy `MappingTestRunner`, and the legacy `ServiceTestRunner` to execute the in-scope tests, producing their respective result objects.
- **Success criteria**: All in-scope tests (Testable and legacy) pass.
- **Failure mode**: Failed/error `TestResult`, `RichMappingTestResult`, or `RichServiceTestResult` entries.
- **Skipped if**: No test elements (Testable or legacy) exist in the primary model.

### 5.6 Phase 6: Plan Generation

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

To achieve granular pass/fail reporting without forcing developers to write individual tests by hand, EMIT leverages JUnit 4's built-in `Parameterized` runner. The framework provides a builder that discovers models, parses them upfront, and flattens their operations into discrete executable tasks.

To test EMIT models in a module, create a single parameterized test class:

```java
@RunWith(Parameterized.class)
public class MyModuleEMITTestSuite
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> tasks()
    {
        // Discovers models, performs initial parse/compile, and returns a flattened 
        // list of granular EMITTask objects for generation, testing, and plan creation.
        return EMITTestSuiteBuilder.buildTasks("emit-models/");
    }

    private final String taskName;
    private final EMITTask task;

    public MyModuleEMITTestSuite(String taskName, EMITTask task) 
    {
        this.taskName = taskName;
        this.task = task;
    }

    @Test
    public void executeTask()
    {
        task.run();
    }
}
```

When JUnit invokes the `@Parameters` method, `EMITTestSuiteBuilder` scans for `*.emit.yaml` files. For each model, it performs Phase 1 (Parse) and Phase 2 (Compile). By inspecting the compiled model, it identifies every file generation specification, every test suite, and every service.

It then returns an array of granular tasks. The `{0}` parameter binds to the `taskName`, yielding highly descriptive individual JUnit test cases:

- `[service-simple] Initialization (Parse & Compile)`
- `[service-simple] Generation: MyAvroGenerationSpec`
- `[service-simple] Test: demo::PersonService / testSuite_1 / test_1`
- `[service-simple] Test: demo::PersonService / testSuite_1 / test_2`
- `[service-simple] Plan: demo::PersonService`

Because each task is a distinct JUnit parameter, IDEs and build servers (like Maven Surefire) will report granular pass/fail statuses, durations, and diffs natively.

To optimize performance, the `PureModel` compiled during the discovery phase is cached and shared among all tasks belonging to the same model. If a model fails to parse or compile during discovery, `EMITTestSuiteBuilder` simply yields a single `Initialization` task that is guaranteed to fail upon execution, skipping discovery of downstream tasks.

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

## 7. Store Implementation Variation

EMIT models that involve stores (relational, service store, etc.) should be testable against
multiple implementations of those stores. For example, a model with a relational mapping should
be runnable against DuckDB, Snowflake, DB2, and any other supported database — without
duplicating the model itself.

This is analogous to how PCT tests individual functions across database targets, but at the
model level. The key difference is that the **store type is fixed by the model** (e.g., a
relational store is always relational); only the **implementation** varies.

### 7.1 Concept

A store implementation variant specifies an alternative connection or runtime configuration
that replaces the default one defined in the model. For a relational model, this typically
means providing a different database connection (e.g., DuckDB instead of H2).

The EMIT framework treats the model's `.pure` files as the base. Each store variant provides
**overlay files** that replace or supplement specific source files (typically connection and
runtime definitions). The pipeline runs in full for each variant: parse, compile, generate,
test, and plan.

### 7.2 Configuration

Store variants are declared in the `emit.yaml` file using a `storeVariation` section:

```yaml
# relational-service.emit.yaml
name: relational-service
title: "Relational Service"

modelSources:
  model:
    root: emit-models/relational-service
    files:
      - model/types.pure
      - store/db.pure
      - mapping/mapping.pure
      - service/myService.pure
      - connection/connection.pure     # default: H2 connection
      - runtime/runtime.pure

# Store implementation variation.
# Each variant provides overlay files that replace files in the base model.
storeVariation:
  - name: duckdb
    overlays:
      - base: connection/connection.pure
        replacement: variants/duckdb/connection.pure
      - base: runtime/runtime.pure
        replacement: variants/duckdb/runtime.pure
  - name: snowflake
    overlays:
      - base: connection/connection.pure
        replacement: variants/snowflake/connection.pure
      - base: runtime/runtime.pure
        replacement: variants/snowflake/runtime.pure

features:
  - relational-mapping
  - service
stores:
  - relational
complexity: intermediate
```

Overlay file paths are resolved relative to the same `root` as the base model.

### 7.3 Execution Semantics

When `storeVariation` is present, the EMIT pipeline executes as follows:

1. **Base run**: The model is executed as-is with its default connection/runtime files.
   This run uses whatever store implementation the model defines by default (e.g., H2).
2. **Variant runs**: For each entry in `storeVariation`, the framework re-runs the full
   pipeline with the overlay files substituted. A variant overlay specifies a `base` file
   (from the model's `files` list) and a `replacement` file. During the parse phase, the
   replacement file's content is loaded instead of the base file.

Each variant run is independent — it produces its own `EMITResult` with per-phase
pass/fail status.

If a model has no `storeVariation` section, the pipeline runs exactly once with the base
model (preserving backward compatibility).

### 7.4 JUnit Integration

Store variants expand the JUnit parameterized test matrix. Each task name includes the
variant name as a qualifier:

```
[relational-service / base]     Initialization (Parse & Compile)
[relational-service / base]     Test: demo::PersonService / testSuite_1 / test_1
[relational-service / base]     Plan: demo::PersonService
[relational-service / duckdb]   Initialization (Parse & Compile)
[relational-service / duckdb]   Test: demo::PersonService / testSuite_1 / test_1
[relational-service / duckdb]   Plan: demo::PersonService
[relational-service / snowflake] Initialization (Parse & Compile)
[relational-service / snowflake] Test: demo::PersonService / testSuite_1 / test_1
[relational-service / snowflake] Plan: demo::PersonService
```

This gives granular visibility into which store implementations pass or fail for each
model, directly in the IDE test runner or CI output.

When no `storeVariation` is defined, the variant qualifier is omitted from task names
(the existing format is unchanged).

### 7.5 Module Placement

Variant overlay files live alongside the model, in a `variants/` subdirectory:

```
emit-models/
  relational-service/
    model/
      types.pure
    store/
      db.pure
    mapping/
      mapping.pure
    service/
      myService.pure
    connection/
      connection.pure          ← default (e.g., H2)
    runtime/
      runtime.pure
    variants/
      duckdb/
        connection.pure        ← DuckDB connection overlay
        runtime.pure
      snowflake/
        connection.pure        ← Snowflake connection overlay
        runtime.pure
```

Because each database implementation lives in a separate engine module (with its own
classpath), the store variant test naturally belongs in the module that provides that
implementation. A relational model's DuckDB variant would be tested in the DuckDB module,
which has DuckDB-specific dependencies on its classpath.

To support this, a module can reference a model defined elsewhere and supply its own
variant overlays. The `storeVariation` entries can be contributed by the module's own
`emit.yaml` (or by extending the base model's descriptor — details TBD in implementation).

---

## 8. Example Catalog Design

The EMIT model collection doubles as a **searchable catalog of feature examples**. This section
defines the metadata conventions and tooling foundations that make this possible.

### 8.1 Model Metadata (`*.emit.yaml`)

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

### 8.2 Feature Taxonomy

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

### 8.3 Directory Organization

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

### 8.4 Catalog Index

The `EMITCatalogBuilder` scans classpath roots for `*.emit.yaml` files and builds an in-memory `EMITCatalogIndex`. The index supports querying models by feature, store type, complexity, and free-text search over titles, descriptions, and tags. The `EMITModelDescriptor` captures all fields from the `emit.yaml` file plus the resolved file paths.

The index is designed so that a future search tool can simply serialize it to JSON and provide a search interface over it.

---

## 9. Sample `.pure` Model Input

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

## 10. Dependencies

The EMIT module will depend on existing `legend-engine` modules:

| Dependency | Purpose |
|---|---|
| `legend-engine-language-pure-grammar` | `PureGrammarParser` for parsing `.pure` files |
| `legend-engine-language-pure-compiler` | `PureModel` construction / compilation |
| `legend-engine-language-pure-dsl-generation` | `ModelGenerationExtension`, `ArtifactGenerationExtension` SPIs |
| `legend-engine-external-shared` | `GenerationExtension` SPI |
| `legend-engine-testable` | `TestableRunner` |
| `legend-engine-test-runner-mapping` | `MappingTestRunner` (legacy Mapping tests) |
| `legend-engine-test-runner-service` | `ServiceTestRunner` (legacy Service tests) |
| `legend-engine-executionPlan-generation` | `PlanGenerator` (used internally by `ServicePlanGenerator`) |
| `legend-engine-language-pure-dsl-service` | Service protocol model |
| `legend-engine-language-pure-dsl-service-generation` | `ServicePlanGenerator` |
| `legend-engine-protocol-pure` | `PureModelContextData`, `PureClientVersions` |

No dependency on `legend-sdlc` is required.

---

## 11. Future Extensions

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
- **Independent store instance variation**: When a model contains multiple store instances
  (e.g., two different relational databases), vary each store's implementation independently.
  This creates a cross-product matrix of store implementations. For example, a model with
  Store A (H2/DuckDB) and Store B (Postgres/Snowflake) would produce four variant runs.

---

## 12. Implementation Roadmap

| Milestone | Description |
|---|---|
| 1 | Create the `legend-engine-core-emit` module. Implement the core data model (`EMITResult`, `EMITPhase`, `EMITPhaseResult`), `emit.yaml` parsing (`EMITModelDescriptor`), and file loading (`EMITModelLoader`). |
| 2 | Implement `EMITRunner` with all six pipeline phases (Parse, Compile, Model Generation, Artifact Generation, Test Execution, Plan Generation). |
| 3 | Implement `EMITTestSuiteBuilder` for JUnit integration and granular task discovery. |
| 4 | Add store implementation variation support to `EMITRunner` and `EMITTestSuiteBuilder`. |
| 5 | Implement `EMITCatalogIndex` and `EMITCatalogBuilder`. Write an initial catalog of 5-10 example models with `emit.yaml` metadata. |
| 6 | *(Future)* Catalog web UI / CLI search tool. |
| 7 | *(Future)* Independent store instance variation (cross-product matrix). |
