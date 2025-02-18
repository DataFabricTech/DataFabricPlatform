package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "connection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Connection {
    @Column(name = "execute_at")
    private Long executeAt;

    @Id
    @Column(name = "execute_by")
    private String executeBy;

    @Column(name = "query_execution_time")
    private Long queryExecutionTime;

    @Column(name = "service_id")
    private UUID serviceID;
}
