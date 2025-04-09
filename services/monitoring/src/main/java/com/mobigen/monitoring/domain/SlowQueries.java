package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "slow_queries")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SlowQueries {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id")
    private String id;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "query")
    private String query;

    @Column(name = "duration_time")
    private Float durationTime;

    @Column(name = "created_at")
    private Long createdAt;
}
