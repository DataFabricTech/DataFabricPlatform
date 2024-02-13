package dto;

import dto.enums.FormatType;
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
@Table(name = "data_format_schema")
public class DataFormatSchema implements generateKey{
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "format_type", nullable = false)
    private FormatType formatType;
    @Column(unique = true)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataFormatSchema")
    private List<Model> models = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataFormatSchema")
    private List<DataFormatOptionSchema> dataFormatOptionSchemas = new ArrayList<>();

    @Builder(toBuilder = true)
    public DataFormatSchema(FormatType formatType, String name, List<Model> models,
                            List<DataFormatOptionSchema> dataFormatOptionSchemas) {
        this.formatType = formatType;
        this.name = name;
        this.models = models;
        this.dataFormatOptionSchemas = dataFormatOptionSchemas;
    }

    @Override
    public Object generateKey() {
        return this.formatType;
    }
}
