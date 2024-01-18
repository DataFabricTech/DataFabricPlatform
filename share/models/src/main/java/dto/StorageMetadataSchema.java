package dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storage_metadata_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageMetadataSchema {
    @Id
    @Column(name = "metadata_id")
    private UUID metadataId;
    private String name;
    private String description;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "metadata_id")
    private List<StorageMetadata> storageMetadata = new ArrayList<>();

    @Builder
    public StorageMetadataSchema(UUID metadataId, String name, String description, List<StorageMetadata> storageMetadata) {
        this.metadataId = metadataId;
        this.name = name;
        this.description = description;
        this.storageMetadata = storageMetadata;
    }
}
