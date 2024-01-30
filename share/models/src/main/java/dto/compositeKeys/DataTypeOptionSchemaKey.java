package dto.compositeKeys;

import dto.enums.DataType;
import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;

public class DataTypeOptionSchemaKey implements Serializable {
    private DataType dataType;
    private String dataTypeOptionSchemaKey;

    public DataTypeOptionSchemaKey() {
    }

    @Builder
    public DataTypeOptionSchemaKey(DataType dataType, String dataTypeOptionSchemaKey) {
        this.dataType = dataType;
        this.dataTypeOptionSchemaKey = dataTypeOptionSchemaKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (DataTypeOptionSchemaKey) o;
        return Objects.equals(that.dataType, this.dataType) &&
                Objects.equals(that.dataTypeOptionSchemaKey, this.dataTypeOptionSchemaKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dataType, this.dataTypeOptionSchemaKey);
    }

}
