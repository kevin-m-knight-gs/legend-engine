// Copyright 2020 Goldman Sachs
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

package org.finos.legend.engine.language.pure.compiler.toPureGraph;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.SetIterable;
import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.handlers.Handlers;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.test.TestBuilderHelper;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.context.PackageableElementPointer;
import org.finos.legend.engine.protocol.pure.m3.multiplicity.Multiplicity;
import org.finos.legend.engine.protocol.pure.v1.model.context.PackageableElementType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.AssociationMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.ClassMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.EnumValueMappingEnumSourceValue;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.EnumValueMappingIntegerSourceValue;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.EnumValueMappingSourceValue;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.EnumValueMappingStringSourceValue;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.EnumerationMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.LocalMappingPropertyInfo;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.PropertyMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.aggregationAware.AggregateFunction;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.aggregationAware.AggregateSetImplementationContainer;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.aggregationAware.GroupByFunction;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.mappingTest.InputData;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.mappingTest.MappingTest;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.mappingTest.MappingTestSuite;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.mappingTest.MappingTest_Legacy;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.mappingTest.StoreTestData;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.xStore.XStoreAssociationMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.modelToModel.mapping.ObjectInputData;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.modelToModel.mapping.PurePropertyMapping;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.ValueSpecification;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.Variable;
import org.finos.legend.engine.protocol.pure.m3.function.LambdaFunction;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.finos.legend.pure.generated.Root_meta_external_store_model_ModelStore_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_data_StoreTestData;
import org.finos.legend.pure.generated.Root_meta_pure_data_StoreTestData_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_EnumValueMapping_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_EnumerationMapping_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_MappingClass_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_aggregationAware_AggregateSetImplementationContainer_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_aggregationAware_AggregateSpecification_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_aggregationAware_AggregationFunctionSpecification_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_aggregationAware_GroupByFunctionSpecification_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_metamodel_MappingTestSuite;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_metamodel_MappingTestSuite_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_mapping_xStore_XStoreAssociationImplementation_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_metamodel_function_LambdaFunction_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_metamodel_function_property_Property_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_metamodel_relationship_Generalization_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_metamodel_type_generics_GenericType_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_metamodel_valuespecification_VariableExpression_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_test_AtomicTest;
import org.finos.legend.pure.m2.dsl.mapping.M2MappingPaths;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.AssociationImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.InstanceSetImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.Mapping;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.MappingClass;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.PropertyMappingsImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.SetImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.aggregationAware.AggregateSpecification;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.aggregationAware.AggregationFunctionSpecification;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.aggregationAware.GroupByFunctionSpecification;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.xStore.XStoreAssociationImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.PropertyOwner;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.property.Property;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.relationship.Association;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.relationship.Generalization;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.Class;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.generics.GenericType;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.InstanceValue;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.VariableExpression;
import org.finos.legend.pure.m3.coreinstance.meta.pure.store.Store;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.testable.Test;
import org.finos.legend.pure.m3.navigation.M3Paths;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.legend.pure.generated.platform_dsl_mapping_functions_Mapping.Root_meta_pure_mapping__allClassMappingsRecursive_Mapping_1__SetImplementation_MANY_;

public class HelperMappingBuilder
{
    public static SetIterable<Mapping> getAllIncludedMappings(Mapping mapping)
    {
        return collectAllIncludedMappings(mapping, Sets.mutable.empty());
    }

    private static MutableSet<Mapping> collectAllIncludedMappings(Mapping mapping, MutableSet<Mapping> results)
    {
        if (results.add(mapping))
        {
            mapping._includes().forEach(include -> collectAllIncludedMappings(include._included(), results));
        }
        return results;
    }

    public static ImmutableSet<SetImplementation> getAllClassMappings(Mapping mapping)
    {
        return HelperMappingBuilder.getAllIncludedMappings(mapping).flatCollect(m -> (Iterable<SetImplementation>) m._classMappings()).toSet().toImmutable();
    }

    public static ImmutableSet<org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.EnumerationMapping<Object>> getAllEnumerationMappings(Mapping mapping)
    {
        return HelperMappingBuilder.getAllIncludedMappings(mapping).flatCollect(m -> (Iterable<org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.EnumerationMapping<Object>>) m._enumerationMappings(), Sets.mutable.empty()).toImmutable();
    }

    public static String getPropertyMappingTargetId(PropertyMapping pm)
    {
        return pm.target == null ? "" : pm.target;
    }

    public static String getPropertyMappingTargetId(PropertyMapping propertyMapping, Property<?, ?> property, CompileContext context)
    {
        if (propertyMapping.target == null && (property._genericType()._rawType() instanceof Class))
        {
            return HelperModelBuilder.getTypeFullPath(property._genericType()._rawType(), "_", context.pureModel.getExecutionSupport());
        }
        return HelperMappingBuilder.getPropertyMappingTargetId(propertyMapping);
    }

