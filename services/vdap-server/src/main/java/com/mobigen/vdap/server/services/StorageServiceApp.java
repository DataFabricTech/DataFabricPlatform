package com.mobigen.vdap.server.services;

import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.TagLabel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mobigen.vdap.common.utils.CommonUtil.listOrEmpty;

@Slf4j
@Service
public class StorageServiceApp {

    public StorageService create(StorageService request) {
        entity = addHref(uriInfo, repository.create(uriInfo, entity));
        Response.created(entity.getHref()).entity(entity).build();

        validateTags(entity);
        prepare(entity, update);
        setFullyQualifiedName(entity);
        validateExtension(entity);

        return null;
    }

    public void validateTags(StorageService entity) {
        for (TagLabel label : listOrEmpty(entity.getTags())) {
            TagLabelUtil.applyTagCommonFields(label);
        }
        validateTags(entity.getTags());
        entity.setTags(addDerivedTags(entity.getTags()));
        checkMutuallyExclusive(entity.getTags());
        checkDisabledTags(entity.getTags());
    }
}
