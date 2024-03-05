package com.mobigen.datafabric.core.services;

import com.mobigen.datafabric.core.util.Converter;
import com.mobigen.datafabric.libs.rabbitmq.Client;
import com.mobigen.datafabric.libs.rabbitmq.Configuration;
import com.mobigen.datafabric.libs.rabbitmq.ExchangeType;
import com.mobigen.datafabric.libs.rabbitmq.Worker;
import com.mobigen.datafabric.share.interfaces.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Must be running with Core Application")
//@Disabled
class StorageGrpcServiceTest {
    ManagedChannel channel;
    StorageServiceGrpc.StorageServiceBlockingStub stub;
    AdaptorServiceGrpc.AdaptorServiceBlockingStub adaptorStub;
    MetadataSchemaServiceGrpc.MetadataSchemaServiceBlockingStub metadataStub;
    final UUID adaptorId = UUID.randomUUID();
    final UUID storageId = UUID.randomUUID();
    final UUID storageId2 = UUID.randomUUID();
    final UUID metadataId = UUID.randomUUID();
    final int MQ_PORT = 5672;
    final String MQ_QUEUE_NAME = "test";
    final String MQ_URL = "192.168.106.104";
    final int MQ_QUEUE_ID = 2;

    @BeforeEach
    void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        stub = StorageServiceGrpc.newBlockingStub(channel);
        adaptorStub = AdaptorServiceGrpc.newBlockingStub(channel);
        metadataStub = MetadataSchemaServiceGrpc.newBlockingStub(channel);

        StorageAdaptorSchema storageAdaptorSchema = StorageAdaptorSchema.newBuilder()
                .setAdaptorId(adaptorId.toString())
                .setName("addAdaptorTest name")
                .setType(AdaptorType.POSTGRES)
                .setEnable(true)
                .build();

        MetadataSchema metadataSchema = MetadataSchema.newBuilder()
                .setMetadataId(metadataId.toString())
                .setName("metadata")
                .setDescription("description")
                .build();

        adaptorStub.addAdaptor(storageAdaptorSchema);
        metadataStub.addStorageMetadataSchema(metadataSchema);
    }

    @AfterEach