    public static org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.EnumerationMapping<Object> processEnumMapping(EnumerationMapping em, Mapping pureMapping, CompileContext context)
    {
        // validate there is no mixed protocol format for source value
        List<Object> sourceValues = em.enumValueMappings.stream().flatMap(enumValueMapping -> enumValueMapping.sourceValues.stream()).collect(Collectors.toList());
        if (sourceValues.stream().anyMatch(sourceValue -> !(sourceValue instanceof EnumValueMappingSourceValue)) && sourceValues.stream().anyMatch(sourceValue -> sourceValue instanceof EnumValueMappingSourceValue))
        {
            throw new EngineException("Mixed formats for enum value mapping source values", em.sourceInformation, EngineErrorType.COMPILATION);
        }
        // process enum value mappings
        String id = getEnumerationMappingId(em);
        return new Root_meta_pure_mapping_EnumerationMapping_Impl<>(id, SourceInformationHelper.toM3SourceInformation(em.sourceInformation), null)
                ._classifierGenericType(context.newGenericType(context.pureModel.getType(M2MappingPaths.EnumerationMapping), Lists.mutable.with(context.newGenericType(context.pureModel.getType(M3Paths.Any)))))
                ._name(id)
                ._parent(pureMapping)
                ._enumeration(context.resolveEnumeration(em.enumeration.path, em.enumeration.sourceInformation))
                ._enumValueMappings(ListIterate.collect(em.enumValueMappings, v -> new Root_meta_pure_mapping_EnumValueMapping_Impl(null, SourceInformationHelper.toM3SourceInformation(v.sourceInformation), context.pureModel.getClass("meta::pure::mapping::EnumValueMapping"))
                        ._enum(context.resolveEnumValue(em.enumeration.path, v.enumValue))
                        ._sourceValues(convertSourceValues(em, v.sourceValues, context))
                ));
    }

    public static RichIterable<?> convertSourceValues(EnumerationMapping em, List<Object> sourceValues, CompileContext context)
    {
        // for v1_11_0 and later, we structure source value
        if (sourceValues.stream().allMatch(sourceValue -> sourceValue instanceof EnumValueMappingSourceValue))
        {
            return ListIterate.collect(sourceValues, o -> processStructuredSourceValue(o, context));
        }
        // for v1_5_0 and below, old format that has a structure for the sourceValue with type
        if (sourceValues.size() == 1 && sourceValues.get(0) instanceof Map)
        {
            return processSourceValuesWithTypeFlagOnEnumValueMapping((Map) sourceValues.get(0), context);
        }
        // for v1_10_0, we experimented with having `sourceType` on the enumeration mapping but that not only does not help
        // but even causes more trouble as it does not solve the fundamental problem
        if (em.sourceType != null)
        {
            return processSourceValuesWithSourceType(sourceValues, em.sourceType, context);
        }
        // for v1_6_0 to v1_9_0, we used `transformAny` function so source value can be anything String or Integer
        return ListIterate.collect(sourceValues, HelperMappingBuilder::processSimpleSourceValue);
    }

    /**
     * Process structured source value with type information embedded in each value
     * (>= v1_11_0)
     */
    private static Object processStructuredSourceValue(Object o, CompileContext context)
    {
        if (o instanceof EnumValueMappingIntegerSourceValue)
        {
            return Long.valueOf(((EnumValueMappingIntegerSourceValue) o).value);
        }
        else if (o instanceof EnumValueMappingStringSourceValue)
        {
            return ((EnumValueMappingStringSourceValue) o).value;
        }
        return context.resolveEnumValue(((EnumValueMappingEnumSourceValue) o).enumeration, ((EnumValueMappingEnumSourceValue) o).value);
    }

    /**
     * Try to convert OLD format source value to their appropriate type when possible
     * (>= v1_6_0 and <= v1_9_0)
     */
    private static Object processSimpleSourceValue(Object o)
    {
        if (o instanceof Integer)
        {
            // We use Long.valueOf() so that we can have a wider range of number
            return Long.valueOf((Integer) o);
        }
        return o;
    }

    /**
     * Support converting OLD format that has a structure for the sourceValue with type (<= v1_5_0)
     */
    private static RichIterable<?> processSourceValuesWithTypeFlagOnEnumValueMapping(Map map, CompileContext context)
    {
        String type = (String) map.get("_type");
        if ("string".equals(type))
        {
            return Lists.mutable.withAll((List) map.get("values"));
        }
        else if ("integer".equals(type))
        {
            // We use Long.valueOf() so that we can have a wider range of number
            return ListIterate.collect((List<Object>) map.get("values"), o -> Long.valueOf((Integer) o));
        }
        else if ("enumValue".equals(type))
        {
            return Lists.mutable.with(context.resolveEnumValue((String) map.get("fullPath"), (String) map.get("value")));
        }
        else if ("collection".equals(type))
        {
            return ListIterate.collect((List<Object>) map.get("values"), o -> processSourceValuesWithTypeFlagOnEnumValueMapping((Map) o, context));
        }
        throw new UnsupportedOperationException("Type '" + type + "' is not supported in enumeration mapping");
    }

