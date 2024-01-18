package dto;

import dto.compositeKeys.ModelMetadataKey;
import dto.compositeKeys.StorageConnInfoKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "storage_conn_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(StorageConnInfoKey.class)
public class StorageConnInfo {
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    @Id
    private String type;
    @Id
    private String key;
    private String value;
    @Column(name = "is_option")
    private boolean isOption;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false,updatable = false)
    private Storage storage;

    @Builder
    public StorageConnInfo(UUID storageId, String type, String key, String value, boolean isOption) {
        this.storageId = storageId;
        this.type = type;
        this.key = key;
        this.value = value;
        this.isOption = isOption;
    }
}
