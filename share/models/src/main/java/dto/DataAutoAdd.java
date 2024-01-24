package dto;

import dto.enums.FormatType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "data_auto_add")
public class DataAutoAdd implements generateKey{
    @Id
    @Column(name = "storage_id", nullable = false)
    private UUID storageId;
    private int num;
    private String regex;
    @Enumerated(EnumType.STRING)
    @Column(name = "format_type")
    private FormatType formatType;
    @ManyToOne
    @JoinColumn(name = "storage_id", insertable = false,updatable = false)
    private Storage storage;

    @Builder(toBuilder = true)
    public DataAutoAdd(UUID storageId, int num, String regex, FormatType formatType) {
        this.storageId = storageId;
        this.num = num;
        this.regex = regex;
        this.formatType = formatType;
    }

    @Override
    public Object generateKey() {
        return this.storageId;
    }
}