    /**
     * Support converting OLD format that has source type (v1_10_0)
     */
    private static RichIterable<?> processSourceValuesWithSourceType(List<Object> sourceValues, String sourceType, CompileContext context)
    {
        if (sourceType.toUpperCase().equals("STRING"))
        {
            // String needs no processing
            return Lists.mutable.withAll(sourceValues);
        }
        else if (sourceType.toUpperCase().equals("INTEGER"))
        {
            return ListIterate.collect(sourceValues, o ->
            {
                // Since in the protocol we had SourceValues: String[*] for easy serialization, we need to anticipate
                // that we can receive "10" for value 10 but with the source type, we know how to process this to 10
                if (o instanceof String)
                {
                    return Long.valueOf((String) o);
                }
                else if (o instanceof Integer)
                {
                    return Long.valueOf((Integer) o);
                }
                return o;
            });
        }
        else
        {
            return ListIterate.collect(sourceValues, o -> context.resolveEnumValue(sourceType, (String) o));
        }
    }

    public static String getEnumerationMappingId(EnumerationMapping em)
    {
        return em.id != null ? em.id : em.enumeration.path.replaceAll("::", "_");
    }

    public static String getClassMappingId(ClassMapping cm, CompileContext context)
    {
        return cm.id != null ? cm.id : HelperModelBuilder.getElementFullPath(context.resolveClass(cm._class, cm.classSourceInformation), context.pureModel.getExecutionSupport()).replaceAll("::", "_");
    }

    public static org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.LambdaFunction<?> processPurePropertyMappingTransform(PurePropertyMapping ppm, LambdaFunction lambda, PropertyMappingsImplementation owner, org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.Type inputVarType, CompileContext context, String mappingName)
    {
        List<ValueSpecification> expressions = lambda.body;
        VariableExpression lambdaParam = new Root_meta_pure_metamodel_valuespecification_VariableExpression_Impl("", null, context.pureModel.getClass("meta::pure::metamodel::valuespecification::VariableExpression"))
                ._name("src")
                ._multiplicity(context.pureModel.getMultiplicity("one"))
                ._genericType(context.newGenericType(inputVarType));
        MutableList<VariableExpression> pureParameters = Lists.mutable.with(lambdaParam);
        ProcessingContext ctx = new ProcessingContext("Pure M2M Transform Lambda");
        ctx.addInferredVariables("src", lambdaParam);
        MutableList<String> openVariables = Lists.mutable.empty();
        MutableList<org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.ValueSpecification> valueSpecifications = ListIterate.collect(expressions, p -> p.accept(new ValueSpecificationBuilder(context, openVariables, ctx)));
        MutableList<String> cleanedOpenVariables = openVariables.distinct();
        cleanedOpenVariables.removeAll(pureParameters.collect(e -> e._name()));
        GenericType functionType = PureModel.buildFunctionType(pureParameters, valueSpecifications.getLast()._genericType(), valueSpecifications.getLast()._multiplicity(), context.pureModel);
        String propertyName = owner._id() + "." + ppm.property.property;
        ctx.flushVariable("src");
        return new Root_meta_pure_metamodel_function_LambdaFunction_Impl<Object>(propertyName, SourceInformationHelper.toM3SourceInformation(lambda.sourceInformation), null)
                ._classifierGenericType(context.newGenericType(context.pureModel.getType(M3Paths.LambdaFunction), Lists.mutable.with(functionType)))
                ._openVariables(cleanedOpenVariables)
                ._expressionSequence(valueSpecifications);
    }

    public static void collectPrerequisiteElementsFromPurePropertyMappingTransform(Set<PackageableElementPointer> prerequisiteElements, PurePropertyMapping ppm, CompileContext context)
    {
        ValueSpecificationPrerequisiteElementsPassBuilder valueSpecificationPrerequisiteElementsPassBuilder = new ValueSpecificationPrerequisiteElementsPassBuilder(context, prerequisiteElements);
        ListIterate.forEach(ppm.transform.body, p -> p.accept(valueSpecificationPrerequisiteElementsPassBuilder));
    }

    public static void processMappingTest(MappingTest_Legacy mappingTestLegacy, CompileContext context)
    {
        // todo hack to support legacy test flow
        // this is to ensure generics are set and prevent NPE
        // this assume a cache for types, and this prime the value
        context.resolveGenericType("meta::pure::mapping::Result")
                ._typeArguments(Lists.fixedSize.of(context.resolveGenericType("meta::pure::metamodel::type::Any")))
                ._multiplicityArguments(Lists.fixedSize.of(context.pureModel.getMultiplicity("zeromany")));

        HelperValueSpecificationBuilder.buildLambda(mappingTestLegacy.query, context);
        mappingTestLegacy.inputData.forEach(t -> HelperMappingBuilder.processMappingTestInputData(t, context));
    }

