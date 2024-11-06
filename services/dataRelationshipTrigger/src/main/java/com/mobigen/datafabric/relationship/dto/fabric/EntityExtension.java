package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "entity_extension")
public class EntityExtension {
    @EmbeddedId
    private EntityExtensionId id;

    @NotNull
    @Column(name = "jsonSchema", nullable = false)
    private String jsonSchema;

    @NotNull
    @Column(name = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> json;

}