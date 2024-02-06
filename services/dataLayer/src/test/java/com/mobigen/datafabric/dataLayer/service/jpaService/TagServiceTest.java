package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.StorageMetadataSchema;
import dto.Tag;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class TagServiceTest {
    @Autowired
    private TagService tagService;

    @Autowired
    private JpaService<Tag, UUID> jpaService;

    private UUID tagId;

    private Tag tag;
    private String tagValue;

    @BeforeEach
    void init() {
        this.tagId = UUID.randomUUID();
        this.tagValue = "test Tag Value";
        tag = Tag.builder()
                .tagId(tagId)
                .tagValue(tagValue)
                .build();
    }

    @DisplayName("save success test V1 - TagService")
    @Test
    void successSaveTestV1() {
        assertDoesNotThrow(() -> tagService.save(tag));
    }

    @DisplayName("save same key fail test - V1")
    @Test
    void failSaveWithSameKeyTestV1() {
        assertThrows(IllegalStateException.class, () -> {
            tagService.save(tag);
            var newTag = Tag.builder()
                    .tagId(tagId)
                    .tagValue("newTag")
                    .build();
            tagService.save(newTag);
        });
    }

    @DisplayName("save null Entity fail test - V1")
    @Test
    void failSaveNullEntityTestV1() {
        assertThrows(NullPointerException.class, () -> tagService.save(null));
    }

    @DisplayName("update success test - V1")
    @Test
    void successUpdateTestV1() {
        assertDoesNotThrow(() -> {
            tagService.save(tag);
            var found = tagService.findById(tagId);
            assertDoesNotThrow(() -> {
                var oldTag = found.get();
                var updateTag = oldTag.toBuilder().tagValue("update value").build();
                tagService.update(updateTag);
            });

            assertEquals(1, tagService.findAll().size());

            var updated = tagService.findById(tagId);
            assertDoesNotThrow(() -> {
                assertEquals("update value", updated.get().getTagValue());
            });
        });
    }

    @DisplayName("update nothing test - V1")
    @Test
    void whatUpdateNothingEntityTestV1() {
        assertDoesNotThrow(() -> {
            tagService.save(tag);
            var found = tagService.findById(tagId);
            assertDoesNotThrow(() -> {
                var oldTag = found.get();
                var updateTag = oldTag.toBuilder().build();
                tagService.update(updateTag);
            });

            assertEquals(1, tagService.findAll().size());

            var updated = tagService.findById(tagId);
            assertDoesNotThrow(() -> {
                assertEquals(tagId, updated.get().getTagId());
                assertEquals("test Tag Value", updated.get().getTagValue());
            });
        });
    }

    @DisplayName("save success test V2 - JpaService ")
    @Test
    void successSaveTestV2() {
        assertDoesNotThrow(() -> jpaService.save(tag));
        assertDoesNotThrow(() -> jpaService.findById(tagId));
    }

    @DisplayName("update success test - V2")
    @Test
    void successUpdateTestV2() {
        assertThrows(NullPointerException.class, () -> jpaService.save(null));
    }

    @DisplayName("update success test - V2")
    @Test
    void failUpdateTestV2() {
        assertDoesNotThrow(() -> {
            jpaService.save(tag);
            var found = jpaService.findById(tagId);
            assertDoesNotThrow(() -> {
                var oldTag = found.get();
                var updateTag = oldTag.toBuilder().tagValue("update value").build();
                jpaService.update(updateTag);
            });

            assertEquals(1, jpaService.findAll().size());

            var updated = jpaService.findById(tagId);
            assertDoesNotThrow(() -> {
                assertEquals("update value", updated.get().getTagValue());
            });
        });
    }

    @DisplayName("update nothing test - V2")
    @Test
    void whatUpdateNothingEntityTestV2() {
        assertDoesNotThrow(() -> {
            jpaService.save(tag);
            var found = jpaService.findById(tagId);
            assertDoesNotThrow(() -> {
                var oldTag = found.get();
                var updateTag = oldTag.toBuilder().build();
                jpaService.update(updateTag);
            });

            assertEquals(1, jpaService.findAll().size());

            var updated = jpaService.findById(tagId);
            assertDoesNotThrow(() -> {
                assertEquals(tagId, updated.get().getTagId());
                assertEquals("test Tag Value", updated.get().getTagValue());
            });
        });
    }

    @DisplayName("find success test")
    @Test
    void findByIdTest() {
        assertDoesNotThrow(() -> {
            tagService.save(tag);
            assertEquals(1, tagService.findAll().size());
            assertEquals(tagValue, tagService.findById(tagId).get().getTagValue());
        });
    }

    @DisplayName("empty entity find test")
    @Test
    void findEmptyTest() {
        assertDoesNotThrow(() -> {
            var empty = tagService.findById(UUID.randomUUID());
            if (empty.isPresent())
                throw new Exception("Unknown Exception");
        });

    }

    @DisplayName("find all entity success test")
    @Test
    void findAllTest() {
        assertDoesNotThrow(() -> {
            tagService.save(tag);
            var newTag = Tag.builder()
                    .tagId(UUID.randomUUID())
                    .tagValue("new Tag")
                    .build();
            tagService.save(newTag);
        });
    }

    @DisplayName("find all entity success test")
    @Test
    void findAllEmptyTest() {
        assertDoesNotThrow(() -> assertEquals(0, tagService.findAll().size()));
    }

    @DisplayName("delete by id success test")
    @Test
    void deleteByIdTest() {
        assertDoesNotThrow(() -> {
            tagService.save(tag);
            tagService.deleteById(tagId);
        });
    }

    @DisplayName("delete by id not exist entity test")
    @Test
    void deleteByIdNullEntityTest() {
        assertDoesNotThrow(() -> {
            tagService.deleteById(tagId);
        });
    }

    @DisplayName("delete with entity success test")
    @Test
    void deleteWithEntityTest() {
        assertDoesNotThrow(() -> {
            tagService.save(tag);
            tagService.delete(tag);
        });
    }

    @DisplayName("delete not exist entity test")
    @Test
    void deleteWithEntityNotExistTest() {
        assertDoesNotThrow(() -> {
            tagService.delete(tag);
        });
    }

    @DisplayName("generate key success test")
    @Test
    void testGenerateKey() {
        final UUID metadataId = UUID.randomUUID();
        var storageMetadataSchema = StorageMetadataSchema.builder()
                .metadataId(metadataId)
                .name("example_storage_metadata_schema")
                .description("example_storage_metadata_schema_description")
                .build();

        assertEquals(metadataId, storageMetadataSchema.generateKey());
    }
}