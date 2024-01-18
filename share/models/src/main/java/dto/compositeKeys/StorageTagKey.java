package dto.compositeKeys;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class StorageTagKey implements Serializable {
    private UUID storageId;
    private UUID tag;
}
