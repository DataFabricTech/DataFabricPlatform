package com.mobigen.vdap.server.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "storage_service_entity")
public class StorageServiceEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "kind", nullable = false)
    private String kind;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "json", nullable = false)
    private String json;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
