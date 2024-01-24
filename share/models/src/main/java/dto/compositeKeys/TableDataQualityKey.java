package dto.compositeKeys;

import dto.enums.QualityType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TableDataQualityKey implements Serializable {
    private UUID modelId;
    private int num;
    private QualityType qualityType;

    public TableDataQualityKey() {
    }

    @Builder(toBuilder = true)
    public TableDataQualityKey(UUID modelId, int num, QualityType qualityType) {
        this.modelId = modelId;
        this.num = num;
        this.qualityType = qualityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDataQualityKey that = (TableDataQualityKey) o;
        return Objects.equals(that.modelId, this.modelId) &&
                that.num == this.num &&
                Objects.equals(that.qualityType, this.qualityType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelId, this.num, this.qualityType);
    }

}
