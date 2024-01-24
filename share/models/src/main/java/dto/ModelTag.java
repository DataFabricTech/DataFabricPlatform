package dto;

import dto.compositeKeys.ModelTagKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "model_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ModelTagKey.class)
public class ModelTag implements generateKey {
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @ManyToOne
    @JoinColumn(name = "model_id", insertable = false, updatable = false)
    private Model model;

    @ManyToOne
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;

    @Builder(toBuilder = true)
    public ModelTag(UUID modelId, UUID tagId) {
        this.modelId = modelId;
        this.tagId = tagId;
    }

    @Override
    public Object generateKey() {
        return ModelTagKey.builder()
                .modelId(this.modelId)
                .tagId(this.tagId)
                .build();
    }
}
