package dto.compositeKeys;

import dto.enums.FormatType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@Getter
public class DataSampleKey implements Serializable {
    private UUID modelId;
    private FormatType formatType;

    public DataSampleKey() {
    }

    @Builder
    public DataSampleKey(UUID modelId, FormatType formatType) {
        this.modelId = modelId;
        this.formatType = formatType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DataSampleKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                Objects.equals(that.formatType, this.formatType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.formatType);
    }

}
