package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openmetadata.schema.entity.classification.Tag;
import org.openmetadata.schema.type.TagLabel;

@Entity
@Getter
@Setter
@Table(name = "tag", uniqueConstraints = {@UniqueConstraint(columnNames = {"fqnHash"})})
public class TagEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Tag json;

    @Column(name = "updatedAt", nullable = false)
    private Long updatedAt;

    @Column(name = "updatedBy", nullable = false)
    private String updatedBy;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "fqnHash")
    private String fqnHash;

    @Column(name = "name", nullable = false)
    private String name;
}
