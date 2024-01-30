package dto;

import dto.compositeKeys.DataTypeOptionKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_type_option")
@IdClass(DataTypeOptionKey.class)
public class DataTypeOption implements generateKey{
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "data_type_option_key", nullable = false)
    private String dataTypeOptionKey;
    @Column(name = "data_type_option_value")
    private String dataTypeOptionValue;

    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false, insertable = false)
    private Model model;

    @Builder(toBuilder = true)
    public DataTypeOption(UUID modelId, String dataTypeOptionKey, String dataTypeOptionValue) {
        this.modelId = modelId;
        this.dataTypeOptionKey = dataTypeOptionKey;
        this.dataTypeOptionValue = dataTypeOptionValue;
    }

    @Override
    public Object generateKey() {
        return DataTypeOptionKey.builder()
                .modelId(this.modelId)
                .dataTypeOptionKey(this.dataTypeOptionKey)
                .build();
    }
}
