package com.mobigen.monitoring.domain;

import com.mobigen.monitoring.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Services {
    @Id
    @Column(name = "service_id", nullable = false)
    private UUID serviceID;

    @Column(name = "service_name", nullable = false)
    private String name;

    @Column(name = "service_display_name")
    private String displayName;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;

    @Column(name = "monitoring_period")
    private Integer monitoringPeriod;

    @Column(name = "monitoring")
    private Boolean monitoring;

    @PrePersist
    public void prePersist() {
        if (monitoringPeriod == null) {
            monitoringPeriod = 30;
        }

        if (monitoring == null) {
            monitoring = true;
        }
    }
}
