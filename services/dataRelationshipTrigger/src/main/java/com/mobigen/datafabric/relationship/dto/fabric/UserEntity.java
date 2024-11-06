package com.mobigen.datafabric.relationship.dto.fabric;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openmetadata.schema.entity.teams.User;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "user_entity")
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "deactivated")
    private String deactivated;

    @NotNull
    @Column(name = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private User json;

    @NotNull
    @Column(name = "updatedAt", nullable = false)
    private Long updatedAt;

    @NotNull
    @Column(name = "updatedBy", nullable = false)
    private String updatedBy;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "nameHash")
    private String nameHash;
}