package dto;

import dto.compositeKeys.ColumnMetadataKey;
import dto.enums.ColumnType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "column_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ColumnMetadataKey.class)
public class ColumnMetadata implements generateKey {
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Id
    @Column(name = "num", nullable = false)
    private int num;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "column_type")
    private ColumnType columnType;
    private Long length;
    @Column(name = "is_pk")
    private boolean isPK;
    @Column(name = "is_fk")
    private boolean isFK;
    private boolean nullable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "columnMetadata")
    private List<TableDataQuality> tableDataQualities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "model_id", insertable = false, updatable = false)
    private Model model;

    @Builder(toBuilder = true)
    public ColumnMetadata(UUID modelId, int num, String name, String description, ColumnType columnType, Long length,
                          boolean isPK, boolean isFK, boolean nullable, List<TableDataQuality> tableDataQualities) {
        this.modelId = modelId;
        this.num = num;
        this.name = name;
        this.description = description;
        this.columnType = columnType;
        this.length = length;
        this.isPK = isPK;
        this.isFK = isFK;
        this.nullable = nullable;
        this.tableDataQualities = tableDataQualities == null? new ArrayList<>():tableDataQualities;
    }

    @Override
    public Object generateKey() {
        return ColumnMetadataKey.builder()
                .num(this.num)
                .modelId(this.modelId)
                .build();
    }
}
