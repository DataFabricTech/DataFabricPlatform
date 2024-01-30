package com.mobigen.datafabric.extraction.dataSourceMetadata;

import com.mobigen.datafabric.extraction.Mysql.MysqlMetadata;
import com.mobigen.datafabric.extraction.UserDefineException.ColumnWhileStoppedException;
import com.mobigen.datafabric.extraction.UserDefineException.DefaultWhileStoppedException;
import com.mobigen.datafabric.extraction.UserDefineException.UserDefineExceptionMessage;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class MysqlMetadataTest {
    @Test
    void extract() throws SQLException {
        var mysql = new MysqlMetadata();

        try {
            mysql.extractDefault();
        } catch (Exception e) {
            System.out.println(UserDefineExceptionMessage.DEFAULT_ERROR_MESSAGE);
            throw new DefaultWhileStoppedException(e.getMessage());
        }
        try {
            mysql.extractAdditional();
        } catch (Exception ex) {
            System.out.println(UserDefineExceptionMessage.COLUMN_ERROR_MESSAGE);
            throw new ColumnWhileStoppedException(ex.getMessage());
        }
    }
}