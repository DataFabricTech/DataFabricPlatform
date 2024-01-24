package dto.compositeKeys;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ModelFeedbackKey implements Serializable {
    private UUID modelId;
    private UUID feedId;

    public ModelFeedbackKey() {
    }

    @Builder
    public ModelFeedbackKey(UUID modelId, UUID feedId) {
        this.modelId = modelId;
        this.feedId = feedId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ModelFeedbackKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.feedId, this.feedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.feedId);
    }

}
