package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.MySQL.MySQLMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;
import org.junit.jupiter.api.Test;

class MySQLMetadataTest {

    @Test
    void extract() {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        var target = new TargetConfig();
        var mysql = new MySQLMetadata(target);

        try {
            mysql.extractDefault();
            mysql.extractAdditional();
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }
    }
}