package dto;

import dto.compositeKeys.StorageAdaptorConnInfoSchemaKey;
import dto.enums.ValueType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "storage_adaptor_conn_info_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(StorageAdaptorConnInfoSchemaKey.class)
public class StorageAdaptorConnInfoSchema implements generateKey{
    @Id
    @Column(name = "adaptor_id", nullable = false)
    private UUID adaptorId;
    @Id
    private String type;
    @Id
    @Column(name = "adaptor_conn_schema_key",nullable = false)
    private String adaptorConnSchemaKey;
    @Column(name = "adaptor_conn_schema_value")
    private String adaptorConnSchemaValue;
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type")
    private ValueType valueType;
    @Column(name = "default_value")
    private String defaultValue;
    private String description;
    private boolean required;

    @ManyToOne
    @JoinColumn(name = "adaptor_id", insertable = false,updatable = false)
    private StorageAdaptorSchema storageAdaptorSchema;

    @Builder(toBuilder = true)
    public StorageAdaptorConnInfoSchema(UUID adaptorId, String type, String adaptorConnSchemaKey, String adaptorConnSchemaValue, ValueType valueType,
                                        String defaultValue, String description, boolean required) {
        this.adaptorId = adaptorId;
        this.type = type;
        this.adaptorConnSchemaKey = adaptorConnSchemaKey;
        this.adaptorConnSchemaValue = adaptorConnSchemaValue;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.required = required;
    }

    @Override
    public Object generateKey() {
        return StorageAdaptorConnInfoSchemaKey.builder()
                .adaptorId(this.adaptorId)
                .adaptorConnSchemaKey(this.adaptorConnSchemaKey)
                .type(this.type)
                .build();
    }
}
