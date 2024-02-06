package com.mobigen.datafabric.jpaSample.service;

import com.mobigen.datafabric.dataLayer.service.jpaService.TagService;
import dto.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JpaService {
    @Autowired
    private TagService tagService;

    void saveTag(Tag tag) {
        tagService.save(tag);
    }
}
