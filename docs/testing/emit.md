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
- Full-text search and a web-based catalog browser are future work, but the metadata foundations
  make them straightforward to add later.

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
| **Scope** | Functional correctness of a single function across target runtimes (databases) | Full build pipeline: parse → compile → generate → test → plan |
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
                    │     (ArtifactGenerationFactory)     │
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
            class-simple/
              emit.yaml
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
        relational-simple/                ← Model root
          emit.yaml
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
        relational-joins/
          emit.yaml
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
        service-with-tests/               ← Model root
          emit.yaml
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
        multi-execution/
          emit.yaml
          ...

legend-engine-xts-generation/
  ...
    src/test/resources/
      emit-models/
        avro-generation/
          emit.yaml
          model/
            types.pure
          generation/
            genSpec.pure
```

The `EMITRunner.runFromDirectory(path)` method recursively discovers all `.pure` files under
the given root directory. The `emit.yaml` file must sit at the root of the model tree.

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

  // Phase-specific data (populated on success)
  public PureModelContextData parsedData;         // after PARSE
  public PureModel compiledModel;                 // after COMPILE
  public PureModelContextData generatedModelData;                       // after MODEL_GENERATION
  public Map<FileGenerationSpecification, List<GenerationOutput>> fileGenerationOutputs; // after FILE_GENERATION (file generations)
  public Map<ArtifactGenerationExtension, List<ArtifactGenerationResult>> artifactOutputs; // after FILE_GENERATION (artifact generations)
  public RunTestsResult testResults;                                     // after TEST_EXECUTION (Testable)
  public List<RichMappingTestResult> legacyMappingTestResults;           // after TEST_EXECUTION (Legacy Mapping)
  public List<RichServiceTestResult> legacyServiceTestResults;           // after TEST_EXECUTION (Legacy Service)
  public List<ExecutionPlan> plans;                                      // after PLAN_GENERATION
}
```

---

## 5. Phase Details

### 5.1 Phase 1: Parse

- Recursively discover all `.pure` files under the model root directory.
- Read each file's content.
- Call `PureGrammarParser.newInstance().parseModel(content)` for each file.
- Combine the resulting `PureModelContextData` objects using the builder's `addPureModelContextData()`.
- **Success criteria**: No parse exceptions.
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
- This mirrors what `legend-sdlc-generation-model-maven-plugin` /
  `ModelGenerationFactory.newFactory(genSpec, pmcd, pureModel).generate()` does.
- **Success criteria**: Generation completes without exceptions; re-compilation succeeds.
- **Skipped if**: No `GenerationSpecification` elements exist in the model.

### 5.4 Phase 4: File Generation

This phase covers both types of file generation, mirroring what `FileGenerationMojo` in
`legend-sdlc-generation-file-maven-plugin` does in a single Maven execution:

#### 5.4a Specification-Driven File Generations

- Discover the `GenerationSpecification` element and iterate over its `fileGenerations` list.
- For each `FileGenerationSpecification` referenced, use `FileGenerationFactory` to run the
  corresponding `FileGenerator`.
- Produces `GenerationOutput` per specification (e.g., Avro schemas, JSON Schemas, Protobuf
  definitions).
- This mirrors the first half of `FileGenerationMojo.execute()` (lines 135-158 in the SDLC plugin).
- **Skipped if**: No `GenerationSpecification` exists, or its `fileGenerations` list is empty.

#### 5.4b Element-Driven Artifact Generations

- Use `ArtifactGenerationFactory.newFactory(pureModel, pmcd, elements).generate()` to run all
  registered `ArtifactGenerationExtension` SPIs against every packageable element.
- Each extension declares which elements it `canGenerate(element)` for, and produces artifacts
  for matching elements.
- This mirrors the second half of `FileGenerationMojo.execute()` (lines 160-179 in the SDLC plugin).
- **Skipped if**: No elements are candidates for any registered artifact generation extension.

**Success criteria**: Both sub-phases complete without exceptions.

### 5.5 Phase 5: Test Execution

- **Run Testable tests**: Find all `Testable` elements in the compiled `PureModel` (e.g., services, mappings, functions
  with test suites). Use `TestableRunner.doTests(...)` to run them and collect `RunTestsResult`.
- **Run Legacy Mapping tests**: Find `Mapping` elements with legacy `MappingTest` / `MappingTestSuite` elements.
  Use the legacy `MappingTestRunner` to execute them, producing `RichMappingTestResult` objects.
- **Run Legacy Service tests**: Find `Service` elements with legacy `ServiceTest` elements.
  Use the legacy `ServiceTestRunner` to execute them, producing `RichServiceTestResult` objects.
