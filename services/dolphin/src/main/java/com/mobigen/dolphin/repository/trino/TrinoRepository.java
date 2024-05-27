package com.mobigen.dolphin.repository.trino;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.entity.response.ModelDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class TrinoRepository {
    private final DolphinConfiguration dolphinConfiguration;
    private final JdbcTemplate trinoJdbcTemplate;

    private RowMapper<ModelDto> modelDtoRowMapper() {
        return ((rs, rowNum) -> ModelDto.builder()
                .name(rs.getString("Table"))
                .build());
    }

    public List<ModelDto> getModelList() {
        // SHOW TABLES [ FROM schema ] [ LIKE pattern ]
        return trinoJdbcTemplate.query("show tables from " +
                        dolphinConfiguration.getModel().getCatalog() + "." + dolphinConfiguration.getModel().getSchema(),
                modelDtoRowMapper());
    }

    public void execute(String sql) {
        trinoJdbcTemplate.execute(sql);
    }


    public String executeQuery(String jobId, String sql, Boolean direct) {
        if (!direct) {
            // 결과를 가져와서 파일로 저장
            try {
                var path = new ClassPathResource("dev/" + jobId + ".csv");
                FileOutputStream outputStream = new FileOutputStream(path.getFile().getPath());
                trinoJdbcTemplate.query(sql, new StreamingCsvResultSetExtractor(outputStream));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "";
        } else {
            // trino 가 직접 hive table 생성을 통해서 결과 데이터 저장
            var resultTableName = "internalhive.dolphin_cache.data_" + (jobId.hashCode() & 0xfffffff);
            var s = "create table " + resultTableName +
                    " with (format = 'PARQUET', external_location = 's3a://warehouse/result/" + jobId + "')" +
                    " as " + sql;
            trinoJdbcTemplate.execute(s);
            return resultTableName;
        }
    }

    public String executeQuery(String jobId, String sql) {
        return executeQuery(jobId, sql, false);
    }

    static class StreamingCsvResultSetExtractor implements ResultSetExtractor<Void> {
        private final char DELIMITER = ',';
        @Getter
        @Setter
        private char ESCAPE_CHAR = '"';
        private final OutputStream outputStream;

        public StreamingCsvResultSetExtractor(final OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
            try (PrintWriter printWriter = new PrintWriter(outputStream, true)) {
                var resultSetMetadata = rs.getMetaData();
                var columnCount = resultSetMetadata.getColumnCount();
                writeHeader(resultSetMetadata, columnCount, printWriter);
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        var value = rs.getObject(i);
                        var strValue = value == null ? "" : value.toString();
                        printWriter.write(strValue.contains(",") ? ESCAPE_CHAR + strValue + ESCAPE_CHAR : strValue);
                        if (i != columnCount) {
                            printWriter.write(DELIMITER);
                        }
                    }
                    printWriter.println();
                }
                printWriter.flush();
            }
            return null;
        }

        private void writeHeader(final ResultSetMetaData resultSetMetaData,
                                 final int columnCount,
                                 final PrintWriter printWriter) throws SQLException {
            for (int i = 1; i <= columnCount; i++) {
                printWriter.write(resultSetMetaData.getColumnName(i));
                if (i != columnCount) {
                    printWriter.append(DELIMITER);
                }
            }
            printWriter.println();
        }
    }

}
