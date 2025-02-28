package com.mobigen.vdap.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "entity_relationship")
@IdClass(EntityRelationshipId.class)
public class EntityRelationshipEntity {
    @Id
    @Column(name="from_id")
    private String fromId;
    
    @Id
    @Column(name="to_id")
    private String toId;
    
    @Id
    @Column(name="relation")
    private Integer relation;
    
    @Column(name="from_entity", nullable = false)
    private String fromEntity;
    
    @Column(name="to_entity", nullable = false)
    private String toEntity;
    
    @Column(name="json_schema")
    private String jsonSchema;
    
    @Column(name="json", columnDefinition = "json")
    private String json;
    
    @Column(name="deleted", nullable = false)
    private Boolean deleted = false;
} 