package com.mobigen.datafabric.extraction.Mariadb;

import com.mobigen.datafabric.extraction.RdbDefault.RdbTableExtraction;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.Connection;

public class MariadbTableExtraction extends RdbTableExtraction {

    public MariadbTableExtraction(TargetConfig target, Connection conn) throws ClassNotFoundException {
        super(target, conn);
    }

}
