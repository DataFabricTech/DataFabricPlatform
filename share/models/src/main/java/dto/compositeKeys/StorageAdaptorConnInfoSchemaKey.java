package dto.compositeKeys;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class StorageAdaptorConnInfoSchemaKey implements Serializable {
    private UUID adaptorId;
    private String type;
    private String key;
}
