package dto;

import dto.compositeKeys.StorageTagKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "storage_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(StorageTagKey.class)
public class StorageTag {
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    private UUID tag;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false,updatable = false)
    private Storage storage;

    @Builder
    public StorageTag(UUID storageId, UUID tag) {
        this.storageId = storageId;
        this.tag = tag;
    }
}
