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

package org.finos.legend.engine.test.emit.persistence;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.test.emit.junit.EMITTestSuiteBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

/**
 * Guard for the JUnit integration ({@link EMITTestSuiteBuilder}).
 *
 * <p>When {@code findTestCandidates} returns nothing for a model, the builder
 * simply omits the {@code Test} container — no failing leaf is produced, so a
 * persistence model whose suite-less tests stopped being discovered would
 * "pass" by emitting no test task at all (see {@link PersistenceEMITTests}).
 *
 * <p>This test fails in that scenario: it asserts the builder emits a Test task
 * for the suite-less persistence atomic test, that the task name carries no
 * suite segment, and that executing the task actually runs (and passes) the
 * persistence test.
 */
public class TestPersistenceEMITTestSuiteBuilder
{
    @Test
    void persistenceModelYieldsAnExecutableSuiteLessTestTask() throws Throwable
    {
        MutableList<DynamicTest> tasks = Lists.mutable.fromStream(
                EMITTestSuiteBuilder.tests("emit-models/", "persistence-snapshot"));

        // The Test task name is "[<model>] Test: <testablePath> / <atomicTestId>" — note there is no
        // " / <suiteId> " segment, because persistence tests are not organized into a suite.
        MutableList<DynamicTest> testTasks = tasks.select(t -> t.getDisplayName().startsWith("[persistence-snapshot] Test:"));
        Assertions.assertEquals(
                Lists.mutable.with("[persistence-snapshot] Test: test::TestPersistence / test1"),
                testTasks.collect(DynamicTest::getDisplayName),
                () -> tasks.makeString("Expected exactly one suite-less persistence Test task; got all tasks:", "\n  - ", "\n"));

        // Executing the task actually drives the persistence test runner against H2; it must pass.
        for (DynamicTest task : testTasks)
        {
            task.getExecutable().execute();
        }
    }
}
