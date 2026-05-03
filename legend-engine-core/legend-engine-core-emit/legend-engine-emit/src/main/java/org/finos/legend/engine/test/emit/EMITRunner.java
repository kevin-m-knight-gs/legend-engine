// Copyright 2026 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.test.emit;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.SetIterable;
import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.engine.external.shared.format.extension.GenerationExtension;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.CompileContext;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.PureModel;
import org.finos.legend.engine.language.pure.dsl.generation.extension.Artifact;
import org.finos.legend.engine.language.pure.dsl.generation.extension.ArtifactGenerationExtension;
import org.finos.legend.engine.language.pure.dsl.generation.extension.ArtifactGenerationExtensionLoader;
import org.finos.legend.engine.language.pure.dsl.generation.extension.ModelGenerationExtension;
import org.finos.legend.engine.language.pure.dsl.service.generation.ServicePlanGenerator;
import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParser;
import org.finos.legend.engine.plan.generation.transformers.LegendPlanTransformers;
import org.finos.legend.engine.plan.platform.PlanPlatform;
import org.finos.legend.engine.protocol.pure.PureClientVersions;
import org.finos.legend.engine.protocol.pure.m3.PackageableElement;
import org.finos.legend.engine.protocol.pure.v1.model.context.PureModelContextData;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.ExecutionPlan;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.fileGeneration.FileGenerationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.generationSpecification.GenerationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.service.Service;
import org.finos.legend.engine.protocol.pure.v1.model.test.result.TestExecuted;
import org.finos.legend.engine.protocol.pure.v1.model.test.result.TestExecutionStatus;
import org.finos.legend.engine.pure.code.core.PureCoreExtensionLoader;
import org.finos.legend.engine.shared.core.deployment.DeploymentMode;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.test.emit.catalog.EMITModelDescriptor;
import org.finos.legend.engine.testable.TestableRunner;
import org.finos.legend.engine.testable.extension.TestableRunnerExtensionLoader;
import org.finos.legend.engine.testable.model.RunTestsResult;
import org.finos.legend.engine.testable.model.RunTestsTestableInput;
import org.finos.legend.pure.generated.Root_meta_pure_extension_Extension;
import org.finos.legend.pure.generated.Root_meta_pure_generation_metamodel_GenerationOutput;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class EMITRunner
{
    private static final String CLIENT_VERSION = PureClientVersions.production;

    private final EMITModelLoader loader;

    public EMITRunner()
    {
        this(new EMITModelLoader());
    }

    public EMITRunner(EMITModelLoader loader)
    {
        this.loader = loader;
    }

    public EMITResult runFromYaml(Path emitYaml)
    {
        EMITResult result = new EMITResult();

        EMITSourceSet sourceSet;
        long start = System.currentTimeMillis();
        try
        {
            sourceSet = this.loader.load(emitYaml);
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.success(EMITPhase.INITIALIZATION, elapsed,
                    sourceSet.getModelFiles().size() + " model files, " + sourceSet.getDependencyFiles().size() + " dependency files",
                    sourceSet));
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.INITIALIZATION, elapsed, e.getMessage(), e));
            markRemainingNotRun(result, EMITPhase.INITIALIZATION);
            return result;
        }

        return run(sourceSet, result);
    }

    public EMITResult run(EMITModelDescriptor descriptor)
    {
        EMITResult result = new EMITResult();

        EMITSourceSet sourceSet;
        long start = System.currentTimeMillis();
        try
        {
            sourceSet = this.loader.load(descriptor);
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.success(EMITPhase.INITIALIZATION, elapsed,
                    sourceSet.getModelFiles().size() + " model files, " + sourceSet.getDependencyFiles().size() + " dependency files",
                    sourceSet));
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.INITIALIZATION, elapsed, e.getMessage(), e));
            markRemainingNotRun(result, EMITPhase.INITIALIZATION);
            return result;
        }

        return run(sourceSet, result);
    }

    private EMITResult run(EMITSourceSet sourceSet, EMITResult result)
    {
        ParseOutput parsed = parse(result, sourceSet);
        if (parsed == null)
        {
            markRemainingNotRun(result, EMITPhase.PARSE);
            return result;
        }

        PureModel pureModel = compile(result, parsed.getCombined());
        if (pureModel == null)
        {
            markRemainingNotRun(result, EMITPhase.COMPILE);
            return result;
        }

        ModelGenOutcome modelGen = runModelGeneration(result, parsed.getCombined(), pureModel);
        if (modelGen.isFailed())
        {
            markRemainingNotRun(result, EMITPhase.MODEL_GENERATION);
            return result;
        }
        PureModelContextData effectivePmcd = modelGen.getEffectivePmcd();
        PureModel effectivePureModel = modelGen.getEffectivePureModel();

        runFileGeneration(result, effectivePmcd, effectivePureModel);
        runTestExecution(result, effectivePmcd, effectivePureModel, parsed.getPrimarySourceIds());
        runPlanGeneration(result, effectivePmcd, effectivePureModel, parsed.getPrimarySourceIds());

        return result;
    }

    // ------- Phase 1: Parse -------

    private static class ParseOutput
    {
        private final PureModelContextData combined;
        private final SetIterable<String> primarySourceIds;

        private ParseOutput(PureModelContextData combined, SetIterable<String> primarySourceIds)
        {
            this.combined = combined;
            this.primarySourceIds = primarySourceIds;
        }

        PureModelContextData getCombined()
        {
            return this.combined;
        }

        SetIterable<String> getPrimarySourceIds()
        {
            return this.primarySourceIds;
        }
    }

    private ParseOutput parse(EMITResult result, EMITSourceSet sourceSet)
    {
        long start = System.currentTimeMillis();
        try
        {
            PureGrammarParser parser = PureGrammarParser.newInstance();
            PureModelContextData.Builder builder = PureModelContextData.newBuilder();
            MutableSet<String> primarySourceIds = Sets.mutable.empty();

            int[] totalElements = {0};
            sourceSet.forEachFile(file ->
            {
                PureModelContextData fileData = parser.parseModel(readFile(file), file.getVirtualPath(), 0, 0, true);
                builder.addPureModelContextData(fileData);
                totalElements[0] += fileData.getElements().size();
                if (file.isPrimary())
                {
                    primarySourceIds.add(file.getVirtualPath());
                }
            });
            PureModelContextData combined = builder.build();

            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.success(EMITPhase.PARSE, elapsed, sourceSet.getTotalFileCount() + " files, " + totalElements[0] + " elements", combined));
            return new ParseOutput(combined, primarySourceIds);
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.PARSE, elapsed, e.getMessage(), e));
            return null;
        }
    }

    private String readFile(EMITSourceFile file)
    {
        try
        {
            return new String(Files.readAllBytes(file.getAbsolutePath()), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    // ------- Phase 2: Compile -------

    private PureModel compile(EMITResult result, PureModelContextData pmcd)
    {
        long start = System.currentTimeMillis();
        try
        {
            PureModel pureModel = new PureModel(pmcd, Identity.getAnonymousIdentity().getName(), DeploymentMode.TEST);
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.success(EMITPhase.COMPILE, elapsed, "PureModel built successfully", pureModel));
            return pureModel;
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.COMPILE, elapsed, e.getMessage(), e));
            return null;
        }
    }

    // ------- Phase 3: Model Generation -------

    private static class ModelGenOutcome
    {
        private final boolean failed;
        private final PureModelContextData effectivePmcd;
        private final PureModel effectivePureModel;

        private ModelGenOutcome(boolean failed, PureModelContextData effectivePmcd, PureModel effectivePureModel)
        {
            this.failed = failed;
            this.effectivePmcd = effectivePmcd;
            this.effectivePureModel = effectivePureModel;
        }

        boolean isFailed()
        {
            return this.failed;
        }

        PureModelContextData getEffectivePmcd()
        {
            return this.effectivePmcd;
        }

        PureModel getEffectivePureModel()
        {
            return this.effectivePureModel;
        }
    }

    private ModelGenOutcome runModelGeneration(EMITResult result, PureModelContextData pmcd, PureModel pureModel)
    {
        long start = System.currentTimeMillis();
        if (pmcd.getElementsOfType(GenerationSpecification.class).isEmpty())
        {
            result.add(EMITPhaseResult.skipped(EMITPhase.MODEL_GENERATION, "no GenerationSpecification"));
            return new ModelGenOutcome(false, pmcd, pureModel);
        }
        try
        {
            MutableList<PureModelContextData> generated = Lists.mutable.empty();
            ServiceLoader.load(ModelGenerationExtension.class).forEach(extension ->
            {
                List<Function3<org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.PackageableElement, CompileContext, String, PureModelContextData>> generators = extension.getPureModelContextDataGenerators();
                if (generators == null || generators.isEmpty())
                {
                    return;
                }
                pmcd.getElements().forEach(protocolElement ->
                {
                    org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.PackageableElement pureElement;
                    try
                    {
                        pureElement = pureModel.getPackageableElement(protocolElement.getPath());
                    }
                    catch (Exception ignored)
                    {
                        return;
                    }
                    CompileContext context = pureModel.getContext(protocolElement);
                    generators.forEach(generator ->
                    {
                        PureModelContextData out = generator.value(pureElement, context, CLIENT_VERSION);
                        if (out != null && !out.getElements().isEmpty())
                        {
                            generated.add(out);
                        }
                    });
                });
            });

            if (generated.isEmpty())
            {
                long elapsed = System.currentTimeMillis() - start;
                result.add(EMITPhaseResult.success(EMITPhase.MODEL_GENERATION, elapsed, "no generators produced output", pmcd));
                return new ModelGenOutcome(false, pmcd, pureModel);
            }

            PureModelContextData.Builder builder = PureModelContextData.newBuilder().withPureModelContextData(pmcd);
            generated.forEach(builder::addPureModelContextData);
            PureModelContextData enriched = builder.build();
            PureModel enrichedModel = new PureModel(enriched, Identity.getAnonymousIdentity().getName(), DeploymentMode.TEST);

            long elapsed = System.currentTimeMillis() - start;
            int newElements = enriched.getElements().size() - pmcd.getElements().size();
            result.add(EMITPhaseResult.success(EMITPhase.MODEL_GENERATION, elapsed, "generated " + newElements + " elements from " + generated.size() + " extension calls", enriched));
            return new ModelGenOutcome(false, enriched, enrichedModel);
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.MODEL_GENERATION, elapsed, e.getMessage(), e));
            return new ModelGenOutcome(true, pmcd, pureModel);
        }
    }

    // ------- Phase 4: File Generation (specs + artifacts) -------

    public static class FileGenerationOutput
    {
        private final Map<String, List<Root_meta_pure_generation_metamodel_GenerationOutput>> bySpecification = new LinkedHashMap<>();
        private final Map<String, List<Artifact>> byArtifactExtension = new LinkedHashMap<>();

        public Map<String, List<Root_meta_pure_generation_metamodel_GenerationOutput>> getBySpecification()
        {
            return Collections.unmodifiableMap(this.bySpecification);
        }

        public Map<String, List<Artifact>> getByArtifactExtension()
        {
            return Collections.unmodifiableMap(this.byArtifactExtension);
        }
    }

    private void runFileGeneration(EMITResult result, PureModelContextData pmcd, PureModel pureModel)
    {
        long start = System.currentTimeMillis();
        MutableList<FileGenerationSpecification> fileGens = pmcd.getElementsOfType(FileGenerationSpecification.class);
        List<ArtifactGenerationExtension> artifactExts = ArtifactGenerationExtensionLoader.extensions();

        boolean specsApplicable = fileGens.notEmpty();
        boolean artifactsApplicable = !artifactExts.isEmpty() && !pmcd.getElements().isEmpty();
        if (!specsApplicable && !artifactsApplicable)
        {
            result.add(EMITPhaseResult.skipped(EMITPhase.FILE_GENERATION, "no FileGenerationSpecification or ArtifactGenerationExtension applies"));
            return;
        }

        try
        {
            FileGenerationOutput output = new FileGenerationOutput();

            if (specsApplicable)
            {
                MutableList<GenerationExtension> generationExtensions = Lists.mutable.withAll(ServiceLoader.load(GenerationExtension.class));
                fileGens.forEach(spec ->
                {
                    GenerationExtension match = (spec.type == null) ? null : generationExtensions.detect(c -> spec.type.equals(c.getKey()));
                    if (match == null)
                    {
                        throw new IllegalStateException("No GenerationExtension registered for FileGenerationSpecification type '" + spec.type + "' (element " + spec.getPath() + ")");
                    }
                    List<Root_meta_pure_generation_metamodel_GenerationOutput> outs = match.generateFromElement(spec, pureModel.getContext());
                    output.bySpecification.put(spec.getPath(), (outs == null) ? Lists.fixedSize.empty() : outs);
                });
            }

            if (artifactsApplicable)
            {
                artifactExts.forEach(ext ->
                {
                    MutableList<Artifact> artifacts = ListIterate.flatCollect(pmcd.getElements(), element ->
                    {
                        org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.PackageableElement pureElement;
                        try
                        {
                            pureElement = pureModel.getPackageableElement(element.getPath());
                        }
                        catch (Exception ignored)
                        {
                            return Lists.fixedSize.empty();
                        }
                        if (ext.canGenerate(pureElement))
                        {
                            List<Artifact> arts = ext.generate(pureElement, pureModel, pmcd, CLIENT_VERSION);
                            if (arts != null)
                            {
                                return arts;
                            }
                        }
                        return Lists.fixedSize.empty();
                    });
                    if (artifacts.notEmpty())
                    {
                        output.byArtifactExtension.put(ext.getKey(), artifacts);
                    }
                });
            }

            long elapsed = System.currentTimeMillis() - start;
            String message = output.bySpecification.size() + " file generations, " + output.byArtifactExtension.size() + " artifact extensions";
            result.add(EMITPhaseResult.success(EMITPhase.FILE_GENERATION, elapsed, message, output));
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.FILE_GENERATION, elapsed, e.getMessage(), e));
        }
    }

    // ------- Phase 5: Test Execution -------

    private void runTestExecution(EMITResult result, PureModelContextData pmcd, PureModel pureModel, SetIterable<String> primarySourceIds)
    {
        long start = System.currentTimeMillis();
        MutableList<RunTestsTestableInput> inputs = ListIterate.collectIf(pmcd.getElements(),
                e -> isInPrimaryScope(e, primarySourceIds) && TestableRunnerExtensionLoader.isTestable(e) && !TestableRunnerExtensionLoader.isTestableEmpty(e),
                e ->
                {
                    RunTestsTestableInput input = new RunTestsTestableInput();
                    input.testable = e.getPath();
                    return input;
                },
                Lists.mutable.empty());
        if (inputs.isEmpty())
        {
            result.add(EMITPhaseResult.skipped(EMITPhase.TEST_EXECUTION, "no Testable elements with tests in primary scope"));
            return;
        }
        EMITPhaseResult phaseResult;
        try
        {
            RunTestsResult runTestsResult = new TestableRunner().doTests(inputs, pureModel, pmcd);
            int total = runTestsResult.results.size();
            int failed = ListIterate.count(runTestsResult.results, r -> !(r instanceof TestExecuted) || (((TestExecuted) r).testExecutionStatus == TestExecutionStatus.FAIL));
            long elapsed = System.currentTimeMillis() - start;
            String message = total + " tests; " + failed + " failed";
            phaseResult = (failed == 0) ? EMITPhaseResult.success(EMITPhase.TEST_EXECUTION, elapsed, message, runTestsResult) : EMITPhaseResult.failure(EMITPhase.TEST_EXECUTION, elapsed, message, null, runTestsResult);
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            phaseResult = EMITPhaseResult.failure(EMITPhase.TEST_EXECUTION, elapsed, e.getMessage(), e);
        }
        result.add(phaseResult);
    }

    // ------- Phase 6: Plan Generation -------

    private void runPlanGeneration(EMITResult result, PureModelContextData pmcd, PureModel pureModel, SetIterable<String> primarySourceIds)
    {
        long start = System.currentTimeMillis();
        MutableList<Service> services = ListIterate.collectIf(pmcd.getElements(),
                e -> (e instanceof Service) && isInPrimaryScope(e, primarySourceIds),
                e -> (Service) e,
                Lists.mutable.empty());
        if (services.isEmpty())
        {
            result.add(EMITPhaseResult.skipped(EMITPhase.PLAN_GENERATION, "no Service elements in primary scope"));
            return;
        }
        try
        {
            RichIterable<? extends Root_meta_pure_extension_Extension> extensions = PureCoreExtensionLoader.extensions()
                    .flatCollect(e -> e.extraPureCoreExtensions(pureModel.getExecutionSupport()));

            Map<String, ExecutionPlan> plans = new LinkedHashMap<>();
            services.forEach(service ->
            {
                ExecutionPlan plan = ServicePlanGenerator.generateServiceExecutionPlan(service, null, pureModel, CLIENT_VERSION, PlanPlatform.JAVA, extensions, LegendPlanTransformers.transformers);
                plans.put(service.getPath(), plan);
            });
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.success(EMITPhase.PLAN_GENERATION, elapsed, "plans for " + plans.size() + " service(s)", Collections.unmodifiableMap(plans)));
        }
        catch (Exception e)
        {
            long elapsed = System.currentTimeMillis() - start;
            result.add(EMITPhaseResult.failure(EMITPhase.PLAN_GENERATION, elapsed, e.getMessage(), e));
        }
    }

    // ------- helpers -------

    private static boolean isInPrimaryScope(PackageableElement element, SetIterable<String> primarySourceIds)
    {
        return (element.sourceInformation == null) ||
                (element.sourceInformation.sourceId == null) ||
                primarySourceIds.contains(element.sourceInformation.sourceId);
    }

    private static void markRemainingNotRun(EMITResult result, EMITPhase failed)
    {
        EMITPhase[] values = EMITPhase.values();
        for (int i = failed.ordinal() + 1; i < values.length; i++)
        {
            EMITPhase phase = values[i];
            if (result.getPhase(phase) == null)
            {
                result.add(EMITPhaseResult.notRun(phase, "skipped due to failure in " + failed.name()));
            }
        }
    }
}
