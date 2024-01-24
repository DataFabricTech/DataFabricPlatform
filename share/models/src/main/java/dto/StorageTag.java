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
public class StorageTag implements generateKey {
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    @Id
    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false, updatable = false)
    private Storage storage;

    @ManyToOne
    @JoinColumn(name = "tag_id", referencedColumnName = "tag_id", insertable = false, updatable = false)
    private Tag tag;

    @Builder(toBuilder = true)
    public StorageTag(UUID storageId, UUID tagId) {
        this.storageId = storageId;
        this.tagId = tagId;
    }

    @Override
    public Object generateKey() {
        return StorageTagKey.builder()
                .storageId(this.storageId)
                .tagId(this.tagId)
                .build();
    }
}
