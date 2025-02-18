package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ingestion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ingestion {
    @Id
    @Column(name = "ingestion_id", nullable = false)
    private UUID ingestionID;

    @Column(name = "ingestion_name", nullable = false)
    private String name;

    @Column(name = "ingestion_display_name", nullable = false)
    private String displayName;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "service_fqn")
    private String serviceFQN;

    @Column(name = "service_id")
    private UUID serviceID;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "ingestion_id")
    private List<IngestionHistory> ingestionHistories = new ArrayList<>();
}
