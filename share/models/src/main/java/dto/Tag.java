package dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag implements generateKey {
    @Id
    @Column(name = "tag_id")
    private UUID tagId;
    private String value;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tag")
    private List<ModelTag> modelTags;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tag")
    private List<StorageTag> storageTags;

    @Builder(toBuilder = true)
    public Tag(UUID tagId, String value) {
        this.tagId = tagId;
        this.value = value;
    }

    @Override
    public Object generateKey() {
        return this.tagId;
    }
}
