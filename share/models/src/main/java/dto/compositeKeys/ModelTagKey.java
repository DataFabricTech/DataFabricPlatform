package dto.compositeKeys;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ModelTagKey implements Serializable {
    private UUID modelId;
    private UUID tagId;

    public ModelTagKey() {
    }

    @Builder
    public ModelTagKey(UUID modelId, UUID tagId) {
        this.modelId = modelId;
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ModelTagKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.tagId, this.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.tagId);
    }

}
