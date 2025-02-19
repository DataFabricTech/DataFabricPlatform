package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.api.classification.CreateTag;
import com.mobigen.vdap.schema.entity.classification.Tag;

import static org.openmetadata.service.util.EntityUtil.getEntityReference;

public class TagMapper implements EntityMapper<Tag, CreateTag> {
  @Override
  public Tag createToEntity(CreateTag create, String user) {
    return copy(new Tag(), create, user)
        .withParent(getEntityReference("tag", create.getParent()))
        .withClassification(getEntityReference("classification", create.getClassification()))
        .withProvider(create.getProvider())
            // 이걸 이용해서 일반적인 태그로 사용(true), 카테고리(false)로 사용할 것 인지 설정
        .withMutuallyExclusive(create.getMutuallyExclusive());
  }
}
