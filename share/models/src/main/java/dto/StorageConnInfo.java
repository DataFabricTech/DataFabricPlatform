package dto;

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
public class StorageConnInfo implements generateKey{
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    @Id
    private String type;
    @Id
    @Column(name = "storage_conn_key", nullable = false)
    private String storageConnKey;
    @Column(name = "storage_conn_value")
    private String storageConnValue;
    @Column(name = "is_option")
    private boolean isOption;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false,updatable = false)
    private Storage storage;

    @Builder(toBuilder = true)
    public StorageConnInfo(UUID storageId, String type, String storageConnKey, String storageConnValue, boolean isOption) {
        this.storageId = storageId;
        this.type = type;
        this.storageConnKey = storageConnKey;
        this.storageConnValue = storageConnValue;
        this.isOption = isOption;
    }

    @Override
    public Object generateKey() {
        return StorageConnInfoKey.builder()
                .storageId(this.storageId)
                .storageConnKey(this.storageConnKey)
                .type(this.type)
                .build();
    }
}
