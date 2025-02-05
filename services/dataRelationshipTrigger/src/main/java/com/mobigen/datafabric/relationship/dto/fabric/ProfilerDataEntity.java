package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "profiler_data_time_series",
        uniqueConstraints = @UniqueConstraint(columnNames = {"entityFQNHash", "extension", "operation", "timestamp"}))
public class ProfilerDataEntity {
    @EmbeddedId
    private ProfilerDataEntityId id;

    @Column(name = "jsonSchema", nullable = false)
    private String jsonSchema;

    @Column(name = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> json;

}
