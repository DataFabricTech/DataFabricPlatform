package com.mobigen.monitoring.domain;

import com.mobigen.monitoring.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "connection_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConnectionHistory {
    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Id
    @Column(name = "service_id", nullable = false)
    private UUID serviceID;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;
}
