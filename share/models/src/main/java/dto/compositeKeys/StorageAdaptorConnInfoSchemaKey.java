package dto.compositeKeys;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StorageAdaptorConnInfoSchemaKey implements Serializable {
    private UUID adaptorId;
    private String type;
    private String adaptorConnSchemaKey;

    public StorageAdaptorConnInfoSchemaKey() {
    }

    @Builder
    public StorageAdaptorConnInfoSchemaKey(UUID adaptorId, String type, String adaptorConnSchemaKey) {
        this.adaptorId = adaptorId;
        this.type = type;
        this.adaptorConnSchemaKey = adaptorConnSchemaKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (StorageAdaptorConnInfoSchemaKey) o;
        return Objects.equals(that.adaptorId, this.adaptorId) &&
                Objects.equals(that.type, this.type) &&
                Objects.equals(that.adaptorConnSchemaKey, this.adaptorConnSchemaKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.adaptorId, this.type, this.adaptorConnSchemaKey);
    }

}
