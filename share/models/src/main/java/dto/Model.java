package dto;

import dto.enums.DataType;
import dto.enums.StatusType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "model")
public class Model implements generateKey{
    @Id
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Column(unique = true)
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private UUID createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    @Column(name = "modified_by")
    private UUID modifiedBy;
    @Column(name = "sync_enable")
    private Boolean syncEnable;
    @Column(name = "sync_time")
    private String syncTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sync_at")
    private LocalDateTime syncAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<ModelMetadata> modelMetadatas = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<ColumnMetadata> columnMetadatas = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<DataSample> dataSamples = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<ModelRatingAndComment> modelRatingAndComments = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<ModelTag> modelTags = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<ModelFeedback> modelFeedbacks = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<ModelRelation> modelRelations = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "model")
    private List<DataTypeOption> dataTypeOptions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "data_type", insertable = false,updatable = false)
    private DataTypeSchema dataTypeSchema;
    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false,updatable = false)
    private Storage storage;

    @Builder(toBuilder = true)
    public Model(UUID modelId, String name, String description, DataType dataType, UUID storageId,
                 StatusType status, LocalDateTime createdAt, UUID createdBy, LocalDateTime modifiedAt,
                 UUID modifiedBy, Boolean syncEnable, String syncTime, LocalDateTime syncAt,
                 List<ModelMetadata> modelMetadatas, List<ColumnMetadata> columnMetadatas,
                 List<DataSample> dataSamples, List<ModelRatingAndComment> modelRatingAndComments,
                 List<ModelTag> modelTags, List<ModelFeedback> modelFeedbacks,
                 List<ModelRelation> modelRelations, List<DataTypeOption> dataTypeOptions) {
        this.modelId = modelId;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.storageId = storageId;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
        this.syncEnable = syncEnable;
        this.syncTime = syncTime;
        this.syncAt = syncAt;
        this.modelMetadatas = modelMetadatas == null ? new ArrayList<>():modelMetadatas;
        this.columnMetadatas = columnMetadatas == null ? new ArrayList<>():columnMetadatas;
        this.dataSamples = dataSamples == null ? new ArrayList<>():dataSamples;
        this.modelRatingAndComments = modelRatingAndComments == null ? new ArrayList<>():modelRatingAndComments;
        this.modelTags = modelTags == null ? new ArrayList<>():modelTags;
        this.modelFeedbacks = modelFeedbacks == null ? new ArrayList<>():modelFeedbacks;
        this.modelRelations = modelRelations == null ? new ArrayList<>():modelRelations;
        this.dataTypeOptions = dataTypeOptions == null ? new ArrayList<>():dataTypeOptions;
    }

    @Override
    public Object generateKey() {
        return this.modelId;
    }
}
