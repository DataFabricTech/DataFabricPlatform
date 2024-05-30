package com.mobigen.dolphin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@Builder
public class QueryResultDTO {
    private List<Column> columns;
    private List<List<Object>> rows;
    private int totalCount;

    @Data
    @Builder
    public static class Column {
        private String name;
        private String type;
        private String comment;
    }
}