- This phase mirrors `legend-sdlc-test-maven-plugin`.
- **Success criteria**: All tests (Testable and legacy) pass.
- **Failure mode**: Failed/error `TestResult`, `RichMappingTestResult`, or `RichServiceTestResult` entries.
- **Skipped if**: No test elements (Testable or legacy) exist.

### 5.6 Phase 6: Plan Generation

- Find all `Service` elements in the `PureModelContextData`.
- For each service, call
  `ServicePlanGenerator.generateServiceExecutionPlan(service, context, pureModel, ...)`.
- This handles both `PureSingleExecution` (producing a `SingleExecutionPlan`) and
  `PureMultiExecution` (producing a `CompositeExecutionPlan`).
- Collect and report the generated `ExecutionPlan` objects.
- This mirrors `legend-sdlc-generation-service-execution-maven-plugin`.
- **Success criteria**: Plan generation completes without exceptions for all services.
- **Skipped if**: No `Service` elements exist.

---

## 6. JUnit Integration

The primary way to use EMIT is via JUnit tests. A typical test looks like:

```java
public class MyModelEMITTest
{
  @Test
  public void testMyModel()
  {
    EMITRunner runner = new EMITRunner();
    EMITResult result = runner.runFromDirectory(
            Paths.get("src/test/resources/emit-models/my-model")
    );
    assertTrue(result.isSuccess(), result.getSummary());
  }
}
```

### 6.1 Parameterized Tests

For testing multiple models, a parameterized approach is recommended:

```java
@RunWith(Parameterized.class)
public class EMITParameterizedTest
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> models()
    {
        return Arrays.asList(
            new Object[]{"simple-class", "src/test/resources/emit-models/simple-class"},
            new Object[]{"service-model", "src/test/resources/emit-models/service-model"}
        );
    }

    private final String name;
    private final String path;

    public EMITParameterizedTest(String name, String path) { ... }

    @Test
    public void testModel()
    {
        EMITResult result = new EMITRunner().runFromDirectory(Paths.get(path));
        assertTrue(result.isSuccess(), result.getSummary());
    }
}
```

---

## 7. Example Catalog Design

The EMIT model collection doubles as a **searchable catalog of feature examples**. This section
defines the metadata conventions and tooling foundations that make this possible.

### 7.1 Model Metadata (`emit.yaml`)

Every EMIT model directory contains a `emit.yaml` file with structured metadata:

```yaml
# emit.yaml — metadata for a single EMIT example model
name: service-relational-with-generation
title: "Relational Service with File Generation"
description: |
  Demonstrates a service backed by a relational mapping with a
  Relational-to-H2 connection, including test data, test suites,
  and an Avro file generation specification.

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

### 7.4 Catalog Index (`EMITCatalogIndex`)

A Java API for building and querying the catalog at test time:

```java
public class EMITCatalogIndex
{
    /**
     * Scan a root directory, read all emit.yaml files, and build the index.
     */
    public static EMITCatalogIndex build(Path rootDirectory);

    /** All model descriptors in the catalog. */
    public List<EMITModelDescriptor> allModels();

    /** Find models that exercise a specific feature. */
    public List<EMITModelDescriptor> byFeature(String feature);

    /** Find models matching a set of features (AND). */
    public List<EMITModelDescriptor> byFeatures(Set<String> features);

    /** Find models by complexity level. */
    public List<EMITModelDescriptor> byComplexity(String complexity);

    /** Find models by store type. */
    public List<EMITModelDescriptor> byStore(String store);

    /** Find models matching a free-text query against title, description, and tags. */
    public List<EMITModelDescriptor> search(String query);
}
```

```java
public class EMITModelDescriptor
{
    public String name;               // directory name
    public String title;              // human-readable title
    public String description;        // multi-line description
    public List<String> features;     // feature tags
    public List<String> stores;       // store types
    public String complexity;         // basic / intermediate / advanced
    public List<String> tags;         // free-form tags
    public Path directory;            // path to model directory
    public List<Path> pureFiles;      // resolved .pure file paths
}
```

The `EMITCatalogIndex` is designed so that a future web UI or CLI search tool can simply
serialize the index to JSON and provide a search interface over it.

### 7.5 Auto-Derived Metadata (Future Enhancement)

In later stages, the framework can supplement the hand-written `emit.yaml` with auto-derived
metadata by introspecting the parsed `PureModelContextData`:

- **Element types present** (classes, enumerations, services, mappings, etc.)
- **Store types used** (from connection and runtime definitions)
- **Whether tests are defined** (presence of `testSuites` on services, functions, etc.)
- **Whether generations are defined** (presence of `GenerationSpecification`)

This lets the catalog stay accurate even if `emit.yaml` tags are incomplete.

### 7.6 Integration with EMITRunner

The `EMITRunner` is metadata-aware. When run from a directory containing `emit.yaml`, it reads
the descriptor and:

1. Reports the model's title and description in test output.
2. Includes feature tags in the `EMITResult` for downstream reporting.

```java
EMITRunner runner = new EMITRunner();
EMITResult result = runner.runFromDirectory(Paths.get("emit-models/service/service-with-tests"));

