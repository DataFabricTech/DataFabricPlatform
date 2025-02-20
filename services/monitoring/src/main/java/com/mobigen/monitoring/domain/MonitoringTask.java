package com.mobigen.monitoring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "monitoring_task")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MonitoringTask {
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id")
    private String id;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;
}
