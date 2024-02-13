package dto;

import dto.compositeKeys.DataFormatOptionKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_format_option")
@IdClass(DataFormatOptionKey.class)
public class DataFormatOption implements generateKey{
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "data_format_option_key", nullable = false)
    private String dataFormatOptionKey;
    @Column(name = "data_format_option_value")
    private String dataFormatOptionValue;

    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false, insertable = false)
    private Model model;

    @Builder(toBuilder = true)
    public DataFormatOption(UUID modelId, String dataFormatOptionKey, String dataFormatOptionValue) {
        this.modelId = modelId;
        this.dataFormatOptionKey = dataFormatOptionKey;
        this.dataFormatOptionValue = dataFormatOptionValue;
    }

    @Override
    public Object generateKey() {
        return DataFormatOptionKey.builder()
                .modelId(this.modelId)
                .dataFormatOptionKey(this.dataFormatOptionKey)
                .build();
    }
}
