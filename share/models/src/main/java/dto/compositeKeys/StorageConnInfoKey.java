package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StorageConnInfoKey implements Serializable {
    private UUID storageId;
    private String type;
    private String storageConnKey;

    public StorageConnInfoKey() {
    }

    @Builder
    public StorageConnInfoKey(UUID storageId, String type, String storageConnKey) {
        this.storageId = storageId;
        this.type = type;
        this.storageConnKey = storageConnKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (StorageConnInfoKey) o;
        return Objects.equals(that.storageId, this.storageId) &&
                Objects.equals(that.type, this.type) &&
                Objects.equals(that.storageConnKey, this.storageConnKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.storageId, this.type, this.storageConnKey);
    }

}
