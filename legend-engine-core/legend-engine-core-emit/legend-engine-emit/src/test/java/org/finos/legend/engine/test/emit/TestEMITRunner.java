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

import org.finos.legend.engine.test.emit.EMITPhaseResult.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestEMITRunner
{
    @Test
    void classSimpleRunsThroughAllPhases()
    {
        Path emitYaml = resource("emit-models/basic/class-simple.emit.yaml");

        EMITResult result = new EMITRunner().runFromYaml(emitYaml);

        Assertions.assertTrue(result.isSuccess(), () -> "Expected EMIT run to succeed but got:\n" + result.getSummary());

        Assertions.assertEquals(Status.SUCCESS, result.getPhase(EMITPhase.INITIALIZATION).getStatus());
        Assertions.assertEquals(Status.SUCCESS, result.getPhase(EMITPhase.PARSE).getStatus());
        Assertions.assertEquals(Status.SUCCESS, result.getPhase(EMITPhase.COMPILE).getStatus());
        Assertions.assertEquals(Status.SKIPPED, result.getPhase(EMITPhase.MODEL_GENERATION).getStatus());
        Assertions.assertEquals(Status.SKIPPED, result.getPhase(EMITPhase.TEST_EXECUTION).getStatus());
        Assertions.assertEquals(Status.SKIPPED, result.getPhase(EMITPhase.PLAN_GENERATION).getStatus());
    }

    @Test
    void initializationFailureSkipsRemainingPhases()
    {
        Path missing = Paths.get("does-not-exist.emit.yaml");

        EMITResult result = new EMITRunner().runFromYaml(missing);

        Assertions.assertFalse(result.isSuccess(), "Expected failure on missing descriptor");
        Assertions.assertEquals(Status.FAILURE, result.getPhase(EMITPhase.INITIALIZATION).getStatus());
        Assertions.assertEquals(Status.NOT_RUN, result.getPhase(EMITPhase.PARSE).getStatus());
        Assertions.assertEquals(Status.NOT_RUN, result.getPhase(EMITPhase.COMPILE).getStatus());
        Assertions.assertEquals(Status.NOT_RUN, result.getPhase(EMITPhase.MODEL_GENERATION).getStatus());
        Assertions.assertEquals(Status.NOT_RUN, result.getPhase(EMITPhase.TEST_EXECUTION).getStatus());
        Assertions.assertEquals(Status.NOT_RUN, result.getPhase(EMITPhase.PLAN_GENERATION).getStatus());
    }

    private static Path resource(String name)
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        Assertions.assertNotNull(url, "test resource not found: " + name);
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
