package dto;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
//    @Id
//    @Column(name = "tag_id")
//    private UUID tagId;
//    private String value;
//
//    @OneToOne
//    @JoinColumn(name = "tag_id")
//    private ModelTag
}
