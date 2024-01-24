package dto;

import dto.enums.AdaptorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storage_adaptor_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageAdaptorSchema implements generateKey{
    @Id
    @Column(name = "adaptor_id", nullable = false)
    private UUID adaptorId;
    private String name;
    @Column(name = "adaptor_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdaptorType adaptorType;
    @Lob
    private byte[] logo;
    private boolean enable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storageAdaptorSchema")
    private List<Storage> storage = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storageAdaptorSchema")
    private List<StorageAdaptorConnInfoSchema> storageAdaptorConnInfoSchemas = new ArrayList<>();

    @Builder(toBuilder = true)
    public StorageAdaptorSchema(UUID adaptorId, String name, AdaptorType adaptorType, byte[] logo, boolean enable,
                                List<Storage> storage,
                                List<StorageAdaptorConnInfoSchema> storageAdaptorConnInfoSchemas) {
        this.adaptorId = adaptorId;
        this.name = name;
        this.adaptorType = adaptorType;
        this.logo = logo;
        this.enable = enable;
        this.storage = storage;
        this.storageAdaptorConnInfoSchemas = storageAdaptorConnInfoSchemas;
    }

    @Override
    public Object generateKey() {
        return this.adaptorId;
    }
}
