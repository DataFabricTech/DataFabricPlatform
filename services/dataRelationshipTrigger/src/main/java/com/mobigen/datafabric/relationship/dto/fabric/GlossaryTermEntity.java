package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "glossary_term_entity")
public class GlossaryTermEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull
    @Column(name = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> json;

    @NotNull
    @Column(name = "updatedAt", nullable = false)
    private Long updatedAt;

    @NotNull
    @Column(name = "updatedBy", nullable = false)
    private String updatedBy;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "fqnHash")
    private String fqnHash;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;
}