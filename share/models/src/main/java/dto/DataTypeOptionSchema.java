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
public class DataTypeOptionSchema {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    @Id
    private String key;
    private String value;
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type")
    private ValueType valueType;
    @Column(name = "default_value")
    private String defaultValue;
    private String description;

    @ManyToOne
    @JoinColumn(name = "data_type", updatable = false, insertable = false)
    private DataTypeSchema dataTypeSchema;

    @Builder
    public DataTypeOptionSchema(DataType dataType, String key, String value, ValueType valueType,
                                String defaultValue, String description) {
        this.dataType = dataType;
        this.key = key;
        this.value = value;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.description = description;
    }
}
