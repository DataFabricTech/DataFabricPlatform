package com.mobigen.datafabric.extraction.RdbDefault;

import com.mobigen.datafabric.extraction.UserDefineException.TableWhileStoppedException;
import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.ExtractAdditional;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;

import java.sql.DriverManager;
import java.sql.SQLException;

public class RdbExtractMetadata implements Extract, ExtractAdditional {

    public TargetConfig target;
    Metadata metadata = new Metadata();

    public RdbExtractMetadata(TargetConfig target) throws ClassNotFoundException {
        this.target = target;
    }

    @Override
    public Metadata extract() {
        try {
            extractDefault();
            extractAdditional();
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }
        return this.metadata;
    }

    @Override
    public void extractDefault() {
        try {
            // JDBC 드라이버 로딩
            Class.forName(target.getConnectInfo().getRdbmsConnectInfo().getJDBC_DRIVER());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

//        try (var conn = DriverManager.getConnection(target.getConnectInfo().getRdbmsConnectInfo().getUrl(), target.getConnectInfo().getRdbmsConnectInfo().getUsername(), target.getConnectInfo().getRdbmsConnectInfo().getPassword())) {
//            var stmt = conn.createStatement();
//
//            var JDBC_DRIVER = stmt.executeQuery("SELECT value FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".storage_adaptor_conn_info_schema WHERE type='" + target.getConnectInfo().getRdbmsConnectInfo().getHost() + "' and key='JDBC_DRIVER'");
//            var url = stmt.executeQuery("SELECT value FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".storage_adaptor_conn_info_schema WHERE type='" + target.getConnectInfo().getRdbmsConnectInfo().getHost() + "' and key='url'");
//            var username = stmt.executeQuery("SELECT value FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".storage_adaptor_conn_info_schema WHERE type='" + target.getConnectInfo().getRdbmsConnectInfo().getHost() + "' and key='username'");
//            var password = stmt.executeQuery("SELECT value FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".storage_adaptor_conn_info_schema WHERE type='" + target.getConnectInfo().getRdbmsConnectInfo().getHost() + "' and key='password'");
//            var schema = stmt.executeQuery("SELECT value FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".storage_adaptor_conn_info_schema WHERE type='" + target.getConnectInfo().getRdbmsConnectInfo().getHost() + "' and key='schema'");
//        } catch (SQLException e) {
//            System.out.println("겉 catch 호출---------------------");
////            throw new RuntimeException(e);
//        }

        try (var conn = DriverManager.getConnection(target.getConnectInfo().getRdbmsConnectInfo().getUrl(), target.getConnectInfo().getRdbmsConnectInfo().getUsername(), target.getConnectInfo().getRdbmsConnectInfo().getPassword())) {
            var metadata = conn.getMetaData();
            var stmt = conn.createStatement();

            // 테이블 정보 추출
            System.out.println("Tables:");
            var tables = metadata.getTables(target.getConnectInfo().getRdbmsConnectInfo().getSchema(), null, null, new String[]{"TABLE"});
            System.out.println(tables);

            while (tables.next()) {
                try {
                    RdbDefaultExraction.DefaultExract(tables, stmt, target, metadata);
                } catch(TableWhileStoppedException e){
                    System.out.println("Default while 문 실행 중 오류");
                }
            }
            tables.close();

        } catch (SQLException e) {
            System.out.println("겉 catch 호출---------------------");
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void extractAdditional() throws UnsupportedFormatException {

        try (var conn = DriverManager.getConnection(target.getConnectInfo().getRdbmsConnectInfo().getUrl(), target.getConnectInfo().getRdbmsConnectInfo().getUsername(), target.getConnectInfo().getRdbmsConnectInfo().getPassword());) {
            var table = new RdbTableExtraction(target, conn);
            table.extract(target);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
