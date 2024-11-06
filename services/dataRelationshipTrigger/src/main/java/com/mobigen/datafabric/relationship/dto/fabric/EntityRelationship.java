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
@Table(name = "entity_relationship")
public class EntityRelationship {
    @EmbeddedId
    private EntityRelationshipId id;

    @NotNull
    @Column(name = "fromEntity", nullable = false)
    private String fromEntity;

    @NotNull
    @Column(name = "toEntity", nullable = false)
    private String toEntity;

    @Column(name = "jsonSchema")
    private String jsonSchema;

    @Column(name = "json")
    private String json;

    @NotNull
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}