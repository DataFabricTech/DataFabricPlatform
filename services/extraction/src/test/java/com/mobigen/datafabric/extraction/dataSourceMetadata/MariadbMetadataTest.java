package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.Mariadb.MariadbMetadata;
import com.mobigen.datafabric.extraction.UserDefineException.ColumnWhileStoppedException;
import com.mobigen.datafabric.extraction.UserDefineException.DefaultWhileStoppedException;
import com.mobigen.datafabric.extraction.UserDefineException.UserDefineExceptionMessage;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class MariadbMetadataTest {
    @Test
    void extract() throws SQLException {
        var mariadb = new MariadbMetadata();

        try {
            mariadb.extractDefault();
        } catch (Exception e) {
            System.out.println(UserDefineExceptionMessage.DEFAULT_ERROR_MESSAGE);
            throw new DefaultWhileStoppedException(e.getMessage());
        }
        try {
            mariadb.extractAdditional();
        } catch (Exception ex) {
            System.out.println(UserDefineExceptionMessage.COLUMN_ERROR_MESSAGE.getErrorMessage());
            throw new ColumnWhileStoppedException(ex.getMessage());
        }
    }
}