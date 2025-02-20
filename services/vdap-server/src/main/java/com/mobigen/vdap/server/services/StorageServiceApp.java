package com.mobigen.vdap.server.services;

import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.server.secrets.SecretsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mobigen.vdap.common.utils.CommonUtil.listOrEmpty;

@Slf4j
@Service
public class StorageServiceApp {

    public void prepareInternal(StorageService entity, boolean update) {
//        validateTags(entity);
        prepare(entity, update);
        setFullyQualifiedName(entity);
        validateExtension(entity);
    }
    public void prepare(StorageService service, boolean update) {
        if (service.getConnection() != null) {
            service
                    .getConnection()
                    .setConfig(
                            new SecretsManager()
                                    .encryptServiceConnectionConfig(
                                            service.getConnection().getConfig(),
                                            service.getServiceType().value(),
                                            service.getName(),
                                            serviceType));
        }
    }

    public StorageService create(StorageService entity) {
//        entity = addHref(uriInfo, repository.create(uriInfo, entity));
//        Response.created(entity.getHref()).entity(entity).build();
//        validateTags(entity);
        prepareInternal(entity, false);

        return null;
    }

//    public StorageService createOrUpdate(StorageService request) {
//
//    }

//    protected void validateTags(StorageService entity) {
//        if (!supportsTags) {
//            return;
//        }
//        validateTags(entity.getTags());
//        entity.setTags(addDerivedTags(entity.getTags()));
//        checkMutuallyExclusive(entity.getTags());
//        checkDisabledTags(entity.getTags());
//    }
//    protected void validateTags(List<TagLabel> labels) {
//        for (TagLabel label : listOrEmpty(labels)) {
//            TagLabelUtil.applyTagCommonFields(label);
//        }
//    }
}
