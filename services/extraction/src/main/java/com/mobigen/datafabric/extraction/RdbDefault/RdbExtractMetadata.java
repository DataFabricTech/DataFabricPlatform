package com.mobigen.datafabric.extraction.RdbDefault;

import com.mobigen.datafabric.extraction.UserDefineException.ColumnWhileStoppedException;
import com.mobigen.datafabric.extraction.UserDefineException.DefaultWhileStoppedException;
import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.ExtractAdditional;
import com.mobigen.datafabric.extraction.model.Metadata;
import dto.ColumnMetadata;
import dto.ModelMetadata;
import org.apache.tika.exception.UnsupportedFormatException;

import java.sql.SQLException;
import java.util.List;

public class RdbExtractMetadata implements Extract, ExtractAdditional {

    RdbConnectInfo rdbConnectInfo = new RdbConnectInfo();

    public RdbExtractMetadata() throws SQLException {
    }

    @Override
    public Metadata extract() {
        try {
            extractDefault();
            extractAdditional();
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void extractDefault() {
            try {
                List<ModelMetadata> modelMetadataList = RdbDefaultExraction.DefaultExract(rdbConnectInfo.targetDbConnInfoList, rdbConnectInfo.extractInfoList, rdbConnectInfo.targetStmt, rdbConnectInfo.dbmetadata);

                for(var x : modelMetadataList){
                    System.out.println(x);
                }
            } catch(Exception e){
                throw new DefaultWhileStoppedException(e.getMessage());
            }
    }

    @Override
    public void extractAdditional() throws UnsupportedFormatException {
        try {
            List<ColumnMetadata> columnMetadataList = RdbColumnExtraction.extract(rdbConnectInfo.targetDbConn, rdbConnectInfo.targetStmt, rdbConnectInfo.targetDbConnInfoList, rdbConnectInfo.dbmetadata);

            for(var x : columnMetadataList){
                System.out.println(x);
            }
        } catch (Exception ex){
            throw new ColumnWhileStoppedException("RdbExtractionMetadata : " + ex.getMessage());
        }
    }
}