    public static void collectPrerequisiteElementsFromMappingTest(Set<PackageableElementPointer> prerequisiteElements, MappingTest_Legacy mappingTestLegacy, CompileContext context)
    {
        mappingTestLegacy.query.accept(new ValueSpecificationPrerequisiteElementsPassBuilder(context, prerequisiteElements));
        ListIterate.forEach(mappingTestLegacy.inputData, t -> HelperMappingBuilder.collectPrerequisiteElementsFromMappingTestInputData(prerequisiteElements, t, context));
    }

    public static void processMappingTestInputData(InputData inputData, CompileContext context)
    {
        if (inputData instanceof ObjectInputData)
        {
            ObjectInputData objectInputData = (ObjectInputData) inputData;
            if (objectInputData.inputType == null)
            {
                throw new EngineException("Object input data does not have a format type", objectInputData.sourceInformation, EngineErrorType.COMPILATION);
            }
            context.resolveClass(objectInputData.sourceClass, objectInputData.sourceInformation);
        }
        else
        {
            context.getCompilerExtensions().getExtraMappingTestInputDataProcessors().forEach(processor -> processor.value(inputData, context));
        }
    }

    public static void collectPrerequisiteElementsFromMappingTestInputData(Set<PackageableElementPointer> prerequisiteElements, InputData inputData, CompileContext context)
    {
        if (inputData instanceof ObjectInputData)
        {
            ObjectInputData objectInputData = (ObjectInputData) inputData;
            prerequisiteElements.add(new PackageableElementPointer(PackageableElementType.CLASS, objectInputData.sourceClass, objectInputData.sourceInformation));
        }
        else
        {
            context.getCompilerExtensions().getExtraMappingTestInputDataPrerequisiteElementsPassProcessors().forEach(processor -> processor.value(inputData, prerequisiteElements));
        }
    }

    public static AssociationImplementation processAssociationImplementation(AssociationMapping associationMapping, CompileContext context, Mapping parentMapping)
    {
        if (associationMapping instanceof XStoreAssociationMapping)
        {
            XStoreAssociationMapping xStoreAssociationMapping = (XStoreAssociationMapping) associationMapping;
            XStoreAssociationImplementation base = new Root_meta_pure_mapping_xStore_XStoreAssociationImplementation_Impl("", SourceInformationHelper.toM3SourceInformation(associationMapping.sourceInformation), context.pureModel.getClass("meta::pure::mapping::xStore::XStoreAssociationImplementation"));
            final org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.relationship.Association pureAssociation = context.resolveAssociation(xStoreAssociationMapping.association.path, xStoreAssociationMapping.association.sourceInformation);
            MutableList<Store> stores = ListIterate.collect(xStoreAssociationMapping.stores, context::resolveStore);
            base._association(pureAssociation)._stores(stores)._parent(parentMapping)._propertyMappings(ListIterate.collect(xStoreAssociationMapping.propertyMappings, propertyMapping -> propertyMapping.accept(new PropertyMappingBuilder(context, parentMapping, base, HelperMappingBuilder.getAllClassMappings(parentMapping)))));
            return base;
        }
        return context.getCompilerExtensions().getExtraAssociationMappingProcessors().stream()
                .map(processor -> processor.value(associationMapping, parentMapping, context))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Unsupported association mapping type '" + associationMapping.getClass() + "'"));
    }

    public static void collectPrerequisiteElementsFromAssociationImplementation(Set<PackageableElementPointer> prerequisiteElements, AssociationMapping associationMapping, CompileContext context)
    {
        if (associationMapping instanceof XStoreAssociationMapping)
        {
            XStoreAssociationMapping xStoreAssociationMapping = (XStoreAssociationMapping) associationMapping;
            prerequisiteElements.add(xStoreAssociationMapping.association);
            prerequisiteElements.addAll(ListIterate.collect(xStoreAssociationMapping.stores, s -> new PackageableElementPointer(PackageableElementType.STORE, s)));
            PropertyMappingPrerequisiteElementsBuilder propertyMappingPrerequisiteElementsBuilder = new PropertyMappingPrerequisiteElementsBuilder(context, prerequisiteElements);
            ListIterate.forEach(xStoreAssociationMapping.propertyMappings, propertyMapping -> propertyMapping.accept(propertyMappingPrerequisiteElementsBuilder));
        }
        else
        {
            context.getCompilerExtensions().getExtraAssociationMappingPrerequisiteElementsPassProcessors().forEach(processor -> processor.value(associationMapping, prerequisiteElements));
        }
    }

