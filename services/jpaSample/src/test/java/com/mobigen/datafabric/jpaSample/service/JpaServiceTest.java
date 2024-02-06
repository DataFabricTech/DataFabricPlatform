package com.mobigen.datafabric.jpaSample.service;

import com.mobigen.datafabric.dataLayer.service.jpaService.JpaService;
import com.mobigen.datafabric.dataLayer.service.jpaService.TagService;
import dto.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JpaServiceTest {
    @Autowired
    private TagService tagService;

    @DisplayName("Entity Name을 알 경우에 사용할 수 있는 Test")
    @Test
    void knowTheEntityName() {
        var tag = Tag.builder()
                .tagId(UUID.randomUUID())
                .tagValue("tag Service")
                .build();

        assertDoesNotThrow(() -> {
            tagService.save(tag);
        });
    }

    @Autowired
    private JpaService<Tag, UUID> tagService2;

    @DisplayName("Entity Name과 Key가 뭔지 알 경우에 사용할 수 있는 Test")
    @Test
    void knowTheEntityAndKeyName() {
        var tag = Tag.builder()
                .tagId(UUID.randomUUID())
                .tagValue("JpaService 2")
                .build();

        assertDoesNotThrow(() -> {
            tagService2.save(tag);
        });
    }
}