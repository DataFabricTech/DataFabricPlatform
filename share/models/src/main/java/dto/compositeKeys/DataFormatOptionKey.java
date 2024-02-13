package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class DataFormatOptionKey implements Serializable {
    private UUID modelId;
    private String dataFormatOptionKey;

    public DataFormatOptionKey() {
    }

    @Builder
    public DataFormatOptionKey(UUID modelId, String dataFormatOptionKey) {
        this.modelId = modelId;
        this.dataFormatOptionKey = dataFormatOptionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DataFormatOptionKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.dataFormatOptionKey, this.dataFormatOptionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.dataFormatOptionKey);
    }
}