    public static Root_meta_pure_mapping_MappingClass_Impl processMappingClass(org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.MappingClass mappingclass, CompileContext context, Mapping parent)
    {
        Root_meta_pure_mapping_MappingClass_Impl mappingClass = new Root_meta_pure_mapping_MappingClass_Impl<>(" ", SourceInformationHelper.toM3SourceInformation(mappingclass.sourceInformation), null);
        mappingClass._name(mappingclass.name);
        MutableList<Generalization> generalizations = ListIterate.collect(mappingclass.superTypes, (superTypePtr) ->
        {
            String superType = superTypePtr.path;
            Generalization generalization = new Root_meta_pure_metamodel_relationship_Generalization_Impl("", null, context.pureModel.getClass("meta::pure::metamodel::relationship::Generalization"))._general(context.resolveGenericType(superType))._specific(mappingClass);
            context.resolveType(superType)._specializationsAdd(generalization);
            return generalization;
        });
        mappingClass._generalizations(generalizations);
        if (mappingclass.setImplementation != null)
        {
            mappingClass._setImplementation(mappingclass.setImplementation.accept(new ClassMappingFirstPassBuilder(context, parent)).getOne());
        }
        org.finos.legend.engine.protocol.pure.m3.type.Class rootClass = mappingclass.rootClass;
        if (rootClass != null)
        {
            context.processThirdPass(rootClass);
            mappingClass._class(context.pureModel.getClass(context.pureModel.buildPackageString(rootClass._package, rootClass.name), rootClass.sourceInformation));
        }
        return mappingClass;
    }

    public static void collectPrerequisiteElementsFromMappingClass(Set<PackageableElementPointer> prerequisiteElements, org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.MappingClass mappingClass, CompileContext context)
    {
        prerequisiteElements.addAll(mappingClass.superTypes);
        if (Objects.nonNull(mappingClass.setImplementation))
        {
            mappingClass.setImplementation.accept(new ClassMappingPrerequisiteElementsPassBuilder(context, prerequisiteElements));
        }
    }

    public static org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.aggregationAware.AggregateSetImplementationContainer processAggregateSetImplementationContainer(AggregateSetImplementationContainer aggregateSetImplementationContainer, CompileContext context, Mapping parent)
    {
        if (aggregateSetImplementationContainer.setImplementation.mappingClass == null)
        {
            Class<?> _class = context.resolveClass(aggregateSetImplementationContainer.setImplementation._class, aggregateSetImplementationContainer.setImplementation.classSourceInformation);
            aggregateSetImplementationContainer.setImplementation.mappingClass = new org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.MappingClass();
            aggregateSetImplementationContainer.setImplementation.mappingClass.name = _class.getName() + "_" + parent.getName() + "_" + aggregateSetImplementationContainer.setImplementation.id;
            aggregateSetImplementationContainer.setImplementation.mappingClass.superTypes = Lists.mutable.with(new PackageableElementPointer(HelperModelBuilder.getElementFullPath(_class, context.pureModel.getExecutionSupport())));
        }
        org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.aggregationAware.AggregateSetImplementationContainer container = new Root_meta_pure_mapping_aggregationAware_AggregateSetImplementationContainer_Impl("", null, context.pureModel.getClass("meta::pure::mapping::aggregationAware::AggregateSetImplementationContainer"));
        container._setImplementation((InstanceSetImplementation) aggregateSetImplementationContainer.setImplementation.accept(new ClassMappingFirstPassBuilder(context, parent)).getOne());
        container._index(aggregateSetImplementationContainer.index);
        container._aggregateSpecification(processAggregateSpecification(aggregateSetImplementationContainer.aggregateSpecification, context, Lists.mutable.empty(), aggregateSetImplementationContainer.setImplementation._class));
        return container;
    }

    public static void collectPrerequisiteElementsFromAggregateSetImplementationContainer(Set<PackageableElementPointer> prerequisiteElements, AggregateSetImplementationContainer aggregateSetImplementationContainer, CompileContext context)
    {
        prerequisiteElements.add(new PackageableElementPointer(PackageableElementType.CLASS, aggregateSetImplementationContainer.setImplementation._class, aggregateSetImplementationContainer.setImplementation.classSourceInformation));
        aggregateSetImplementationContainer.setImplementation.accept(new ClassMappingPrerequisiteElementsPassBuilder(context, prerequisiteElements));
        HelperMappingBuilder.collectPrerequisiteElementsFromAggregateSpecification(prerequisiteElements, aggregateSetImplementationContainer.aggregateSpecification, context);
    }

