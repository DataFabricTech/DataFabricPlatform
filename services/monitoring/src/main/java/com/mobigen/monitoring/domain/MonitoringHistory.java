package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.NavigableMap;

@Table(name = "monitoring_history")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class MonitoringHistory {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    private String id;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "cpu_used")
    private Float cpuUsed;

    @Column(name = "memory_used")
    private Float memoryUsed;

    @Column(name = "success_request")
    private Long successRequest;

    @Column(name = "failed_request")
    private Long failedRequest;
}
