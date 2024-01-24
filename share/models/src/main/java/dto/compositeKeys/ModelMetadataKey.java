package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ModelMetadataKey implements Serializable {
    private UUID modelId;
    private UUID metadataId;

    public ModelMetadataKey() {
    }

    @Builder
    public ModelMetadataKey(UUID modelId, UUID metadataId) {
        this.modelId = modelId;
        this.metadataId = metadataId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ModelMetadataKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.metadataId, this.metadataId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.metadataId);
    }
}
