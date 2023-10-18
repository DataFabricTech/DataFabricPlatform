//package com.mobigen.datafabric.core.old.models.entities;
//
//import jakarta.persistence.*;
//
//import java.util.UUID;
//
//@Entity
//@Table(name = "DataStorage")
//public class DataStorageEntity {
//    @Id
//    @GeneratedValue
//    UUID id;
//    @ManyToOne(optional = false)
//    @JoinColumn(name = "type", referencedColumnName = "id",
//            foreignKey = @ForeignKey(name = "DataStorageType_fk"))
//    DataStorageTypeEntity type;
//    String name;
//}
