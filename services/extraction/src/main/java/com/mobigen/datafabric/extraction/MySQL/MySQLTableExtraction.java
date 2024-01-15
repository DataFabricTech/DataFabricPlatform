package com.mobigen.datafabric.extraction.MySQL;

import com.mobigen.datafabric.extraction.RDBDefault.RDBDefaultTableExtraction;
import com.mobigen.datafabric.extraction.extraction.Extract;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class MySQLTableExtraction extends RDBDefaultTableExtraction {
    public MySQLTableExtraction(TargetConfig target) {
        super(target);
    }
}
