// Copyright 2023 Goldman Sachs
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

package org.finos.legend.engine.plan.execution.nodes.helpers.platform;

import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.JavaClass;
import org.junit.Assert;
import org.junit.Test;

public class TestJavaHelper
{
    @Test
    public void testGetJavaClassFullName()
    {
        Assert.assertEquals("MyClass", JavaHelper.getJavaClassFullName(newJavaClass(null, "MyClass")));
        Assert.assertEquals("MyClass", JavaHelper.getJavaClassFullName(newJavaClass("", "MyClass")));
        Assert.assertEquals("a.b.c.MyClass", JavaHelper.getJavaClassFullName(newJavaClass("a.b.c", "MyClass")));
        Assert.assertEquals("w.x.y.z.YourClass", JavaHelper.getJavaClassFullName(newJavaClass("w.x.y.z", "YourClass")));
        Assert.assertEquals("pkg.ClassName", JavaHelper.getJavaClassFullName(newJavaClass("pkg", "ClassName")));
    }

    private JavaClass newJavaClass(String pkg, String name)
    {
        JavaClass javaClass = new JavaClass();
        javaClass._package = pkg;
        javaClass.name = name;
        return javaClass;
    }
}
