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
public class DataTypeSchema {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    @Column(unique = true)
    private String name;

    @OneToMany
    @JoinColumn(name = "data_type")
    private List<Model> models = new ArrayList<>();
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "data_type")
    private List<DataTypeOptionSchema> dataTypeOptionSchemas = new ArrayList<>();

    @Builder
    public DataTypeSchema(DataType dataType, String name, List<Model> models,
                          List<DataTypeOptionSchema> dataTypeOptionSchemas) {
        this.dataType = dataType;
        this.name = name;
        this.models = models;
        this.dataTypeOptionSchemas = dataTypeOptionSchemas;
    }
}
