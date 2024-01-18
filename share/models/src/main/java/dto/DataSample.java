package dto;

import dto.compositeKeys.DataSampleKey;
import dto.enums.FormatType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "data_sample")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(DataSampleKey.class)
public class DataSample {
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Enumerated(EnumType.STRING)
    @Id
    @Column(name = "format_type")
    private FormatType formatType;
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "model_id", insertable = false,updatable = false)
    private Model model;

    @Builder
    public DataSample(UUID modelId, FormatType formatType, String filePath) {
        this.modelId = modelId;
        this.formatType = formatType;
        this.filePath = filePath;
    }
}
