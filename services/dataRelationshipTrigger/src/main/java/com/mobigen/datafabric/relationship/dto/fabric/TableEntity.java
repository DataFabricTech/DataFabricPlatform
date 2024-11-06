package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "table_entity")
public class TableEntity {
    @Id
    @Size(max = 36)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @NotNull
    @Column(name = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private org.openmetadata.schema.entity.data.Table json;

    @NotNull
    @Column(name = "updatedAt", nullable = false)
    private Long updatedAt;

    @Size(max = 256)
    @NotNull
    @Column(name = "updatedBy", nullable = false, length = 256)
    private String updatedBy;

    @Column(name = "deleted")
    private Boolean deleted;

    @Size(max = 1024)
    @Column(name = "fqnHash", length = 1024)
    private String fqnHash;

    @Size(max = 256)
    @NotNull
    @Column(name = "name", nullable = false, length = 256)
    private String name;

}