//    @Disabled
    void tearDown() {
        var storages = stub.getAllStorages(null).getData().getStorageList();
        for (var i : storages) {
            stub.deleteStorage(ReqId.newBuilder().setId(i.getStorageId()).build());
        }

        adaptorStub.deleteAdaptor(ReqId.newBuilder().setId(adaptorId.toString()).build());
        metadataStub.deleteStorageMetadataSchema(ReqId.newBuilder().setId(metadataId.toString()).build());
    }

    @Test
    void dropTable() {

    }

    @Test
    void addStorage() {
        assertDoesNotThrow(() -> {
            var storage = setStorage(storageId.toString());

            var response = stub.addStorage(storage);

            assertEquals("success", response.getCode());
        });
    }

    @Test
    void getStorage() {
        assertDoesNotThrow(() -> {
            var storage = setStorage(storageId.toString());

            stub.addStorage(storage);
        });

        assertDoesNotThrow(() -> {
            var response = stub.getStorage(ReqId.newBuilder().setId(storageId.toString()).build());

            assertEquals(storageId.toString(), response.getStorage().getStorageId());
        });

    }

    @Test
    void getStorages() {
        assertDoesNotThrow(() -> {
            var storage = setStorage(storageId.toString());

            stub.addStorage(storage);

            var storage2 = storage.toBuilder()
                    .setStorageId(storageId2.toString())
                    .setName("test2")
                    .setDescription("storage test description v2").build();

            stub.addStorage(storage2);
        });

        assertDoesNotThrow(() -> {
            // when
            var response = stub.getStorages(ReqGetStorages.newBuilder()
                    .setName("unitTest")
                    .build());

            assertEquals("success", response.getCode());
            assertEquals(1, response.getData().getStorageList().size());
            assertTrue(response.getData().getStorage(0).getSyncEnable());
            assertEquals(storageId.toString(), response.getData().getStorage(0).getStorageId());
            assertEquals("unitTest", response.getData().getStorage(0).getName());

            // when
            response = stub.getStorages(ReqGetStorages.newBuilder()
                    .setName("test2")
                    .build());

            assertEquals("success", response.getCode());
            assertEquals(1, response.getData().getStorageList().size());
            assertEquals(storageId2.toString(), response.getData().getStorage(0).getStorageId());
            assertEquals("test2", response.getData().getStorage(0).getName());

            // when
            response = stub.getStorages(ReqGetStorages.newBuilder()
                    .setStatus(Status.CONNECTED)
                    .build());

            assertEquals("success", response.getCode());
            assertEquals(2, response.getData().getStorageList().size());
        });
    }

    @Test
    void getAllStorages() {
        assertDoesNotThrow(() -> {
            stub.addStorage(setStorage(UUID.randomUUID().toString()));
            stub.addStorage(setStorage(UUID.randomUUID().toString()));
            stub.addStorage(setStorage(UUID.randomUUID().toString()));
            stub.addStorage(setStorage(UUID.randomUUID().toString()));
        });

        assertDoesNotThrow(() -> {
            assertEquals(4, stub.getAllStorages(null).getData()
                    .getStorageList().size());

            assertEquals(1, stub.getAllStorages(ReqGetStorages.newBuilder()
                            .setPageable(Pageable.newBuilder()
                                    .setPage(Page.newBuilder()
                                            .setSelectPage(1)
                                            .setSize(1)
                                            .build())
                                    .build())
                            .build())
                    .getData().getStorageList().size());
        });
    }

    @Test
    void updateStorage() {
        assertDoesNotThrow(() -> stub.addStorage(setStorage(storageId.toString())));

        assertDoesNotThrow(() -> {
            var response = stub.getAllStorages(null).getData().getStorageList().get(0);
            var update = response.toBuilder().setSyncEnable(false).build();
            stub.updateStorage(update);
        });

        assertDoesNotThrow(() -> {
            var response = stub.getAllStorages(null).getData().getStorageList().get(0);
            assertFalse(response.getSyncEnable());
        });
    }

    @Test
    void deleteStorage() {
        assertDoesNotThrow(() -> stub.addStorage(setStorage(storageId.toString())));

        assertDoesNotThrow(() -> {
            var storages = stub.getAllStorages(null).getData().getStorageList();
            for (var i : storages) {
                stub.deleteStorage(ReqId.newBuilder().setId(i.getStorageId()).build());
            }

            assertEquals(0, stub.getAllStorages(null).getData().getStorageList().size());
        });
    }

    @Test
    void connectTest() {
        assertDoesNotThrow(() -> {
            var response = stub.connectTest(ReqId.newBuilder().setId(storageId.toString()).build());

            assertEquals("success", response.getCode());
            assertEquals(Status.CONNECTED, response.getStatus());
        });
    }

    @Test
    void getDataListWithId() {
        assertDoesNotThrow(() -> {
            var response = stub.getDataListWithId(ReqId.newBuilder().setId(storageId.toString()).build());
            var dataList = response.getDataList();
            assertEquals("mock", dataList.getName());
            assertEquals(FormatType.DB, dataList.getFormatType());
            assertTrue(dataList.getHasChildren());

            var childDataList = dataList.getDataList(0);
            assertEquals("mockChild", childDataList.getName());
            assertEquals(FormatType.TABLE, childDataList.getFormatType());
            assertFalse(childDataList.getHasChildren());
        });
    }

    @Test
    void getDataListWithStorage() {
        assertDoesNotThrow(() -> {
            var response = stub.getDataListWithId(ReqId.newBuilder().setId(storageId.toString()).build());
            var dataList = response.getDataList();
            assertEquals("mock", dataList.getName());
            assertEquals(FormatType.DB, dataList.getFormatType());
            assertTrue(dataList.getHasChildren());

            var childDataList = dataList.getDataList(0);
            assertEquals("mockChild", childDataList.getName());
            assertEquals(FormatType.TABLE, childDataList.getFormatType());
            assertFalse(childDataList.getHasChildren());
        });
    }

    @Test
    void extractStorageMetadata() {
        var client = Client.getInstance();
        var worker = new WorkerImpl();
        var conf = Configuration.builder()
                .host(MQ_URL)
                .port(MQ_PORT)
                .queueConfigs(new ArrayList<>() {{
                    add(Configuration.QueueConfig.builder()
                            .isPublisher(false)
                            .exchangeType(ExchangeType.WORK_QUEUE)
                            .queueName(MQ_QUEUE_NAME)
                            .prefetchSize(10)
                            .numChannel(1)
                            .worker(worker)
                            .build());
                }})
                .build();

        assertDoesNotThrow(() -> {
            client.initialize(conf);
            worker.getReceiveCount();

            var response = stub.extractStorageMetadata(ReqId.newBuilder().setId(storageId.toString()).build());
            assertEquals("success", response.getCode());
        });
    }

    @Test
    void overview() {
    }

    private Storage setStorage(String id) {
        var converter = new Converter();
        ArrayList<DataAutoAdd> dataAutoAdds = new ArrayList<>() {
            {
                add(DataAutoAdd.newBuilder()
                        .setNum(1)
                        .setRegex("regex")
                        .setFormatType(FormatType.EXCEL)
                        .build());
            }
        };

        ArrayList<StorageMetadata> storageMatadatas = new ArrayList<>() {
            {
                add(StorageMetadata.newBuilder()
                        .setMetadataId(metadataId.toString())
                        .setMetadataValue("unit test storage metadata value")
                        .build());
            }
        };

        ArrayList<StorageTag> storageTags = new ArrayList<>() {
            {
                add(StorageTag.newBuilder()
                        .setTagId(UUID.randomUUID().toString())
                        .build());
            }
        };

        ArrayList<StorageConnInfo> storageConnInfos = new ArrayList<>() {
            {
                add(StorageConnInfo.newBuilder()
                        .setType("connInfoType")
                        .setStorageConnKey("connInfoKey")
                        .setStorageConnValue("connInfoValue")
                        .setIsOption(true)
                        .build());
            }
        };

        return Storage.newBuilder()
                .setStorageId(id)
                .setAdaptorId(adaptorId.toString())
                .setName("unitTest")
                .setDescription("unit test storage description")
                .setCreatedBy(UUID.randomUUID().toString())
                .setCreatedAt(converter.convert(LocalDateTime.now()))
                .setModifiedBy(UUID.randomUUID().toString())
                .setModifiedAt(converter.convert(LocalDateTime.now()))
                .setStatus(Status.CONNECTED)
                .setLastSyncAt(converter.convert(LocalDateTime.now()))
                .setLastMonitoringAt(converter.convert(LocalDateTime.now()))
                .setSyncEnable(true)
                .setSyncTime("syncTime")
                .setMonitoringEnable(true)
                .setMonitoringPeriod(1)
                .setMonitoringFailThreshold(1)

                .addAllDataAutoAdd(dataAutoAdds)
                .addAllStorageMeta(storageMatadatas)
//                .addAllStorageTag(storageTags)
                .addAllStorageConnInfo(storageConnInfos)
                .build();
    }

    class WorkerImpl implements Worker {
        AtomicInteger receiveCount;

        public WorkerImpl() {
            this.receiveCount = new AtomicInteger(0);
        }

        @Override
        public boolean doWork(String exchange, String routingKey, byte[] message) {
            var msg = new String(message);
            System.out.printf("Thread[ %s ] E[ %s ] R[ %s ] Msg[ %s ] Count[ %d ]\n",
                    Thread.currentThread().getName(), exchange, routingKey, msg, receiveCount.getAndIncrement());
            // 메시지에 'true' 가 포함되었는가 아닌가를 이용해 반환 처리 : true 를 반환하면 consumer ack 를 정상으로 전송
            System.out.println("publish is ok");
            return msg.contains("true");
        }

        public int getReceiveCount() {
            return receiveCount.get();
        }
    }
}