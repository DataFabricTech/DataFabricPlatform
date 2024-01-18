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
public class ModelTag {
    @Id
    @Column(name = "model_id",nullable = false)
    private UUID modelId;
    @Id
    private UUID tag;

    @ManyToOne
    @JoinColumn(name = "model_id", insertable = false,updatable = false)
    private Model model;

    @Builder
    public ModelTag(UUID modelId, UUID tag) {
        this.modelId = modelId;
        this.tag = tag;
    }
}
