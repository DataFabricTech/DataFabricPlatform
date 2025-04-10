package com.mobigen.vdap.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "entity_extension")
@IdClass(EntityExtensionId.class)
public class EntityExtension {
    @Id
    @Column(name="id")
    private String id;

    @Id
    @Column(name="extension")
    private String extension;

    @Column(name="entity_type")
    private String entityType;

    @Column(name="json")
    private String json;
}
