package dto.compositeKeys;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataTypeOptionSchemaKey implements Serializable {
    private String dataType;
    private String key;
}
