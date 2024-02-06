package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.service.jpaService.StorageMetadataSchemaService;
import dto.StorageMetadataSchema;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class MultiThreadingTest {
    @Autowired
    private StorageMetadataSchemaService storageMetadataSchemaService;

    @DisplayName("multi threading with two same repository success test")
    @Test
    void multiThreadingWithTwoRepository() {
        final UUID metadataId = UUID.randomUUID();
        final UUID multiMetadataId = UUID.randomUUID();
        var firstThread = new Thread(() -> {
            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaService.save(storageMetadataSchema);
        });

        var secondThread = new Thread(() -> {
            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(multiMetadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaService.save(storageMetadataSchema);
        });

        assertDoesNotThrow(() -> {
            firstThread.start();
            secondThread.start();
        });
    }

    @DisplayName("multi threading with two same repository and same key fail test")
    @Test
    void multiThreadingWithSameKeyRepository() {
        final UUID metadataId = UUID.randomUUID();
        var firstThread = new Thread(() -> {
            try {
                var storageMetadataSchema = StorageMetadataSchema.builder()
                        .metadataId(metadataId)
                        .name("example_storage_metadata_schema")
                        .description("example_storage_metadata_schema_description")
                        .build();

                storageMetadataSchemaService.save(storageMetadataSchema);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        var secondThread = new Thread(() -> {
            try {
                var storageMetadataSchema = StorageMetadataSchema.builder()
                        .metadataId(metadataId)
                        .name("example_storage_metadata_schema")
                        .description("example_storage_metadata_schema_description")
                        .build();

                storageMetadataSchemaService.save(storageMetadataSchema);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThrows(RuntimeException.class, () -> {
            firstThread.run();
            secondThread.run();
        });
    }
}
