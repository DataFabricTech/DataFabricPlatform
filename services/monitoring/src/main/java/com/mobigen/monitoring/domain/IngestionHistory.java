package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "ingestion_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IngestionHistory {
    @Column(name = "event_at")
    private Long eventAt;

    @Id
    @Column(name = "ingestion_id")
    private UUID ingestionID;

    @Column(name = "run_id")
    private UUID ingestionRunId;

    @Column(name = "event")
    private String event;

    @Column(name = "state")
    private String state;
}
