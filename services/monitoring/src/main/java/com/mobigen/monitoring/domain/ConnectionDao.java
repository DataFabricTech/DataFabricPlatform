package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "connection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@ToString
public class ConnectionDao {
    @Id
    @Column(name = "connection_id")
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    private UUID connectionId;

    @Column(name = "execute_at")
    private Long executeAt;

    @Column(name = "query_execution_time")
    private Long queryExecutionTime;

    @Column(name = "service_id")
    private UUID serviceID;
}
