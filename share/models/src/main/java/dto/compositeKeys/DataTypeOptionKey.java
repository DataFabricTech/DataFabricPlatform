package dto.compositeKeys;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class DataTypeOptionKey implements Serializable {
    private UUID modelId;
    private String key;
}
