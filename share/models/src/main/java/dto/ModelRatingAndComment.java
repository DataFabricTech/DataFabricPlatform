package dto;

import dto.compositeKeys.ModelRatingAndCommentKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "model_rating_and_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ModelRatingAndCommentKey.class)
public class ModelRatingAndComment implements generateKey{
    @Id
    @Column(name = "model_id")
    private UUID modelId;
    @Id
    @Column(name = "user_id")
    private UUID userId;
    private int rating;
    private String comments;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false, insertable = false)
    private Model model;

    @Builder(toBuilder = true)
    public ModelRatingAndComment(UUID modelId, UUID userId, int rating, String comments, LocalDateTime createdAt) {
        this.modelId = modelId;
        this.userId = userId;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    @Override
    public Object generateKey() {
        return ModelRatingAndCommentKey.builder()
                .modelId(this.modelId)
                .userId(this.userId)
                .build();
    }
}
