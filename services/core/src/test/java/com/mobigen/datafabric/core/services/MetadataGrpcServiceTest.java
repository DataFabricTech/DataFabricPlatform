package com.mobigen.datafabric.core.services;

import com.mobigen.datafabric.share.interfaces.MetadataSchema;
import com.mobigen.datafabric.share.interfaces.MetadataSchemaServiceGrpc;
import com.mobigen.datafabric.share.interfaces.ReqId;
import com.mobigen.datafabric.share.interfaces.ReqName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Must be running with Core Application")
@Disabled
class MetadataGrpcServiceTest {
    ManagedChannel channel;
    MetadataSchemaServiceGrpc.MetadataSchemaServiceBlockingStub stub;
    final String modelMeta = "modelMeta";
    final String storageMeta = "storageMeta";

    @BeforeEach
    void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        stub = MetadataSchemaServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        var models = stub.getModelMetadataSchemas(ReqName.newBuilder().setName(modelMeta)
                .build()).getData().getMetadataSchemaList();
        for (var i : models) {
            stub.deleteModelMetadataSchema(ReqId.newBuilder()
                    .setId(i.getMetadataId())
                    .build());
        }

        var storages = stub.getStorageMetadataSchemas(ReqName.newBuilder().setName(storageMeta)
                .build()).getData().getMetadataSchemaList();
        for (var i : storages) {
            stub.deleteStorageMetadataSchema(ReqId.newBuilder()
                    .setId(i.getMetadataId())
                    .build());
        }

        storages = stub.getStorageMetadataSchemas(ReqName.newBuilder().setName(modelMeta)
                .build()).getData().getMetadataSchemaList();
        for (var i : storages) {
            stub.deleteStorageMetadataSchema(ReqId.newBuilder()
                    .setId(i.getMetadataId())
                    .build());
        }

        channel.shutdown();
    }
    @Test
    void addStorageMetadataSchema() {
        assertDoesNotThrow(() -> {
            stub.addStorageMetadataSchema(setMetadata(storageMeta));

            var response = stub.getStorageMetadataSchemas(ReqName.newBuilder()
                    .setName(storageMeta).build());

            assertEquals(storageMeta, response.getData().getMetadataSchema(0).getDescription());
        });
    }

    @Test
    void addModelMetadataSchema() {
        assertDoesNotThrow(() -> {
            stub.addModelMetadataSchema(setMetadata(modelMeta));

            var response = stub.getModelMetadataSchemas(ReqName.newBuilder()
                    .setName(modelMeta).build());

            assertEquals(1, response.getData().getMetadataSchemaList().size());
        });
    }

    @Test
    void addStorageMetadataSchemas() {
        assertDoesNotThrow(() -> {
            stub.addStorageMetadataSchema(setMetadata(storageMeta));
            stub.addStorageMetadataSchema(setMetadata(storageMeta));
            stub.addStorageMetadataSchema(setMetadata(storageMeta));

            var response = stub.getStorageMetadataSchemas(ReqName.newBuilder()
                    .setName(storageMeta).build());

            assertEquals(3, response.getData().getMetadataSchemaList().size());
        });
    }

    @Test
    void addModelMetadataSchemas() {
        assertDoesNotThrow(() -> {
            stub.addModelMetadataSchema(setMetadata(modelMeta));
            stub.addModelMetadataSchema(setMetadata(modelMeta));
            stub.addModelMetadataSchema(setMetadata(modelMeta));

            var response = stub.getModelMetadataSchemas(ReqName.newBuilder()
                    .setName(modelMeta).build());

            assertEquals(3, response.getData().getMetadataSchemaList().size());
        });
    }

    @Test
    void getStorageMetadataSchemas() {
        assertDoesNotThrow(() -> {
            stub.addStorageMetadataSchema(setMetadata(storageMeta));

            var response = stub.getStorageMetadataSchemas(ReqName.newBuilder()
                    .setName(storageMeta).build());

            assertEquals(storageMeta, response.getData().getMetadataSchema(0).getDescription());
            assertEquals(storageMeta, response.getData().getMetadataSchema(0).getName());
            assertEquals(1, response.getData().getMetadataSchemaList().size());
        });
    }

    @Test
    void getModelMetadataSchemas() {
        assertDoesNotThrow(() -> {
            stub.addModelMetadataSchema(setMetadata(modelMeta));

            var response = stub.getModelMetadataSchemas(ReqName.newBuilder()
                    .setName(modelMeta).build());

            assertEquals(modelMeta, response.getData().getMetadataSchema(0).getDescription());
            assertEquals(modelMeta, response.getData().getMetadataSchema(0).getName());
            assertEquals(1, response.getData().getMetadataSchemaList().size());
        });
    }

    @Test
    void updateStorageMetadataSchema() {
        assertDoesNotThrow(() -> {
            stub.addStorageMetadataSchema(setMetadata(storageMeta));

            var response = stub.getStorageMetadataSchemas(ReqName.newBuilder().setName(storageMeta).build())
                    .getData().getMetadataSchema(0);
            var update = response.toBuilder().setName(modelMeta).build();

            stub.updateStorageMetadataSchema(update);

            response = stub.getStorageMetadataSchemas(ReqName.newBuilder().setName(modelMeta).build())
                    .getData().getMetadataSchema(0);
            assertEquals(modelMeta, response.getName());
        });
    }

    @Test
    void updateModelMetadataSchema() {
        assertDoesNotThrow(() -> {
            stub.addModelMetadataSchema(setMetadata(modelMeta));

            var response = stub.getModelMetadataSchemas(ReqName.newBuilder().setName(modelMeta).build())
                    .getData().getMetadataSchema(0);
            var update = response.toBuilder().setName(storageMeta).build();

            stub.updateModelMetadataSchema(update);

            response = stub.getModelMetadataSchemas(ReqName.newBuilder().setName(storageMeta).build())
                    .getData().getMetadataSchema(0);
            assertEquals(storageMeta, response.getName());
        });
    }

    @Test
    void deleteStorageMetadataSchema() {
        assertDoesNotThrow(() -> {
            stub.addStorageMetadataSchema(setMetadata(storageMeta));

            var response = stub.getStorageMetadataSchemas(ReqName.newBuilder().setName(storageMeta).build())
                    .getData().getMetadataSchema(0);

            stub.deleteStorageMetadataSchema(ReqId.newBuilder()
                    .setId(response.getMetadataId())
                    .build());
        });
    }

    @Test
    void deleteModelMetadataSchema() {
        assertDoesNotThrow(() -> {
            stub.addModelMetadataSchema(setMetadata(modelMeta));

            var response = stub.getModelMetadataSchemas(ReqName.newBuilder().setName(modelMeta).build())
                    .getData().getMetadataSchema(0);

            stub.deleteStorageMetadataSchema(ReqId.newBuilder()
                    .setId(response.getMetadataId())
                    .build());
        });
    }

    MetadataSchema setMetadata(String name) {
        return MetadataSchema.newBuilder()
                .setMetadataId(UUID.randomUUID().toString())
                .setName(name)
                .setDescription(name)
                .build();
    }
}