package dto;

import dto.compositeKeys.DataTypeOptionSchemaKey;
import dto.enums.DataType;
import dto.enums.ValueType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_type_option_schema")
@IdClass(DataTypeOptionSchemaKey.class)
public class DataTypeOptionSchema implements generateKey{
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    @Id
    @Column(name = "data_type_option_schema_key", nullable = false)
    private String dataTypeOptionSchemaKey;
    @Column(name = "data_type_option_schema_value")
    private String dataTypeOptionSchemaValue;
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type")
    private ValueType valueType;
    @Column(name = "default_value")
    private String defaultValue;
    private String description;

    @ManyToOne
    @JoinColumn(name = "data_type", updatable = false, insertable = false)
    private DataTypeSchema dataTypeSchema;

    @Builder(toBuilder = true)
    public DataTypeOptionSchema(DataType dataType, String dataTypeOptionSchemaKey, String dataTypeOptionSchemaValue, ValueType valueType,
                                String defaultValue, String description) {
        this.dataType = dataType;
        this.dataTypeOptionSchemaKey = dataTypeOptionSchemaKey;
        this.dataTypeOptionSchemaValue = dataTypeOptionSchemaValue;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    @Override
    public Object generateKey() {
        return DataTypeOptionSchemaKey.builder()
                .dataType(this.dataType)
                .dataTypeOptionSchemaKey(this.dataTypeOptionSchemaKey)
                .build();
    }
}
