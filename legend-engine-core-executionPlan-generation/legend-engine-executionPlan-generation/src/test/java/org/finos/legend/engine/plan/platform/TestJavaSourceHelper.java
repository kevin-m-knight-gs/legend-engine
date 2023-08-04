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

package org.finos.legend.engine.plan.platform;

import org.finos.legend.engine.plan.platform.java.JavaSourceHelper;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.JavaClass;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestJavaSourceHelper
{
    @Test
    public void testToValidJavaIdentifier()
    {
        for (String validIdentifier : new String[]{"validIdentifier", "a", "bcd", "e05___"})
        {
            Assert.assertSame(validIdentifier, JavaSourceHelper.toValidJavaIdentifier(validIdentifier));
            Assert.assertSame(validIdentifier, JavaSourceHelper.toValidJavaIdentifier(validIdentifier, '$'));
            Assert.assertSame(validIdentifier, JavaSourceHelper.toValidJavaIdentifier(validIdentifier, '_', true));
        }

        Assert.assertEquals("_", JavaSourceHelper.toValidJavaIdentifier(""));
        Assert.assertEquals("$", JavaSourceHelper.toValidJavaIdentifier("", '$'));
        Assert.assertEquals("_assert", JavaSourceHelper.toValidJavaIdentifier("assert"));
        Assert.assertEquals("$assert", JavaSourceHelper.toValidJavaIdentifier("assert", '$'));
        Assert.assertEquals("assert5", JavaSourceHelper.toValidJavaIdentifier("assert", '5'));
        Assert.assertEquals("a_b_c", JavaSourceHelper.toValidJavaIdentifier("a.b.c"));
        Assert.assertEquals("a_b_c", JavaSourceHelper.toValidJavaIdentifier("a-b-c"));
        Assert.assertEquals("_3abc", JavaSourceHelper.toValidJavaIdentifier("3abc"));
        Assert.assertEquals("_33abc", JavaSourceHelper.toValidJavaIdentifier("33abc"));
        Assert.assertEquals("a__b__c", JavaSourceHelper.toValidJavaIdentifier("a..b..c", false));
        Assert.assertEquals("a_b_c", JavaSourceHelper.toValidJavaIdentifier("a..b..c", true));
        Assert.assertEquals("abc$d$e", JavaSourceHelper.toValidJavaIdentifier("abc..d.#.e", '$', true));
        Assert.assertEquals("abc_def_ghi", JavaSourceHelper.toValidJavaIdentifier("abc::def::ghi", true));
        Assert.assertEquals("_3abc", JavaSourceHelper.toValidJavaIdentifier("3abc"));
        Assert.assertEquals("_3", JavaSourceHelper.toValidJavaIdentifier("3"));
        Assert.assertEquals("$", JavaSourceHelper.toValidJavaIdentifier("%", '$'));

        // Two unicode characters U+2200 (logical for all) and U+2203 (logical there exists)
        Assert.assertEquals("abcdefg$$hij", JavaSourceHelper.toValidJavaIdentifier("abcdefg\u2200\u2203hij", '$', false));

        // Single supplementary unicode character: U+1F729 (alchemical symbol for tin ore); represented as a sequence of two 16-bit characters: U+D83D U+DF29
        Assert.assertEquals("abcdefg$hij", JavaSourceHelper.toValidJavaIdentifier("abcdefg\uD83D\uDF29hij", '$', false));
    }

    @Test
    public void testGetJavaRelativeFilePath()
    {
        Assert.assertEquals("MyClass.java", JavaSourceHelper.getJavaFileRelativePath(newJavaClass(null, "MyClass"), "/"));
        Assert.assertEquals("MyClass.java", JavaSourceHelper.getJavaFileRelativePath(newJavaClass("", "MyClass"), "/"));
        Assert.assertEquals("a/b/c/MyClass.java", JavaSourceHelper.getJavaFileRelativePath(newJavaClass("a.b.c", "MyClass"), "/"));
        Assert.assertEquals("a\\b\\c\\MyClass.java", JavaSourceHelper.getJavaFileRelativePath(newJavaClass("a.b.c", "MyClass"), "\\"));
        Assert.assertEquals("w_x_y_z_YourClass.java", JavaSourceHelper.getJavaFileRelativePath(newJavaClass("w.x.y.z", "YourClass"), "_"));
    }

    @Test
    public void testGetJavaFilePath()
    {
        Path root = Paths.get(".");
        Assert.assertEquals(root.resolve("MyClass.java"), JavaSourceHelper.getJavaFilePath(newJavaClass(null, "MyClass"), root));
        Assert.assertEquals(root.resolve("MyClass.java"), JavaSourceHelper.getJavaFilePath(newJavaClass("", "MyClass"), root));
        Assert.assertEquals(root.resolve(Paths.get("a", "b", "c", "MyClass.java")), JavaSourceHelper.getJavaFilePath(newJavaClass("a.b.c", "MyClass"), root));
        Assert.assertEquals(root.resolve(Paths.get("w", "x", "y", "z", "YourClass.java")), JavaSourceHelper.getJavaFilePath(newJavaClass("w.x.y.z", "YourClass"), root));
    }

    @Test
    public void testGetJavaClassName()
    {
        Assert.assertEquals("MyClass", JavaSourceHelper.getJavaClassName(newJavaClass(null, "MyClass")));
        Assert.assertEquals("MyClass", JavaSourceHelper.getJavaClassName(newJavaClass("", "MyClass")));
        Assert.assertEquals("a.b.c.MyClass", JavaSourceHelper.getJavaClassName(newJavaClass("a.b.c", "MyClass")));
        Assert.assertEquals("w.x.y.z.YourClass", JavaSourceHelper.getJavaClassName(newJavaClass("w.x.y.z", "YourClass")));
    }

    private JavaClass newJavaClass(String pkg, String name)
    {
        return newJavaClass(pkg, name, null, null);
    }

    private JavaClass newJavaClass(String pkg, String name, String source, String bytecode)
    {
        JavaClass javaClass = new JavaClass();
        javaClass._package = pkg;
        javaClass.name = name;
        javaClass.source = source;
        javaClass.byteCode = bytecode;
        return javaClass;
    }
}
