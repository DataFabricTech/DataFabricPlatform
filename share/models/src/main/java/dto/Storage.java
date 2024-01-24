package dto;

import dto.enums.StatusType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Storage implements generateKey{
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    @Column(name = "adaptor_id", nullable = false)
    private UUID adaptorId;
    private String name;
    private String description;
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_monitoring_at")
    private LocalDateTime lastMonitoringAt;
    @Column(name = "sync_enable")
    private boolean syncEnable;
    @Column(name = "sync_time")
    private String syncTime;
    @Column(name = "monitoring_enable")
    private boolean monitoringEnable;
    @Column(name = "monitoring_period")
    private int monitoringPeriod;
    @Column(name = "monitoring_fail_threshold")
    private int monitoringFailThreshold;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storage")
    private List<Model> models = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storage")
    private List<DataAutoAdd> dataAutoAdds = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storage")
    private List<StorageMetadata> storageMetadatas = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storage")
    private List<StorageTag> storageTags = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storage")
    private List<StorageConnInfo> storageConnInfo = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "adaptor_id", insertable = false,updatable = false)
    private StorageAdaptorSchema storageAdaptorSchema;

    @Builder(toBuilder = true)
    public Storage(UUID storageId, UUID adaptorId, String name, String description, UUID createdBy, LocalDateTime createdAt,
                   UUID modifiedBy, LocalDateTime modifiedAt, StatusType status, LocalDateTime lastSyncAt,
                   LocalDateTime lastMonitoringAt, boolean syncEnable, String syncTime, boolean monitoringEnable,
                   int monitoringPeriod, int monitoringFailThreshold, List<Model> models,
                   List<DataAutoAdd> dataAutoAdds, List<StorageMetadata> storageMetadatas, List<StorageTag> storageTags,
                   List<StorageConnInfo> storageConnInfo) {
        this.storageId = storageId;
        this.adaptorId = adaptorId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.status = status;
        this.lastSyncAt = lastSyncAt;
        this.lastMonitoringAt = lastMonitoringAt;
        this.syncEnable = syncEnable;
        this.syncTime = syncTime;
        this.monitoringEnable = monitoringEnable;
        this.monitoringPeriod = monitoringPeriod;
        this.monitoringFailThreshold = monitoringFailThreshold;
        this.models = models == null ? new ArrayList<>(): models;
        this.dataAutoAdds = dataAutoAdds== null ? new ArrayList<>():dataAutoAdds;
        this.storageMetadatas = storageMetadatas== null ? new ArrayList<>():storageMetadatas;
        this.storageTags = storageTags== null ? new ArrayList<>():storageTags;
        this.storageConnInfo = storageConnInfo== null ? new ArrayList<>():storageConnInfo;
    }

    @Override
    public Object generateKey() {
        return this.storageId;
    }
}
