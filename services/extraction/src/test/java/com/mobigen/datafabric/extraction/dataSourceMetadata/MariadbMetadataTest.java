package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.Mariadb.MariadbMetadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;
import org.junit.jupiter.api.Test;


class MariadbMetadataTest {

    @Test
    void extract() throws ClassNotFoundException {
        var target = new TargetConfig();
        var mariadb = new MariadbMetadata(target);
        try {
            mariadb.extractDefault();
            mariadb.extractAdditional();
            //Assertions.assertEquals("si",this.metadata.metadata.get("name"));
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }
    }
}