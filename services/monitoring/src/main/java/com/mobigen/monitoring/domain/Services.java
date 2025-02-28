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

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    private List<ConnectionDao> connectionDaos = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    private List<ConnectionHistory> connectionHistories = new ArrayList<>();

    public void setConnectionHistories(List<ConnectionHistory> connectionHistories) {
        this.connectionHistories = connectionHistories;
    }

    public void setConnectionDaos(List<ConnectionDao> connectionDaos) {
        this.connectionDaos = connectionDaos;
    }
}
