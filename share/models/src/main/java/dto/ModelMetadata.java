package dto;

import dto.compositeKeys.ModelMetadataKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "model_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ModelMetadataKey.class)
public class ModelMetadata implements generateKey{
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "metadata_id", nullable = false)
    private UUID metadataId;
    private String value;

    @ManyToOne
    @JoinColumn(name = "metadata_id", updatable = false, insertable = false)
    private ModelMetadataSchema modelMetadataSchema;
    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false, insertable = false)
    private Model model;

    @Builder(toBuilder = true)
    public ModelMetadata(UUID modelId, UUID metadataId, String value) {
        this.modelId = modelId;
        this.metadataId = metadataId;
        this.value = value;
    }

    @Override
    public Object generateKey() {
        return ModelMetadataKey.builder()
                .modelId(this.modelId)
                .metadataId(this.metadataId)
                .build();
    }
}
