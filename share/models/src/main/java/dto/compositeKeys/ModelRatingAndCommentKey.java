package dto.compositeKeys;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ModelRatingAndCommentKey implements Serializable {
    private UUID modelId;
    private UUID userId;

    public ModelRatingAndCommentKey() {
    }

    @Builder
    public ModelRatingAndCommentKey(UUID modelId, UUID userId) {
        this.modelId = modelId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ModelRatingAndCommentKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.userId, this.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.userId);
    }

}
