package dto.compositeKeys;

import dto.enums.FormatType;
import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;

public class DataFormatOptionSchemaKey implements Serializable {
    private FormatType formatType;
    private String dataFormatOptionSchemaKey;

    public DataFormatOptionSchemaKey() {
    }

    @Builder
    public DataFormatOptionSchemaKey(FormatType formatType, String dataFormatOptionSchemaKey) {
        this.formatType = formatType;
        this.dataFormatOptionSchemaKey = dataFormatOptionSchemaKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DataFormatOptionSchemaKey) o;
        return Objects.equals(that.formatType, this.formatType) &&
                Objects.equals(that.dataFormatOptionSchemaKey, this.dataFormatOptionSchemaKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.formatType, this.dataFormatOptionSchemaKey);
    }

}
