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

package org.finos.legend.engine.plan.platform.java;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.CompositeExecutionPlan;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.ExecutionPlan;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.SingleExecutionPlan;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.JavaClass;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.JavaPlatformImplementation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;

public class JavaSourceHelper
{
    public static String toValidJavaIdentifier(String string)
    {
        return toValidJavaIdentifier(string, true);
    }

    public static String toValidJavaIdentifier(String string, boolean compressAdjacentReplacements)
    {
        return toValidJavaIdentifier_internal(string, '_', compressAdjacentReplacements);
    }

    public static String toValidJavaIdentifier(String string, char replacement)
    {
        return toValidJavaIdentifier(string, replacement, true);
    }

    public static String toValidJavaIdentifier(String string, char replacement, boolean compressAdjacentReplacements)
    {
        if (!Character.isJavaIdentifierPart(replacement))
        {
            throw new IllegalArgumentException("Invalid replacement character: " + replacement);
        }
        return toValidJavaIdentifier_internal(string, replacement, compressAdjacentReplacements);
    }

    private static String toValidJavaIdentifier_internal(String string, char replacement, boolean compressAdjacentReplacements)
    {
        int length = string.length();
        if (length == 0)
        {
            return String.valueOf(replacement);
        }

        if (SourceVersion.isKeyword(string))
        {
            return Character.isJavaIdentifierStart(replacement) ? (replacement + string) : (string + replacement);
        }

        StringBuilder builder = null;
        int start = 0;
        int index = 0;

        // Handle the first code point
        int cp = string.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp))
        {
            builder = new StringBuilder(length + 1).append(replacement);
            index += Character.charCount(cp);
            if (!Character.isJavaIdentifierPart(cp))
            {
                start = index;
            }
        }

        // Handle the rest
        while (index < length)
        {
            cp = string.codePointAt(index);
            if (Character.isJavaIdentifierPart(cp))
            {
                index += Character.charCount(cp);
            }
            else
            {
                if (builder == null)
                {
                    builder = new StringBuilder(length);
                }
                if (start < index)
                {
                    builder.append(string, start, index).append(replacement);
                }
                else if (!compressAdjacentReplacements)
                {
                    builder.append(replacement);
                }
                index += Character.charCount(cp);
                start = index;
            }
        }
        if (builder == null)
        {
            return string;
        }
        if (start < length)
        {
            builder.append(string, start, length);
        }
        return builder.toString();
    }

    public static void writeJavaSourceFiles(Path sourceDirectory, ExecutionPlan... plans)
    {
        writeJavaSourceFiles(sourceDirectory, Arrays.stream(plans));
    }

    public static void writeJavaSourceFiles(Path sourceDirectory, Stream<? extends ExecutionPlan> plans)
    {
        forEachJavaClassByRelativeFilePath(plans, sourceDirectory.getFileSystem().getSeparator(), (relativePath, source) ->
        {
            try
            {
                Path path = sourceDirectory.resolve(relativePath);
                byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
                if (Files.notExists(path))
                {
                    // file does not exist: create it (including directories)
                    Files.createDirectories(path.getParent());
                    Files.write(path, bytes);
                }
                else
                {
                    // file exists: check that the content is the same as we expect
                    if (!fileHasContent(path, bytes))
                    {
                        throw new RuntimeException("conflicting sources for " + relativePath);
                    }
                }
            }
            catch (Exception e)
            {
                StringBuilder builder = new StringBuilder("Error trying to write ").append(relativePath).append(" to ").append(sourceDirectory);
                String eMessage = e.getMessage();
                if (eMessage != null)
                {
                    builder.append(": ").append(eMessage);
                }
                throw new RuntimeException(builder.toString(), e);
            }
        });
    }

    public static Map<String, String> getJavaSourceCodeByRelativeFilePath(ExecutionPlan... plans)
    {
        return getJavaSourceCodeByRelativeFilePath(Arrays.stream(plans));
    }

    public static Map<String, String> getJavaSourceCodeByRelativeFilePath(Stream<? extends ExecutionPlan> plans)
    {
        return getJavaSourceCodeByRelativeFilePath(plans, "/");
    }

    public static Map<String, String> getJavaSourceCodeByRelativeFilePath(Stream<? extends ExecutionPlan> plans, String pathSeparator)
    {
        MutableMap<String, String> map = Maps.mutable.empty();
        MutableSortedMap<String, MutableSet<String>> conflicts = SortedMaps.mutable.empty();
        forEachJavaClassByRelativeFilePath(plans, pathSeparator, (path, source) ->
        {
            String current = map.getIfAbsentPut(path, source);
            if (!source.equals(current))
            {
                conflicts.getIfAbsentPut(path, () -> Sets.mutable.with(current)).add(source);
            }
        });
        if (conflicts.notEmpty())
        {
            StringBuilder builder = new StringBuilder("Conflicting sources for ");
            int length = builder.length();
            conflicts.forEachKeyValue((path, sources) -> ((builder.length() == length) ? builder : builder.append(", ")).append(path).append(" (").append(sources.size()).append(")"));
            throw new RuntimeException(builder.toString());
        }
        return map;
    }

    public static void removeJavaImplementationClasses(ExecutionPlan... plans)
    {
        removeJavaImplementationClasses(Arrays.stream(plans));
    }

    public static void removeJavaImplementationClasses(Stream<? extends ExecutionPlan> plans)
    {
        plans.flatMap(JavaSourceHelper::getSingleExecutionPlanStream)
                .flatMap(JavaSourceHelper::getJavaPlatformImplementationStream)
                .forEach(JavaSourceHelper::removeClasses);
    }

    private static void forEachJavaClassByRelativeFilePath(Stream<? extends ExecutionPlan> plans, String pathSeparator, BiConsumer<String, String> consumer)
    {
        plans.flatMap(JavaSourceHelper::getJavaClassStream).forEach(jc -> consumer.accept(getJavaFileRelativePath(jc, pathSeparator), jc.source));
    }

    private static Stream<JavaClass> getJavaClassStream(ExecutionPlan plan)
    {
        return getSingleExecutionPlanStream(plan)
                .flatMap(JavaSourceHelper::getJavaPlatformImplementationStream)
                .flatMap(JavaSourceHelper::getJavaClassStream);
    }

    private static Stream<SingleExecutionPlan> getSingleExecutionPlanStream(ExecutionPlan plan)
    {
        if (plan instanceof SingleExecutionPlan)
        {
            return Stream.of((SingleExecutionPlan) plan);
        }
        if (plan instanceof CompositeExecutionPlan)
        {
            return ((CompositeExecutionPlan) plan).executionPlans.values().stream();
        }
        throw new IllegalArgumentException("Unsupported execution plan: " + plan);
    }

    private static Stream<JavaPlatformImplementation> getJavaPlatformImplementationStream(SingleExecutionPlan plan)
    {
        Stream<JavaPlatformImplementation> streamFromGlobalImpl = (plan.globalImplementationSupport instanceof JavaPlatformImplementation) ? Stream.of((JavaPlatformImplementation) plan.globalImplementationSupport) : null;
        Stream<JavaPlatformImplementation> streamFromRootNode = getJavaPlatformImplementationStream(plan.rootExecutionNode);
        return (streamFromGlobalImpl == null) ? streamFromRootNode : ((streamFromRootNode == null) ? streamFromGlobalImpl : Stream.concat(streamFromGlobalImpl, streamFromRootNode));
    }

    private static Stream<JavaPlatformImplementation> getJavaPlatformImplementationStream(ExecutionNode node)
    {
        Stream<JavaPlatformImplementation> streamFromImpl = (node.implementation instanceof JavaPlatformImplementation) ? Stream.of((JavaPlatformImplementation) node.implementation) : null;
        Stream<JavaPlatformImplementation> streamFromNodes = ((node.executionNodes == null) || node.executionNodes.isEmpty()) ? null : node.executionNodes.stream().flatMap(JavaSourceHelper::getJavaPlatformImplementationStream);
        return (streamFromImpl == null) ? streamFromNodes : ((streamFromNodes == null) ? streamFromImpl : Stream.concat(streamFromImpl, streamFromNodes));
    }

    private static Stream<JavaClass> getJavaClassStream(JavaPlatformImplementation javaPlatformImpl)
    {
        if ((javaPlatformImpl.code != null) || (javaPlatformImpl.compiledClasses != null) || (javaPlatformImpl.byteCode != null))
        {
            throw new RuntimeException("Unsupported JavaPlatformImplementation: " + javaPlatformImpl);
        }
        return (javaPlatformImpl.classes == null) ? null : javaPlatformImpl.classes.stream();
    }

    private static void removeClasses(JavaPlatformImplementation javaPlatformImpl)
    {
        javaPlatformImpl.code = null;
        javaPlatformImpl.compiledClasses = null;
        javaPlatformImpl.byteCode = null;
        javaPlatformImpl.classes = null;
    }

    public static Path getJavaFilePath(JavaClass javaClass, Path root)
    {
        return root.resolve(getJavaFileRelativePath(javaClass, root.getFileSystem().getSeparator()));
    }

    public static String getJavaFileRelativePath(JavaClass javaClass, String separator)
    {
        if ((javaClass._package == null) || javaClass._package.isEmpty())
        {
            return javaClass.name + JavaFileObject.Kind.SOURCE.extension;
        }
        if (separator.length() == 1)
        {
            return javaClass._package.replace('.', separator.charAt(0)) + separator + javaClass.name + JavaFileObject.Kind.SOURCE.extension;
        }
        return javaClass._package.replace(".", separator) + separator + javaClass.name + JavaFileObject.Kind.SOURCE.extension;
    }

    public static String getJavaClassName(JavaClass javaClass)
    {
        return ((javaClass._package == null) || javaClass._package.isEmpty()) ?
                javaClass.name :
                (javaClass._package + "." + javaClass.name);
    }

    private static boolean fileHasContent(Path path, byte[] content) throws IOException
    {
        return (Files.size(path) == content.length) && Arrays.equals(content, Files.readAllBytes(path));
    }
}
