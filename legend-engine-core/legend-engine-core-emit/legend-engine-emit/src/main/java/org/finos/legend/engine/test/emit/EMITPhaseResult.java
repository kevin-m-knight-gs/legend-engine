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

public class EMITPhaseResult
{
    public enum Status
    {
        SUCCESS,
        FAILURE,
        SKIPPED,
        NOT_RUN
    }

    private final EMITPhase phase;
    private final Status status;
    private final long durationMs;
    private final String message;
    private final Exception exception;
    private final Object output;

    private EMITPhaseResult(EMITPhase phase, Status status, long durationMs, String message, Exception exception, Object output)
    {
        this.phase = phase;
        this.status = status;
        this.durationMs = durationMs;
        this.message = message;
        this.exception = exception;
        this.output = output;
    }

    public EMITPhase getPhase()
    {
        return this.phase;
    }

    public Status getStatus()
    {
        return this.status;
    }

    public long getDurationMs()
    {
        return this.durationMs;
    }

    public String getMessage()
    {
        return this.message;
    }

    public Exception getException()
    {
        return this.exception;
    }

    public Object getOutput()
    {
        return this.output;
    }

    public static EMITPhaseResult success(EMITPhase phase, long durationMs, String message, Object output)
    {
        return new EMITPhaseResult(phase, Status.SUCCESS, durationMs, message, null, output);
    }

    public static EMITPhaseResult failure(EMITPhase phase, long durationMs, String message)
    {
        return failure(phase, durationMs, message, null);
    }

    public static EMITPhaseResult failure(EMITPhase phase, long durationMs, String message, Exception exception)
    {
        return failure(phase, durationMs, message, exception, null);
    }

    public static EMITPhaseResult failure(EMITPhase phase, long durationMs, String message, Exception exception, Object output)
    {
        return new EMITPhaseResult(phase, Status.FAILURE, durationMs, message, exception, output);
    }

    public static EMITPhaseResult skipped(EMITPhase phase, String reason)
    {
        return new EMITPhaseResult(phase, Status.SKIPPED, 0L, reason, null, null);
    }

    public static EMITPhaseResult notRun(EMITPhase phase, String reason)
    {
        return new EMITPhaseResult(phase, Status.NOT_RUN, 0L, reason, null, null);
    }

    public boolean isSuccess()
    {
        return this.status == Status.SUCCESS || this.status == Status.SKIPPED;
    }

    public boolean isFailure()
    {
        return this.status == Status.FAILURE;
    }
}