    private static AggregateSpecification processAggregateSpecification(org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.aggregationAware.AggregateSpecification aggregateSpecification, CompileContext context, MutableList<String> openVariables, String parentClassPath)
    {
        ProcessingContext ctx = new ProcessingContext("Lambda");
        org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.ValueSpecification thisVariable = HelperModelBuilder.createThisVariableForClass(context, parentClassPath);
        ctx.addInferredVariables("this", thisVariable);
        AggregateSpecification as = new Root_meta_pure_mapping_aggregationAware_AggregateSpecification_Impl(" ");
        as._canAggregate(aggregateSpecification.canAggregate);
        for (GroupByFunction gb : aggregateSpecification.groupByFunctions)
        {
            as._groupByFunctionsAdd(processGroupByFunction(gb, context, openVariables, ctx));
        }
        for (AggregateFunction af : aggregateSpecification.aggregateValues)
        {
            as._aggregateValuesAdd(processAggregationFunction(af, context, openVariables, ctx));
        }
        ctx.flushVariable("this");
        return as;
    }

    private static void collectPrerequisiteElementsFromAggregateSpecification(Set<PackageableElementPointer> prerequisiteElements, org.finos.legend.engine.protocol.pure.v1.model.packageableElement.mapping.aggregationAware.AggregateSpecification aggregateSpecification, CompileContext context)
    {
        ValueSpecificationPrerequisiteElementsPassBuilder valueSpecificationPrerequisiteElementsPassBuilder = new ValueSpecificationPrerequisiteElementsPassBuilder(context, prerequisiteElements);
        ListIterate.forEach(aggregateSpecification.groupByFunctions, gb -> gb.groupByFn.accept(valueSpecificationPrerequisiteElementsPassBuilder));
        ListIterate.forEach(aggregateSpecification.aggregateValues, af ->
        {
            af.mapFn.accept(valueSpecificationPrerequisiteElementsPassBuilder);
            af.aggregateFn.accept(valueSpecificationPrerequisiteElementsPassBuilder);
        });
    }

    private static GroupByFunctionSpecification processGroupByFunction(GroupByFunction groupByFunction, CompileContext context, MutableList<String> openVariables, ProcessingContext processingContext)
    {
        GroupByFunctionSpecification gb = new Root_meta_pure_mapping_aggregationAware_GroupByFunctionSpecification_Impl("", null, context.pureModel.getClass("meta::pure::mapping::aggregationAware::GroupByFunctionSpecification"));
        gb._groupByFn((org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.LambdaFunction) ((org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.InstanceValue) groupByFunction.groupByFn.accept(new ValueSpecificationBuilder(context, openVariables, processingContext)))._values().getFirst());
        return gb;
    }

    private static AggregationFunctionSpecification processAggregationFunction(AggregateFunction aggregateFunction, CompileContext context, MutableList<String> openVariables, ProcessingContext processingContext)
    {
        AggregationFunctionSpecification afs = new Root_meta_pure_mapping_aggregationAware_AggregationFunctionSpecification_Impl(" ");
        InstanceValue processed = (InstanceValue) aggregateFunction.mapFn.accept(new ValueSpecificationBuilder(context, openVariables, processingContext));
        afs._mapFn((org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.LambdaFunction) processed._values().getFirst());

        org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.ValueSpecification thisVariable = HelperModelBuilder.createVariableForMapped((org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.LambdaFunction) processed._values().getFirst(), context);
        processingContext.addInferredVariables("mapped", thisVariable);

        if (aggregateFunction.aggregateFn.parameters.size() > 0)
        {
            Variable variable = aggregateFunction.aggregateFn.parameters.get(0);
            variable.genericType = context.convertGenericType(Handlers.funcReturnType(processed, context.pureModel));
            variable.multiplicity = new Multiplicity(1, 1);
        }
        afs._aggregateFn((org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.LambdaFunction) ((InstanceValue) aggregateFunction.aggregateFn.accept(new ValueSpecificationBuilder(context, openVariables, processingContext)))._values().getFirst());
        return afs;
    }

    public static Property getMappedProperty(PropertyMapping propertyMapping, CompileContext context)
    {
        if (propertyMapping.localMappingProperty != null)
        {
            // Local property mapping
            LocalMappingPropertyInfo localMappingPropertyInfo = propertyMapping.localMappingProperty;

            GenericType sourceGenericType = new Root_meta_pure_metamodel_type_generics_GenericType_Impl("", null, context.pureModel.getClass(M3Paths.GenericType)); // Raw type will be populated when mapping class is built
            GenericType targetGenericType = new Root_meta_pure_metamodel_type_generics_GenericType_Impl("", null, context.pureModel.getClass(M3Paths.GenericType))
                    ._rawType(context.resolveType(localMappingPropertyInfo.type, localMappingPropertyInfo.sourceInformation));
            GenericType propertyClassifierGenericType = context.newGenericType(
                    context.pureModel.getType(M3Paths.Property),
                    Lists.fixedSize.of(sourceGenericType, targetGenericType),
                    Lists.fixedSize.of(context.pureModel.getMultiplicity(localMappingPropertyInfo.multiplicity))
            );

            return new Root_meta_pure_metamodel_function_property_Property_Impl<>(propertyMapping.property.property, SourceInformationHelper.toM3SourceInformation(propertyMapping.sourceInformation), null)
                    ._name(propertyMapping.property.property)
                    ._classifierGenericType(propertyClassifierGenericType)
                    ._genericType(targetGenericType)
                    ._multiplicity(context.pureModel.getMultiplicity(localMappingPropertyInfo.multiplicity));
        }

        PropertyOwner owner = context.resolvePropertyOwner(propertyMapping.property._class, propertyMapping.property.sourceInformation);
        Class<?> _class = owner instanceof Class<?> ? (Class<?>) owner : HelperModelBuilder.getAssociationPropertyClass((Association) owner, propertyMapping.property.property, propertyMapping.property.sourceInformation, context);
        return HelperModelBuilder.getPropertyOrResolvedEdgePointProperty(context, _class, Optional.empty(), propertyMapping.property.property, propertyMapping.property.sourceInformation);
    }

