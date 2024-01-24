package dto.compositeKeys;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
public class StorageTagKey implements Serializable {
    private UUID storageId;
    private UUID tagId;

    public StorageTagKey() {
    }

    @Builder
    public StorageTagKey(UUID storageId, UUID tagId) {
        this.storageId = storageId;
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (StorageTagKey) o;
        return Objects.equals(that.storageId, this.storageId) &&
                Objects.equals(that.tagId, this.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.storageId, this.tagId);
    }
}
