package com.mobigen.dolphin.entity.openmetadata;

import com.mobigen.dolphin.util.JsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Data
@Entity
@Table(name = "dbservice_entity")
public class DBServiceEntity {
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;
    private String name;
    private String serviceType;
    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = DBServiceJson.DBServiceJsonConverter.class)
    private DBServiceJson json;
    private Long updatedAt;
    private String updatedBy;
    private Boolean deleted;
    private String nameHash;

    @Data
    public static class DBServiceJson {
        private String id;
        private String name;
        private Boolean deleted;
        private String version;
        private Long updatedAt;
        private String updatedBy;
        private String description;
        private String serviceType;
        private String fullyQualifiedName;
        private Map<String, Map<String, Object>> connection;

        public static class DBServiceJsonConverter extends JsonConverter<DBServiceJson> {
        }
    }
}
