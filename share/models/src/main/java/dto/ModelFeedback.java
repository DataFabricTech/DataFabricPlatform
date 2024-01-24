package dto;

import dto.compositeKeys.ModelFeedbackKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "model_feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ModelFeedbackKey.class)
public class ModelFeedback implements generateKey {
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "feed_id", nullable = false)
    private UUID feedId;
    @Column(name = "parent_feed_id")
    private UUID parentFeedId;
    @Column(name = "user_id")
    private UUID userId;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime time;
    private String title;
    private String body;
    @Column(name = "is_resolved")
    private boolean isResolved;
    @Column(name = "resolved_user_id")
    private UUID resolvedUserId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;

    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false, insertable = false)
    private Model model;

    @Builder(toBuilder = true)
    public ModelFeedback(UUID modelId, UUID feedId, UUID parentFeedId, UUID userId, LocalDateTime time,
                         String title, String body, boolean isResolved, UUID resolvedUserId,
                         LocalDateTime resolvedTime) {
        this.modelId = modelId;
        this.feedId = feedId;
        this.parentFeedId = parentFeedId;
        this.userId = userId;
        this.time = time;
        this.title = title;
        this.body = body;
        this.isResolved = isResolved;
        this.resolvedUserId = resolvedUserId;
        this.resolvedTime = resolvedTime;
    }

    @Override
    public Object generateKey() {
        return ModelFeedbackKey.builder()
                .modelId(this.modelId)
                .feedId(this.feedId)
                .build();
    }
}
