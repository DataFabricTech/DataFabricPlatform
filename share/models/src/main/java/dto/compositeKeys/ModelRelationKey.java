package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ModelRelationKey implements Serializable {
    private UUID modelId;
    private UUID childModelId;

    public ModelRelationKey() {
    }

    @Builder
    public ModelRelationKey(UUID modelId, UUID childModelId) {
        this.modelId = modelId;
        this.childModelId = childModelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ModelRelationKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.childModelId, this.childModelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.childModelId);
    }

}
