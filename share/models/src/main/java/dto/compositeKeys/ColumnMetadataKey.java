package dto.compositeKeys;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class ColumnMetadataKey implements Serializable {
    private UUID modelId;
    private int num;
}
