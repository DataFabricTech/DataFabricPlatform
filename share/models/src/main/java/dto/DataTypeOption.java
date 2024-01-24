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
    private String key;
    private String value;

    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false, insertable = false)
    private Model model;

    @Builder(toBuilder = true)
    public DataTypeOption(UUID modelId, String key, String value) {
        this.modelId = modelId;
        this.key = key;
        this.value = value;
    }

    @Override
    public Object generateKey() {
        return DataTypeOptionKey.builder()
                .modelId(this.modelId)
                .key(this.key)
                .build();
    }
}