System.out.println(result.getDescriptor().title);   // "Service with Test Suites"
        System.out.println(result.getDescriptor().features); // [service, service-test, class]
```

---

## 8. Sample `.pure` Model Input

A minimal example model for EMIT testing, with its accompanying `emit.yaml`:

**`emit.yaml`**:
```yaml
name: service-simple
title: "Simple Service with M2M Mapping"
description: |
  A basic service using a model-to-model mapping. Demonstrates
  class definition, empty mapping, service definition with a
  query and test suite.
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

**`model.pure`**:
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

The EMIT module will depend on existing `legend-engine` modules:

| Dependency | Purpose |
|---|---|
| `legend-engine-language-pure-grammar` | `PureGrammarParser` for parsing `.pure` files |
| `legend-engine-language-pure-compiler` | `PureModel` construction / compilation |
| `legend-engine-language-pure-dsl-generation` | `ModelGenerationExtension`, `ArtifactGenerationExtension` SPIs |
| `legend-engine-xt-artifact-generation-http-api` | `ArtifactGenerationFactory` |
| `legend-engine-testable` | `TestableRunner` |
| `legend-engine-executionPlan-generation` | `PlanGenerator` (used internally by `ServicePlanGenerator`) |
| `legend-engine-language-pure-dsl-service` | Service protocol model |
| `legend-engine-language-pure-dsl-service-generation` | `ServicePlanGenerator` |
| `legend-engine-protocol-pure` | `PureModelContextData`, `PureClientVersions` |

No dependency on `legend-sdlc` is required.

---

## 10. Error Handling and Reporting

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

## 11. Future Extensions

- **Catalog web UI**: A static site or lightweight server that renders the catalog index as a
  searchable, browsable documentation site (e.g., auto-generated from the index JSON).
- **CLI search tool**: `emit search --feature relational-mapping --complexity basic`.
- **Auto-derived metadata**: Introspect `PureModelContextData` to supplement `emit.yaml` tags.
- **Catalog completeness CI check**: Validate that every model has `emit.yaml`, required fields
  are present, and feature tags come from the controlled vocabulary.
- **Model diff testing**: Compare EMIT results between two model versions.
- **Performance benchmarking**: Track phase durations over time.
- **CI integration**: Report EMIT results as GitHub check annotations.
- **Model coverage**: Track which model elements are exercised by tests.
- **Multi-model composition**: Support dependency models (e.g., shared type definitions).
- **Custom phase plugins**: Allow registering additional phases via SPI.
- **Feature coverage matrix**: Auto-generate a matrix showing which features are covered by
  examples and which lack coverage, helping guide the creation of new examples.

---

## 12. Implementation Roadmap

| Step | Description | Estimated Effort |
|---|---|---|
| 1 | Create Maven module `legend-engine-core-emit` with POM and dependencies | Small |
| 2 | Define `emit.yaml` schema and implement `EMITModelDescriptor` (YAML parsing via SnakeYAML or Jackson YAML) | Small |
| 3 | Implement `EMITModelLoader` (file reading, YAML parsing, `.pure` file discovery) | Small |
| 4 | Implement `EMITResult`, `EMITPhase`, `EMITPhaseResult` models | Small |
| 5 | Implement `EMITRunner` — Phase 1 (Parse) and Phase 2 (Compile), with `emit.yaml` awareness | Medium |
| 6 | Implement Phase 3 (Model Generation) | Medium |
| 7 | Implement Phase 4 (Artifact Generation) | Medium |
| 8 | Implement Phase 5 (Test Execution) | Medium |
| 9 | Implement Phase 6 (Plan Generation) | Medium |
| 10 | Implement `EMITCatalogIndex` and `EMITCatalogBuilder` (scan + in-memory query) | Medium |
| 11 | Write initial catalog of example `.pure` models with `emit.yaml` metadata (5-10 examples covering basic → advanced) | Medium |
| 12 | Write self-tests: `TestEMITRunner` (pipeline) and `TestEMITCatalog` (indexing/search) | Medium |
| 13 | Documentation and integration guide | Small |
| 14 | *(Future)* Catalog web UI / CLI search tool | Large |
