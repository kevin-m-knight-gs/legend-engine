package org.finos.legend.engine.code.core;

import org.finos.legend.pure.m3.tests.AbstractCompiledStateIntegrityTest;
import org.junit.BeforeClass;

public class TestCoreExternalQuerySQLMetamodelCompiledStateIntegrity extends AbstractCompiledStateIntegrityTest
{
    @BeforeClass
    public static void initialize()
    {
        initialize("core_external_query_sql_metamodel");
    }
}
