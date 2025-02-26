package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.server.entity.TagEntity;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@NoArgsConstructor
public class TagService {
    private TagRepository tagRepository;

    public EntityReference getReference(UUID id) {
        Optional<TagEntity> entity = tagRepository.findById(id.toString());
        if( entity.isEmpty() ) {
            log.warn("not found tag from id[{}]", id.toString());
            return null;
        }
        Tag tag = JsonUtils.readValue(entity.get().getJson(), Tag.class);
        return new EntityReference()
                .withId(tag.getId())
                .withName(tag.getName())
                .withDisplayName(tag.getDisplayName())
                .withDescription(tag.getDescription());
//                .withHref()
    }
}
