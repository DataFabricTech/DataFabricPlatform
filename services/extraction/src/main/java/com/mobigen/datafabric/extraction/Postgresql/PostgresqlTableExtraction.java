package com.mobigen.datafabric.extraction.Postgresql;

import com.mobigen.datafabric.extraction.RdbDefault.RdbTableExtraction;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.Connection;

public class PostgresqlTableExtraction extends RdbTableExtraction {
    public PostgresqlTableExtraction(TargetConfig target, Connection conn) throws ClassNotFoundException {
        super(target, conn);
    }
}
