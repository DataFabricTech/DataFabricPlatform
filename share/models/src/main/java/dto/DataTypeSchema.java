package dto;

import dto.enums.DataType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_type_schema")
public class DataTypeSchema implements generateKey{
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    @Column(unique = true)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTypeSchema")
    private List<Model> models = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTypeSchema")
    private List<DataTypeOptionSchema> dataTypeOptionSchemas = new ArrayList<>();

    @Builder(toBuilder = true)
    public DataTypeSchema(DataType dataType, String name, List<Model> models,
                          List<DataTypeOptionSchema> dataTypeOptionSchemas) {
        this.dataType = dataType;
        this.name = name;
        this.models = models;
        this.dataTypeOptionSchemas = dataTypeOptionSchemas;
    }

    @Override
    public Object generateKey() {
        return this.dataType.name();
    }
}
