package com.mobigen.datafabric.extraction.Mysql;

import com.mobigen.datafabric.extraction.RdbDefault.RdbTableExtraction;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.Connection;

public class MysqlTableExtraction extends RdbTableExtraction {
    public MysqlTableExtraction(TargetConfig target, Connection conn) throws ClassNotFoundException {
        super(target, conn);
    }
}