    public static void collectPrerequisiteElementsFromMappedProperty(Set<PackageableElementPointer> prerequisiteElements, PropertyMapping propertyMapping)
    {
        if (propertyMapping.localMappingProperty != null)
        {
            prerequisiteElements.add(new PackageableElementPointer(null, propertyMapping.localMappingProperty.type, propertyMapping.localMappingProperty.sourceInformation));
        }
        else
        {
            prerequisiteElements.add(new PackageableElementPointer(null, propertyMapping.property._class, propertyMapping.property.sourceInformation));
            prerequisiteElements.add(new PackageableElementPointer(null, propertyMapping.property.property, propertyMapping.property.sourceInformation));
        }
    }

    public static void buildMappingClassOutOfLocalProperties(SetImplementation setImplementation, RichIterable<? extends org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.PropertyMapping> propertyMappings, CompileContext context)
    {
        if (setImplementation instanceof InstanceSetImplementation && propertyMappings != null)
        {
            List<? extends org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.PropertyMapping> localPropertyMappings = ListIterate.select(
                    propertyMappings.toList(),
                    x -> x._localMappingProperty() != null && x._localMappingProperty()
            );

            if (!localPropertyMappings.isEmpty())
            {
                String mappingClassName = setImplementation._class().getName() + "_" + setImplementation._parent().getName() + "_" + setImplementation._id();

                final MappingClass mappingClass = new Root_meta_pure_mapping_MappingClass_Impl<>(mappingClassName);
                mappingClass._name(mappingClassName);
                GenericType classifierGenericType = context.newGenericType(
                        context.pureModel.getType("meta::pure::metamodel::type::Class"),
                        Lists.fixedSize.of(context.newGenericType(mappingClass))
                );
                mappingClass._classifierGenericType(classifierGenericType);

                GenericType superType = context.newGenericType(setImplementation._class());
                Generalization newGeneralization = new Root_meta_pure_metamodel_relationship_Generalization_Impl("", null, context.pureModel.getClass(M3Paths.Generalization))._specific(mappingClass)._general(superType);
                mappingClass._generalizations(Lists.immutable.with(newGeneralization));
                setImplementation._class()._specializationsAdd(newGeneralization);

                mappingClass._properties(ListIterate.collect(localPropertyMappings, pm ->
                {
                    Property property = pm._property();
                    property._owner(mappingClass);
                    property._classifierGenericType()._typeArguments().toList().get(0)._rawType(mappingClass);
                    return property;
                }));

                ((InstanceSetImplementation) setImplementation)._mappingClass(mappingClass);
            }
        }
    }


