package dto;

import dto.compositeKeys.ModelMetadataKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "model_metadata_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelMetadataSchema implements generateKey{
    @Id
    @Column(name = "metadata_id", nullable = false)
    private UUID metadataId;
    private String name;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "modelMetadataSchema")
    private List<ModelMetadata> modelMetadataList = new ArrayList<>();

    @Builder(toBuilder = true)
    public ModelMetadataSchema(UUID metadataId, String name, String description, List<ModelMetadata> modelMetadataList) {
        this.metadataId = metadataId;
        this.name = name;
        this.description = description;
        this.modelMetadataList = modelMetadataList;
    }

    @Override
    public Object generateKey() {
        return this.metadataId;
    }
}
