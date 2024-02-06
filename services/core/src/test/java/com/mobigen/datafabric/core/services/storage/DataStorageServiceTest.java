package com.mobigen.datafabric.core.services.storage;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class DataStorageServiceTest {
//    DataLayerConnection dataLayerConnection = mock(DataLayerConnection.class);
//    DataStorageService dataStorageService = new DataStorageService(dataLayerConnection);
//
//    @Test
//    void searchTest() {
//        var id1 = UUID.randomUUID().toString();
//        var name1 = "연결정보 1";
//        var storageType1 = "postgresql";
//        var status1 = Utilities.Status.CONNECTED;
//        var id2 = UUID.randomUUID().toString();
//        var name2 = "연결정보 2";
//        var storageType2 = "mysql";
//        var status2 = Utilities.Status.DISCONNECTED;
//
//        when(dataLayerConnection.execute(any())).thenReturn(
//                DataLayer.ResExecute.newBuilder()
//                        .setData(DataLayer.ResExecute.Data.newBuilder()
//                                .setTable(DataLayer.Table.newBuilder()
//                                        .addAllColumns(List.of(
//                                                DataLayer.Column.newBuilder()
//                                                        .setColumnName("id")
//                                                        .setType(Utilities.DataType.STRING)
//                                                        .build(),
//                                                DataLayer.Column.newBuilder()
//                                                        .setColumnName("name")
//                                                        .setType(Utilities.DataType.STRING)
//                                                        .build(),
//                                                DataLayer.Column.newBuilder()
//                                                        .setColumnName("status")
//                                                        .setType(Utilities.DataType.STRING)
//                                                        .build(),
//                                                DataLayer.Column.newBuilder()
//                                                        .setColumnName("storage_type_name")
//                                                        .setType(Utilities.DataType.STRING)
//                                                        .build()
//                                        ))
//                                        .addAllRows(List.of(
//                                                DataLayer.Row.newBuilder()
//                                                        .addAllCell(List.of(
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(0)
//                                                                        .setStringValue(id1)
//                                                                        .build(),
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(1)
//                                                                        .setStringValue(name1)
//                                                                        .build(),
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(2)
//                                                                        .setStringValue(status1.name())
//                                                                        .build(),
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(3)
//                                                                        .setStringValue(storageType1)
//                                                                        .build()
//                                                        ))
//                                                        .build(),
//                                                DataLayer.Row.newBuilder()
//                                                        .addAllCell(List.of(
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(0)
//                                                                        .setStringValue(id2)
//                                                                        .build(),
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(1)
//                                                                        .setStringValue(name2)
//                                                                        .build(),
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(2)
//                                                                        .setStringValue(status2.name())
//                                                                        .build(),
//                                                                DataLayer.Cell.newBuilder()
//                                                                        .setColumnIndex(3)
//                                                                        .setStringValue(storageType2)
//                                                                        .build()
//                                                        ))
//                                                        .build()
//                                        ))
//                                        .build())
//                                .build())
//                        .build()
//        );
//
//        var expect = List.of(
//                StorageOuterClass.Storage.newBuilder()
//                        .setId(id1)
//                        .setName(name1)
//                        .setStorageType(storageType1)
//                        .setStatus(status1)
//                        .build(),
//                StorageOuterClass.Storage.newBuilder()
//                        .setId(id2)
//                        .setName(name2)
//                        .setStorageType(storageType2)
//                        .setStatus(status2)
//                        .build()
//        );
//        var filter = StorageOuterClass.StorageSearchFilter.newBuilder().build();
//        var sorts = List.of(Utilities.Sort.newBuilder().build());
//        var result = dataStorageService.search(filter, sorts);
//        assertEquals(expect, result);
//    }
//
//    @Test
//    void statusTest() {
//    }
//
//    @Test
//    void addStorageTest() {
//    }
//
//    @Test
//    void deleteStorageTest() {
//    }
}