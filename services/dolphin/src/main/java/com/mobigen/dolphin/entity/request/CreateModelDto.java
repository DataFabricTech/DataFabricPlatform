package com.mobigen.dolphin.entity.request;

import com.mobigen.dolphin.util.JoinType;
import com.mobigen.dolphin.util.ModelType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
public class CreateModelDto {
    private String modelName;

    private BaseModel baseModel;
    private List<JoinModel> joins;

    @Getter
    @Setter
    public static class BaseModel {
        private ModelType type;
        private List<String> selectedColumnNames;
        // MODEL
        private String model;
        // QUERY
        private String query;
        // CONNECTOR
        private UUID connectorId;
        private String database;
        private String table;
    }

    @Getter
    @Setter
    public static class JoinModel {
        private JoinType joinType;
        private String model;
        private String on;
    }
}
