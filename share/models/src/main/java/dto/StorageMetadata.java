package dto;

import dto.compositeKeys.StorageMetadataKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "storage_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(StorageMetadataKey.class)
public class StorageMetadata implements generateKey{
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    @Id
    @Column(name = "metadata_id", nullable = false)
    private UUID metadataId;
    @Column(name = "metadata_value")
    private String metadataValue;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false,updatable = false)
    private Storage storage;
    @ManyToOne
    @JoinColumn(name = "metadata_id", insertable = false,updatable = false)
    private StorageMetadataSchema storageMetadataSchema;

    @Builder(toBuilder = true)
    public StorageMetadata(UUID storageId, UUID metadataId, String metadataValue) {
        this.storageId = storageId;
        this.metadataId = metadataId;
        this.metadataValue = metadataValue;
    }

    @Override
    public Object generateKey() {
        return StorageMetadataKey.builder()
                .metadataId(this.metadataId)
                .storageId(this.storageId)
                .build();
    }
}
