package dto.compositeKeys;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class StorageConnInfoKey implements Serializable {
    private UUID storageId;
    private String type;
    private String key;
}
