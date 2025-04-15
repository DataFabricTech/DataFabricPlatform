package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "slow_query_statistic")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SlowQueryStatistic {
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "query")
    private String query;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "average_executed_time")
    private Float averageExecutedTime;

    @Column(name = "created_at")
    private Long createdAt;
}
