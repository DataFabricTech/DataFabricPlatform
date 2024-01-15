package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.MariaDB.MariaDBMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;
import org.junit.jupiter.api.Test;


class MariaDBMetadataTest {

    @Test
    void extract() {
        //HashMap<String, String> map = new HashMap<>();
        var target = new TargetConfig();
        var mariadb = new MariaDBMetadata(target);
        try {
            mariadb.extractDefault();
            mariadb.extractAdditional();
            //Assertions.assertEquals("si",this.metadata.metadata.get("name"));
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }
    }
}