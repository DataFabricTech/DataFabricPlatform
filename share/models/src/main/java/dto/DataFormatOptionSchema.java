package dto;

import dto.compositeKeys.DataFormatOptionSchemaKey;
import dto.enums.DataType;
import dto.enums.FormatType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_format_option_schema")
@IdClass(DataFormatOptionSchemaKey.class)
public class DataFormatOptionSchema implements generateKey {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "format_type", nullable = false)
    private FormatType formatType;
    @Id
    @Column(name = "data_format_option_schema_key", nullable = false)
    private String dataFormatOptionSchemaKey;
    @Column(name = "data_format_option_schema_value")
    private String dataFormatOptionSchemaValue;
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;
    @Column(name = "default_value")
    private String defaultValue;
    private String description;

    @ManyToOne
    @JoinColumn(name = "data_type", updatable = false, insertable = false)
    private DataFormatSchema dataFormatSchema;

    @Builder(toBuilder = true)
    public DataFormatOptionSchema(FormatType formatType, String dataFormatOptionSchemaKey, String dataFormatOptionSchemaValue, DataType dataType,
                                  String defaultValue, String description) {
        this.formatType = formatType;
        this.dataFormatOptionSchemaKey = dataFormatOptionSchemaKey;
        this.dataFormatOptionSchemaValue = dataFormatOptionSchemaValue;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    @Override
    public Object generateKey() {
        return DataFormatOptionSchemaKey.builder()
                .formatType(this.formatType)
                .dataFormatOptionSchemaKey(this.dataFormatOptionSchemaKey)
                .build();
    }
}