    public static Test processMappingTestAndTestSuite(org.finos.legend.engine.protocol.pure.v1.model.test.Test test, final org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.Mapping pureMapping, CompileContext context)
    {
        if (test instanceof MappingTestSuite)
        {
            // validate tests and test suite ids
            MappingTestSuite queryTestSuite = (MappingTestSuite) test;
            TestBuilderHelper.validateNonEmptySuite(queryTestSuite);
            TestBuilderHelper.validateTestIds(queryTestSuite.tests, queryTestSuite.sourceInformation);
            Root_meta_pure_mapping_metamodel_MappingTestSuite compiledMappingSuite = new Root_meta_pure_mapping_metamodel_MappingTestSuite_Impl("", SourceInformationHelper.toM3SourceInformation(test.sourceInformation), context.pureModel.getClass("meta::pure::mapping::metamodel::MappingTestSuite"));
            return compiledMappingSuite._id(queryTestSuite.id)
                ._query(HelperValueSpecificationBuilder.buildLambda(queryTestSuite.func, context))
                ._tests(ListIterate.collect(queryTestSuite.tests, unitTest -> (Root_meta_pure_test_AtomicTest) HelperMappingBuilder.processMappingTestAndTestSuite(unitTest, pureMapping, context)))
                ._testable(pureMapping);
        }
        else if (test instanceof MappingTest)
        {
            MappingTest mappingTest = (MappingTest) test;
            Root_meta_pure_test_AtomicTest pureMappingTest = (Root_meta_pure_test_AtomicTest) TestCompilerHelper.compilePureMappingTests(mappingTest, context, new ProcessingContext("Mapping Test '" + mappingTest.id + "' Second Pass"));
            TestBuilderHelper.validateNonEmptyTest(mappingTest);
            TestBuilderHelper.validateAssertionIds(mappingTest.assertions, mappingTest.sourceInformation);
            pureMappingTest._assertions(ListIterate.collect(mappingTest.assertions, assertion -> context.getCompilerExtensions().getExtraTestAssertionProcessors().stream()
                    .map(processor -> processor.value(assertion, context, new ProcessingContext("Test Assertion '" + assertion.id + "'")))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new UnsupportedOperationException("No Processors found for assertion: " + assertion.id))));
            pureMappingTest._testable(pureMapping);
            return pureMappingTest;
        }
        else
        {
            return null;
        }
    }

    public static void collectPrerequisiteElementsFromMappingTestAndTestSuite(Set<PackageableElementPointer> prerequisiteElements, org.finos.legend.engine.protocol.pure.v1.model.test.Test test, CompileContext context)
    {
        if (test instanceof MappingTestSuite)
        {
            MappingTestSuite queryTestSuite = (MappingTestSuite) test;
            queryTestSuite.func.accept(new ValueSpecificationPrerequisiteElementsPassBuilder(context, prerequisiteElements));
            ListIterate.forEach(queryTestSuite.tests, unitTest -> HelperMappingBuilder.collectPrerequisiteElementsFromMappingTestAndTestSuite(prerequisiteElements, unitTest, context));
        }
        else if (test instanceof MappingTest)
        {
            MappingTest mappingTest = (MappingTest) test;
            TestCompilerHelper.collectPrerequisiteElementsFromPureMappingTests(prerequisiteElements, mappingTest, context);
            ListIterate.forEach(mappingTest.assertions, assertion -> context.getCompilerExtensions().getExtraTestAssertionPrerequisiteElementsPassProcessors().forEach(processor -> processor.value(prerequisiteElements, assertion, context)));
        }
    }

    static Root_meta_pure_data_StoreTestData processMappingElementTestData(StoreTestData testData, CompileContext context, ProcessingContext processingContext)
    {
        Root_meta_pure_data_StoreTestData mappingStoreTestData = new Root_meta_pure_data_StoreTestData_Impl("", SourceInformationHelper.toM3SourceInformation(testData.sourceInformation), context.pureModel.getClass("meta::pure::data::StoreTestData"));
        mappingStoreTestData._data(context.getCompilerExtensions().getExtraEmbeddedDataProcessors().stream().map(processor -> processor.value(testData.data, context, processingContext))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Unsupported data")));
        if (testData.store.path.equals("ModelStore"))
        {
            mappingStoreTestData._store(new Root_meta_external_store_model_ModelStore_Impl(""));
        }
        else
        {
            mappingStoreTestData._store(context.resolveStore(testData.store.path, testData.store.sourceInformation));
        }
        return mappingStoreTestData;
    }

    static void collectPrerequisiteElementsFromMappingElementTestData(Set<PackageableElementPointer> prerequisiteElements, StoreTestData testData, CompileContext context)
    {
        context.getCompilerExtensions().getExtraEmbeddedDataPrerequisiteElementsPassProcessors().forEach(processor -> processor.value(prerequisiteElements, testData.data, context));
        if (!testData.store.path.equals("ModelStore"))
        {
            prerequisiteElements.add(testData.store);
        }
    }

    static ImmutableList<Store> getStoresFromMappingIgnoringIncludedMappings(Mapping mapping, CompileContext context)
    {
        ImmutableList<String> mappedStores = getUniqueMapStorePathsFromMapping(mapping, context);
        return mappedStores.collect(store -> store.equals("ModelStore") ?
            new Root_meta_external_store_model_ModelStore_Impl("", null, context.pureModel.getClass("meta::external::store::model::ModelStore"))
            : context.resolveStore(store));
    }

    static ImmutableList<String> getUniqueMapStorePathsFromMapping(Mapping mapping, CompileContext context)
    {
        MutableSet<String> mappedStores = Sets.mutable.empty();
        ListIterate.forEach(Root_meta_pure_mapping__allClassMappingsRecursive_Mapping_1__SetImplementation_MANY_(mapping, context.pureModel.getExecutionSupport()).toList(), setImplementation ->
        {
            context.getCompilerExtensions().getExtraSetImplementationSourceScanners().forEach(scanner -> scanner.value(setImplementation, mappedStores, context));
        });
        return mappedStores.toList().toImmutable();
    }
}
