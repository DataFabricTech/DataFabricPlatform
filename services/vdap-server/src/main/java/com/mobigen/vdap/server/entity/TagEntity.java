package com.mobigen.vdap.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tag")
public class TagEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "classification_id")
    private String classificationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "json", nullable = false)
    private String json;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}
