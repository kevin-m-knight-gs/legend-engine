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

package org.finos.legend.engine.language.snowflakeApp.api;

import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.functionActivator.validation.FunctionActivatorError;

public class SnowflakeAppError extends FunctionActivatorError
{
    public MutableList<String> foundSQLs;

    public SnowflakeAppError(String message, MutableList<String> foundSQLs)
    {
        super(message);
        this.foundSQLs = foundSQLs;
    }

    public SnowflakeAppError(String message)
    {
        super(message);
    }
}
