package com.mobigen.datafabric.core.services;

import com.mobigen.datafabric.share.interfaces.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Must be running with Core Application")
@Disabled
class AdaptorGrpcServiceTest {
    ManagedChannel channel;
    AdaptorServiceGrpc.AdaptorServiceBlockingStub stub;
    final UUID testId = UUID.randomUUID();
    final UUID testId2 = UUID.randomUUID();

    @BeforeEach
    void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        stub = AdaptorServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        var adaptors = stub.getAllAdaptors(null).getData().getStorageAdaptorSchemaList();
        for (var i : adaptors) {
            stub.deleteAdaptor(ReqId.newBuilder().setId(i.getAdaptorId()).build());
        }

        channel.shutdown();
    }

    @DisplayName("Add one Adaptor")
    @Test
    void addAdaptor() {
        // given
        var storageAdaptorSchema = setAdaptor(testId.toString());

        // when
        var response = stub.addAdaptor(storageAdaptorSchema);

        // then
        assertEquals("success", response.getCode());
    }


    @Test
    void getAdaptor() {
        // given
        var storageAdaptorSchema = setAdaptor(testId.toString());
        assertDoesNotThrow(() -> stub.addAdaptor(storageAdaptorSchema));

        assertDoesNotThrow(() -> {
            // when
            var response = stub.getAdaptor(ReqId.newBuilder().setId(testId.toString()).build());

            // then
            assertEquals(testId.toString(), response.getStorageAdaptorSchema().getAdaptorId());
        });

    }

    @Test
    void getAdaptors() {
        // given
        assertDoesNotThrow(() -> stub.addAdaptor(
                StorageAdaptorSchema.newBuilder()
                        .setAdaptorId(testId.toString())
                        .setName("test1")
                        .setType(AdaptorType.POSTGRES)
                        .setEnable(true)
                        .build()
        ));

        assertDoesNotThrow(() -> stub.addAdaptor(
                StorageAdaptorSchema.newBuilder()
                        .setAdaptorId(testId2.toString())
                        .setName("test2")
                        .setType(AdaptorType.MARIADB)
                        .setEnable(true)
                        .build()
        ));
        assertDoesNotThrow(() -> {
            // when
            var response = stub.getAdaptors(ReqGetAdaptors.newBuilder()
                    .setName("test1")
                    .build());

            // then
            assertEquals("success", response.getCode());
            assertEquals(1, response.getData().getStorageAdaptorSchemaList().size());
            assertTrue(response.getData().getStorageAdaptorSchema(0).getEnable());
            assertEquals(testId.toString(), response.getData().getStorageAdaptorSchema(0).getAdaptorId());
            assertEquals("test1", response.getData().getStorageAdaptorSchema(0).getName());

            // when
            response = stub.getAdaptors(ReqGetAdaptors.newBuilder()
                    .setAdaptorType(AdaptorType.MARIADB)
                    .build());

            // then
            assertEquals("success", response.getCode());
            assertEquals(1, response.getData().getStorageAdaptorSchemaList().size());
            assertTrue(response.getData().getStorageAdaptorSchema(0).getEnable());
            assertEquals(testId2.toString(), response.getData().getStorageAdaptorSchema(0).getAdaptorId());
            assertEquals("test2", response.getData().getStorageAdaptorSchema(0).getName());

            // when
            response = stub.getAdaptors(ReqGetAdaptors.newBuilder()
                    .setEnable(true)
                    .build());

            // then
            assertEquals("success", response.getCode());
            assertEquals(2, response.getData().getStorageAdaptorSchemaList().size());
        });
    }

    @Test
    void getAllAdaptors() {
        // given
        assertDoesNotThrow(() -> {
            stub.addAdaptor(setAdaptor());
            stub.addAdaptor(setAdaptor());
            stub.addAdaptor(setAdaptor());
            stub.addAdaptor(setAdaptor());
        });

        assertDoesNotThrow(() -> {
            assertEquals(4, stub.getAllAdaptors(null).getData()
                    .getStorageAdaptorSchemaList().size());

            assertEquals(1, stub.getAllAdaptors(ReqGetAdaptors.newBuilder()
                            .setPageable(Pageable.newBuilder()
                                    .setPage(Page.newBuilder()
                                            .setSelectPage(1)
                                            .setSize(1)
                                            .build())
                                    .build())
                            .build())
                    .getData().getStorageAdaptorSchemaList().size());
        });
    }

    @Test
    void updateAdaptor() {
        assertDoesNotThrow(() -> stub.addAdaptor(setAdaptor()));

        assertDoesNotThrow(() -> {
            var response = stub.getAllAdaptors(null).getData().getStorageAdaptorSchemaList().get(0);
            var updated = response.toBuilder().setEnable(false).build();
            stub.updateAdaptor(updated);
        });

        assertDoesNotThrow(() -> {
            var response = stub.getAllAdaptors(null).getData().getStorageAdaptorSchemaList().get(0);
            assertFalse(response.getEnable());
        });
    }

    @Test
    void deleteAdaptor() {
        assertDoesNotThrow(() -> stub.addAdaptor(setAdaptor()));

        assertDoesNotThrow(() -> {
            var response = stub.getAllAdaptors(null).getData().getStorageAdaptorSchemaList().get(0);
            stub.deleteAdaptor(ReqId.newBuilder()
                    .setId(response.getAdaptorId())
                    .build());

            assertEquals(0, stub.getAllAdaptors(null).getData().getStorageAdaptorSchemaList().size());
        });
    }

    StorageAdaptorSchema setAdaptor(String id) {
        ArrayList<StorageAdaptorConnInfoSchema> storageAdaptorConnInfoSchemaList = new ArrayList<>() {{
            add(StorageAdaptorConnInfoSchema.newBuilder()
                    .setType("add Adaptor Type")
                    .setAdaptorConnSchemaKey("add Adaptor Type")
                    .setAdaptorConnSchemaValue("add Adaptor Type")
                    .setDataType(DataType.STRING)
                    .setDefaultValue("add Adaptor Type")
                    .setDesc("add Adaptor Type")
                    .setRequired(true)
                    .build());
            add(StorageAdaptorConnInfoSchema.newBuilder()
                    .setType("add Adaptor Type2")
                    .setAdaptorConnSchemaKey("add Adaptor Type2")
                    .setAdaptorConnSchemaValue("add Adaptor Type2")
                    .setDataType(DataType.STRING)
                    .setDefaultValue("add Adaptor Type2")
                    .setDesc("add Adaptor Type2")
                    .setRequired(true)
                    .build());
        }};
        StorageAdaptorSchema storageAdaptorSchema = StorageAdaptorSchema.newBuilder()
                .setAdaptorId(id)
                .setName("addAdaptorTest name")
                .setType(AdaptorType.POSTGRES)
                .setEnable(true)
                .addAllStorageAdaptorConnInfoSchema(storageAdaptorConnInfoSchemaList)
                .build();

        return storageAdaptorSchema;
    }

    StorageAdaptorSchema setAdaptor() {
        ArrayList<StorageAdaptorConnInfoSchema> storageAdaptorConnInfoSchemaList = new ArrayList<>() {{
            add(StorageAdaptorConnInfoSchema.newBuilder()
                    .setType("add Adaptor Type")
                    .setAdaptorConnSchemaKey("add Adaptor Type")
                    .setAdaptorConnSchemaValue("add Adaptor Type")
                    .setDataType(DataType.STRING)
                    .setDefaultValue("add Adaptor Type")
                    .setDesc("add Adaptor Type")
                    .setRequired(true)
                    .build());
            add(StorageAdaptorConnInfoSchema.newBuilder()
                    .setType("add Adaptor Type2")
                    .setAdaptorConnSchemaKey("add Adaptor Type2")
                    .setAdaptorConnSchemaValue("add Adaptor Type2")
                    .setDataType(DataType.STRING)
                    .setDefaultValue("add Adaptor Type2")
                    .setDesc("add Adaptor Type2")
                    .setRequired(true)
                    .build());
        }};
        StorageAdaptorSchema storageAdaptorSchema = StorageAdaptorSchema.newBuilder()
                .setAdaptorId(UUID.randomUUID().toString())
                .setName("addAdaptorTest name")
                .setType(AdaptorType.POSTGRES)
                .setEnable(true)
                .addAllStorageAdaptorConnInfoSchema(storageAdaptorConnInfoSchemaList)
                .build();

        return storageAdaptorSchema;
    }

}