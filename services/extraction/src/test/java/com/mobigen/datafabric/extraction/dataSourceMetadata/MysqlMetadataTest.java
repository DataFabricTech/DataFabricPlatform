package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.Mysql.MysqlMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;
import org.junit.jupiter.api.Test;

class MysqlMetadataTest {

    @Test
    void extract() throws ClassNotFoundException {
        var target = new TargetConfig();
        var mysql = new MysqlMetadata(target);

        try {
            mysql.extractDefault();
            mysql.extractAdditional();
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }
    }
}