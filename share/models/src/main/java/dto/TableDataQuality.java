package dto;

import dto.compositeKeys.TableDataQualityKey;
import dto.enums.QualityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "table_data_quality")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(TableDataQualityKey.class)
public class TableDataQuality {
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "num", nullable = false)
    private int num;
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "quality_type")
    private QualityType qualityType;
    private Integer value;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "model_id", referencedColumnName = "model_id", updatable = false, insertable = false),
            @JoinColumn(name = "num", referencedColumnName = "num", updatable = false, insertable = false)
    })
    private ColumnMetadata columnMetadata;

    @Builder
    public TableDataQuality(UUID modelId, int num, QualityType qualityType, Integer value) {
        this.modelId = modelId;
        this.num = num;
        this.qualityType = qualityType;
        this.value = value;
    }
}
