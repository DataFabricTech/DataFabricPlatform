package dto.compositeKeys;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ColumnMetadataKey implements Serializable {
    private UUID modelId;
    private int num;

    public ColumnMetadataKey() {
    }

    @Builder
    public ColumnMetadataKey(UUID modelId, int num) {
        this.modelId = modelId;
        this.num = num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ColumnMetadataKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                that.num == this.num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.num);
    }
}
