package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StorageMetadataKey implements Serializable {
    private UUID storageId;
    private UUID metadataId;

    public StorageMetadataKey() {
    }

    @Builder
    public StorageMetadataKey(UUID storageId, UUID metadataId) {
        this.storageId = storageId;
        this.metadataId = metadataId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (StorageMetadataKey) o;
        return Objects.equals(that.storageId, this.storageId) &&
                Objects.equals(that.metadataId, this.metadataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.storageId, this.metadataId);
    }

}
