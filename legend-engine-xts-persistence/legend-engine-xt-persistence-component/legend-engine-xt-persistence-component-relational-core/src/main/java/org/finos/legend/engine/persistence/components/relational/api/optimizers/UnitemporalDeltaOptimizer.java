// Copyright 2025 Goldman Sachs
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

package org.finos.legend.engine.persistence.components.relational.api.optimizers;

import org.finos.legend.engine.persistence.components.common.Datasets;
import org.finos.legend.engine.persistence.components.common.OptimizationFilter;
import org.finos.legend.engine.persistence.components.ingestmode.UnitemporalDelta;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.finos.legend.engine.persistence.components.util.LogicalPlanUtils.SUPPORTED_DATA_TYPES_FOR_OPTIMIZATION_COLUMNS;
import static org.finos.legend.engine.persistence.components.util.LogicalPlanUtils.findCommonPrimaryFieldsBetweenMainAndStaging;

public class UnitemporalDeltaOptimizer
{
    private UnitemporalDelta unitemporalDelta;
    private Datasets datasets;

    public UnitemporalDeltaOptimizer(UnitemporalDelta unitemporalDelta, Datasets datasets)
    {
        this.unitemporalDelta = unitemporalDelta;
        this.datasets = datasets;
    }

    public UnitemporalDelta optimize()
    {
        return UnitemporalDelta
            .builder()
            .digestField(unitemporalDelta.digestField())
            .addAllOptimizationFilters(deriveOptimizationFilters())
            .transactionMilestoning(unitemporalDelta.transactionMilestoning())
            .mergeStrategy(unitemporalDelta.mergeStrategy())
            .versioningStrategy(unitemporalDelta.versioningStrategy())
            .deduplicationStrategy(unitemporalDelta.deduplicationStrategy())
            .build();
    }

    private List<OptimizationFilter> deriveOptimizationFilters()
    {
        List<OptimizationFilter> optimizationFilters = new ArrayList<>();
        List<Field> primaryKeys = findCommonPrimaryFieldsBetweenMainAndStaging(datasets.mainDataset(), datasets.stagingDataset());
        List<Field> comparablePrimaryKeys = primaryKeys.stream().filter(field -> SUPPORTED_DATA_TYPES_FOR_OPTIMIZATION_COLUMNS.contains(field.type().dataType())).collect(Collectors.toList());
        for (Field field : comparablePrimaryKeys)
        {
            OptimizationFilter filter = OptimizationFilter.of(field.name());
            optimizationFilters.add(filter);
        }
        return optimizationFilters;
    }
}
