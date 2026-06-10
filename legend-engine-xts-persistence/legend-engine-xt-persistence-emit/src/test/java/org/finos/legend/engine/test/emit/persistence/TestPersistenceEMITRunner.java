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

import org.finos.legend.engine.protocol.pure.v1.model.test.result.TestExecuted;
import org.finos.legend.engine.protocol.pure.v1.model.test.result.TestExecutionStatus;
import org.finos.legend.engine.protocol.pure.v1.model.test.result.TestResult;
import org.finos.legend.engine.test.emit.EMITPhase;
import org.finos.legend.engine.test.emit.EMITPhaseResult;
import org.finos.legend.engine.test.emit.EMITResult;
import org.finos.legend.engine.test.emit.EMITRunner;
import org.finos.legend.engine.testable.model.RunTestsResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Guard for the standalone {@link EMITRunner} (the "main" EMIT framework).
 *
 * <p>The {@code persistence-snapshot} model carries a Persistence Testable
 * whose tests are top-level atomic tests (a {@code tests} block, not a
 * {@code testSuites} block). If EMIT's discovery ever stops recognizing
 * suite-less Testable tests — for instance because a runner extension does not
 * advertise {@code isTestable} — {@code findTestableInputs} returns empty and
 * {@code EMITRunner} marks TEST_EXECUTION as {@code SKIPPED}. A SKIPPED phase
 * still leaves {@link EMITResult#isSuccess()} {@code true}, so the model would
 * pass having run zero tests.
 *
 * <p>This test fails in exactly that scenario: it asserts TEST_EXECUTION
 * actually ran ({@code SUCCESS}, not {@code SKIPPED}) and that at least one
 * persistence test was executed and passed.
 */
public class TestPersistenceEMITRunner
{
    @Test
    void persistenceModelActuallyRunsSuiteLessTests()
    {
        EMITResult result = new EMITRunner().runFromYaml(resource("emit-models/persistence-snapshot.emit.yaml"));

        EMITPhaseResult testPhase = result.getPhase(EMITPhase.TEST_EXECUTION);
        Assertions.assertNotNull(testPhase, () -> "TEST_EXECUTION phase is missing\n" + result.getSummary());

        // The core guard: discovery must NOT have silently skipped the persistence Testable.
        Assertions.assertEquals(EMITPhaseResult.Status.SUCCESS, testPhase.getStatus(),
                () -> "TEST_EXECUTION must run the persistence test, not SKIP it. "
                        + "A SKIPPED phase here means the suite-less Testable was never discovered.\n" + result.getSummary());

        // ... and at least one persistence test must have actually executed.
        RunTestsResult runResult = (RunTestsResult) testPhase.getOutputs().stream()
                .filter(o -> o instanceof RunTestsResult)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No RunTestsResult in TEST_EXECUTION outputs\n" + result.getSummary()));
        Assertions.assertFalse(runResult.results.isEmpty(),
                () -> "Expected at least one persistence test result, but none ran\n" + result.getSummary());

        for (TestResult tr : runResult.results)
        {
            // Persistence tests are top-level atomic tests, so there is no enclosing suite.
            Assertions.assertTrue(tr.testSuiteId == null || tr.testSuiteId.isEmpty(),
                    () -> "Persistence tests are suite-less; expected a null/empty testSuiteId but got: " + tr.testSuiteId);
            Assertions.assertInstanceOf(TestExecuted.class, tr,
                    () -> "Expected an executed persistence test but got " + tr.getClass().getSimpleName() + "\n" + result.getSummary());
            Assertions.assertEquals(TestExecutionStatus.PASS, ((TestExecuted) tr).testExecutionStatus,
                    () -> "Persistence test " + tr.atomicTestId + " did not pass\n" + result.getSummary());
        }

        Assertions.assertTrue(result.isSuccess(), () -> "EMIT pipeline failed:\n" + result.getSummary());
    }

    private static Path resource(String name)
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        Assertions.assertNotNull(url, () -> "test resource not found: " + name);
        try
        {
            return Paths.get(url.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }
}
