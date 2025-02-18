package com.mobigen.monitoring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "model_registration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ModelRegistration {
    @Id
    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "om_model_count", nullable = false)
    private int omModelCount;

    @Column(name = "model_count", nullable = false)
    private int modelCount;
}
