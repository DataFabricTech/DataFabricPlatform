package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.Postgresql.PostgresqlMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;
import org.junit.jupiter.api.Test;


class PostgresqlMetadataTest {

    @Test
    void extract() throws UnsupportedFormatException, ClassNotFoundException {
        var target = new TargetConfig();
        var postgres = new PostgresqlMetadata(target);
        postgres.extractDefault();
        postgres.extractAdditional();
    }
//
//    @Test
//    void extractDefault() {
//        //HashMap<String, String> map = new HashMap<>();
//        var target = new TargetConfig();
//        var postgres = new PostgreSQLMetadata(target);
//        try {
//            postgres.extractDefault();
//            //Assertions.assertEquals("si",this.metadata.metadata.get("name"));
//        } catch (UnsupportedFormatException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    void extractAdditional() {
//        //HashMap<String, String> map = new HashMap<>();
//        var target = new TargetConfig();
//        var postgres = new PostgreSQLMetadata(target);
//        try {
//            postgres.extractAdditional();
//            //Assertions.assertEquals("si",this.metadata.metadata.get("name"));
//        } catch (UnsupportedFormatException e) {
//            throw new RuntimeException(e);
//        }
//    }
}