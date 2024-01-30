package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class DataTypeOptionKey implements Serializable {
    private UUID modelId;
    private String dataTypeOptionKey;

    public DataTypeOptionKey() {
    }

    @Builder
    public DataTypeOptionKey(UUID modelId, String dataTypeOptionKey) {
        this.modelId = modelId;
        this.dataTypeOptionKey = dataTypeOptionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DataTypeOptionKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.dataTypeOptionKey, this.dataTypeOptionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.dataTypeOptionKey);
    }
}
