package com.mobigen.datafabric.extraction.PostgreSQL;

import com.mobigen.datafabric.extraction.RDBDefault.RDBDefaultTableExtraction;
import com.mobigen.datafabric.extraction.model.TargetConfig;

public class PostgresTableExtraction extends RDBDefaultTableExtraction {

    public PostgresTableExtraction(TargetConfig target) {
        super(target);
    }
}
