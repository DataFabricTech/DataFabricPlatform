package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.model.*;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.DataModelOuterClass;
import com.mobigen.datafabric.share.protobuf.UserOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.configuration.Config;
import com.mobigen.sqlgen.where.conditions.Equal;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class DataModelService {

    static Config config = new Config();
    DataLayerConnection dataLayerConnection;

    public DataModelService() {
        this( new DataLayerConnection(
                config.getConfig().getBoolean( "data-layer.test", false )
        ) );
    }

    public DataModelService( DataLayerConnection dataLayerConnection ) {
        this.dataLayerConnection = dataLayerConnection;
    }

    public void NewDataModels( List<DataModelOuterClass.DataModel.Builder> dataModels ) {
        List<String> sqlList = new ArrayList<>();
        // Data Model
        dataModels.forEach( data -> {
            var id = UUID.randomUUID().toString();
            OffsetDateTime createdAt = OffsetDateTime.now( ZoneOffset.UTC );
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss.SSS" );

            var insertDataModel = insert( DataModel.table )
                    .columns(
                            DataModel.id,
                            DataModel.name,
                            DataModel.description,
                            DataModel.type,
                            DataModel.format,
                            DataModel.status,
                            DataModel.createdAt,
                            DataModel.createdBy
                    )
                    .values(
                            id,
                            data.getName(),
                            data.getDescription() == null ? "" : data.getDescription(),
                            data.getDataType(),
                            data.getDataFormat(),
                            data.getStatus(),
                            createdAt.format( dateTimeFormatter ),
                            data.getCreator().getId()
                    )
                    .generate()
                    .getStatement();
            sqlList.add( insertDataModel );
            for( DataModelOuterClass.DataLocation dataLocation : data.getDataLocationList() ) {
                var insertSql = insert( DataModelLocation.table )
                        .columns(
                                DataModelLocation.id,
                                DataModelLocation.storageId,
                                DataModelLocation.path,
                                DataModelLocation.name
                        ).values(
                                id,
                                dataLocation.getStorageId(),
                                dataLocation.getDatabaseName(),
                                dataLocation.getTableName()
                        ).generate().getStatement();
                sqlList.add( insertSql );
            }
            for( Utilities.Meta meta : data.getSystemMetaList() ) {
                var insertSql = insert( DataModelMetadata.table )
                        .columns(
                                DataModelMetadata.id,
                                DataModelMetadata.isSystem,
                                DataModelMetadata.key,
                                DataModelMetadata.value
                        ).values(
                                id,
                                true,
                                meta.getKey(),
                                meta.getValue()
                        ).generate().getStatement();
                sqlList.add( insertSql );
            }
//            if( data.getDataRefine() != null ) {
//
//            }
            for( DataModelOuterClass.DataStructure dataStructure : data.getDataStructureList() ) {
                var insertSql = insert( DataModelSchema.table )
                        .columns(
                                DataModelSchema.id,
                                DataModelSchema.ordinalPosition,
                                DataModelSchema.columnName,
                                DataModelSchema.dataType,
                                DataModelSchema.length,
                                DataModelSchema.defaultValue,
                                DataModelSchema.description
                        ).values(
                                id,
                                dataStructure.getOrder(),
                                dataStructure.getName(),
                                dataStructure.getColType(),
                                dataStructure.getLength(),
                                dataStructure.getDefaultValue(),
                                dataStructure.getDescription()
                        ).generate().getStatement();
                sqlList.add( insertSql );
            }
//            if( data.getTagList(  ) != null && data.getTagList().size() > 0 ) {
//
//            }
        } );
        var result = dataLayerConnection.executeBatch( sqlList );
        log.info( "[ Data-Layer ] Data Model Insert Result : " + result.getDataList() );
    }

    public DataModelOuterClass.DataModel.Builder getDataModel( String id ) {
        var selectSql = select().from( DataModel.table )
                .where( Equal.of( DataModel.id, id ) )
                .generate().getStatement();
        var rowDataModel = dataLayerConnection.execute( selectSql ).getData().getTable();
        if( rowDataModel.getRowsCount() <= 0 ) {
            log.error( "Error. Not Found Data Model. Input ID[ {} ]", id );
            return null;
        }
        List<DataModelOuterClass.DataModel.Builder> dataModelBuilder = new ArrayList<>();
        // Set DataModel Info( name, desc, ... )
        parseAndInsertToDataModel( dataModelBuilder, rowDataModel );
        return dataModelBuilder.get( 0 );
    }

    public DataModelOuterClass.DataModel.Builder getDataModelPreview( String id ) {
        DataModelOuterClass.DataModel.Builder dataModelBuilder = getDataModel( id );
        if( dataModelBuilder == null ) return null;

        // Hard Coding : TODO : 삭제 필요
        dataModelBuilder.setPermission( DataModelOuterClass.Permission.newBuilder().setRead( true ).setWrite( true ).build() );

        // Set DataModel Metadata ( System / User )
        var selectMetadataSql = select().from( DataModelMetadata.table )
                .where( Equal.of( DataModelMetadata.id, id ) )
                .generate().getStatement();
        var rowDataModelMetadata = dataLayerConnection.execute( selectMetadataSql ).getData().getTable();
        if( rowDataModelMetadata.getRowsCount() > 0 ) {
            parseAndInsertToMetadata( dataModelBuilder, rowDataModelMetadata );
        }
        // Set DataModel Tag
        var selectTagSql = select().from( DataModelTag.table )
                .where( Equal.of( DataModelTag.id, id ) )
                .generate().getStatement();
        var rowDataModelTag = dataLayerConnection.execute( selectTagSql ).getData().getTable();
        if( rowDataModelTag.getRowsCount() > 0 ) {
            parseAndInsertToTag( dataModelBuilder, rowDataModelTag );
        }
        // Set DataModel Location
        var selectLocationSql = select().from( DataModelLocation.table )
                .where( Equal.of( DataModelLocation.id, id ) )
                .generate().getStatement();
        var rowDataModelLocation = dataLayerConnection.execute( selectLocationSql ).getData().getTable();
        if( rowDataModelLocation.getRowsCount() > 0 ) {
            parseAndInsertToLocation( dataModelBuilder, rowDataModelLocation );
        }
        // Set DataModel Data Structure
        var selectSchemaSql = select().from( DataModelSchema.table )
                .where( Equal.of( DataModelSchema.id, id ) )
                .generate().getStatement();
        var rowDataModelSchema = dataLayerConnection.execute( selectSchemaSql ).getData().getTable();
        if( rowDataModelSchema.getRowsCount() > 0 ) {
            parseAndInsertToSchema( dataModelBuilder, rowDataModelSchema );
        }
        return dataModelBuilder;
    }

    public DataModelOuterClass.DataModel.Builder getDataModelDefault( String id ) {
        DataModelOuterClass.DataModel.Builder dataModelBuilder = getDataModelPreview( id );
        if( dataModelBuilder == null ) return null;
        // Set Download Info
        dataModelBuilder.setDownloadInfo(
                DataModelOuterClass.DownloadInfo.newBuilder()
                        .setStatus( DataModelOuterClass.DownloadInfo.DownloadStatus.READY )
                        .setUrl( "http://data-fabric.com/download/data" )
                        .build() );

        // Set DataModel User Comment(Rating)
        var selectCommentSql = select().from( DataModelUserComment.table )
                .where( Equal.of( DataModelUserComment.dataModelId, id ) )
                .generate().getStatement();
        var rowDataModelComment = dataLayerConnection.execute( selectCommentSql ).getData().getTable();
        if( rowDataModelComment.getRowsCount() > 0 ) {
            parseAndInsertToComment( dataModelBuilder, rowDataModelComment );
        }
        // TODO : Set Statistics

        return dataModelBuilder;
    }

    private void parseAndInsertToDataModel(
            List<DataModelOuterClass.DataModel.Builder> dataModelbuilder, DataLayer.Table rowDataModel ) {
        for( int rowIdx = 0; rowIdx < rowDataModel.getRowsCount(); rowIdx++ ) {
            DataModelOuterClass.DataModel.Builder builder = DataModelOuterClass.DataModel.newBuilder();
            for( int colIdx = 0; colIdx < rowDataModel.getColumnsList().size(); colIdx++ ) {
                var column = rowDataModel.getColumnsList().get( colIdx );
                if( column.getColumnName().equalsIgnoreCase( DataModel.id.getName() ) ) {
                    builder.setId(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.name.getName() ) ) {
                    builder.setName(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.description.getName() ) ) {
                    builder.setDescription(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.type.getName() ) ) {
                    builder.setDataType(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.format.getName() ) ) {
                    builder.setDataFormat(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.status.getName() ) ) {
                    builder.setStatus(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.createdAt.getName() ) ) {
                    builder.setCreatedAt(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getTimeValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.createdBy.getName() ) ) {
                    builder.setCreator( UserOuterClass.User.newBuilder()
                            .setId( rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() )
                            .setName( "mobigen" )
                            .setNickname( "mobigen.platform" )
                            .build() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.lastModifiedAt.getName() ) ) {
                    builder.setLastModifiedAt(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getTimeValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModel.lastModifiedBy.getName() ) ) {
                    builder.setLastModifier( UserOuterClass.User.newBuilder().setId(
                            rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() ).build() );
                }
            }
            dataModelbuilder.add( builder );
        }
    }

    private void parseAndInsertToMetadata(
            DataModelOuterClass.DataModel.Builder modelBuilder, DataLayer.Table rowDataModel ) {

        for( int rowIdx = 0; rowIdx < rowDataModel.getRowsCount(); rowIdx++ ) {
            boolean isSystem = false;
            Utilities.Meta.Builder builder = Utilities.Meta.newBuilder();
            for( int colIdx = 0; colIdx < rowDataModel.getColumnsList().size(); colIdx++ ) {
                var column = rowDataModel.getColumnsList().get( colIdx );
                if( column.getColumnName().equalsIgnoreCase( DataModelMetadata.isSystem.getName() ) ) {
                    isSystem = rowDataModel.getRows( rowIdx ).getCell( colIdx ).getBoolValue();
                } else if( column.getColumnName().equalsIgnoreCase( DataModelMetadata.key.getName() ) ) {
                    builder.setKey( rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelMetadata.value.getName() ) ) {
                    builder.setValue( rowDataModel.getRows( rowIdx ).getCell( colIdx ).getStringValue() );
                }
            }
            if( isSystem ) {
                modelBuilder.addSystemMeta( builder.build() );
            } else {
                modelBuilder.addUserMeta( builder.build() );
            }
        }
    }

    private void parseAndInsertToTag(
            DataModelOuterClass.DataModel.Builder dataModelBuilder, DataLayer.Table rowDataModel ) {
        for( int rowIdx = 0; rowIdx < rowDataModel.getRowsCount(); rowIdx++ ) {
            for( int i = 0; i < rowDataModel.getColumnsList().size(); i++ ) {
                var column = rowDataModel.getColumnsList().get( i );
                if( column.getColumnName().equalsIgnoreCase( DataModelTag.tag.getName() ) ) {
                    String tag = rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue();
                    if( !tag.isEmpty() ) {
                        dataModelBuilder.addTag( tag );
                    }
                }
            }
        }
    }

    private void parseAndInsertToLocation(
            DataModelOuterClass.DataModel.Builder builder, DataLayer.Table rowDataModel ) {
        for( int rowIdx = 0; rowIdx < rowDataModel.getRowsCount(); rowIdx++ ) {
            DataModelOuterClass.DataLocation.Builder locationBuilder = DataModelOuterClass.DataLocation.newBuilder();
            for( int i = 0; i < rowDataModel.getColumnsList().size(); i++ ) {
                var column = rowDataModel.getColumnsList().get( i );
                if( column.getColumnName().equalsIgnoreCase( DataModelLocation.storageId.getName() ) ) {
                    locationBuilder.setStorageId( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelLocation.path.getName() ) ) {
                    locationBuilder.setDatabaseName( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelLocation.name.getName() ) ) {
                    locationBuilder.setTableName( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                }
            }
            builder.addDataLocation( locationBuilder.build() );
        }
    }

    private void parseAndInsertToSchema(
            DataModelOuterClass.DataModel.Builder builder, DataLayer.Table rowDataModel ) {
        for( int rowIdx = 0; rowIdx < rowDataModel.getRowsCount(); rowIdx++ ) {
            DataModelOuterClass.DataStructure.Builder structBuilder = DataModelOuterClass.DataStructure.newBuilder();
            for( int i = 0; i < rowDataModel.getColumnsList().size(); i++ ) {
                var column = rowDataModel.getColumnsList().get( i );
                if( column.getColumnName().equalsIgnoreCase( DataModelSchema.ordinalPosition.getName() ) ) {
                    structBuilder.setOrder( rowDataModel.getRows( rowIdx ).getCell( i ).getInt32Value() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelSchema.columnName.getName() ) ) {
                    structBuilder.setName( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelSchema.dataType.getName() ) ) {
                    structBuilder.setColType( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelSchema.length.getName() ) ) {
                    structBuilder.setLength( ( int )rowDataModel.getRows( rowIdx ).getCell( i ).getInt64Value() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelSchema.defaultValue.getName() ) ) {
                    structBuilder.setDefaultValue(
                            URLDecoder.decode( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue(), StandardCharsets.UTF_8 ) );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelSchema.description.getName() ) ) {
                    structBuilder.setDescription( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                }
            }
            builder.addDataStructure( structBuilder.build() );
        }
    }

    private void parseAndInsertToComment(
            DataModelOuterClass.DataModel.Builder modelBuilder, DataLayer.Table rowDataModel ) {
        DataModelOuterClass.DataModel.RatingAndComments.Builder ratingAndCommentsBuilder =
                DataModelOuterClass.DataModel.RatingAndComments.newBuilder();
        for( int rowIdx = 0; rowIdx < rowDataModel.getRowsCount(); rowIdx++ ) {
            DataModelOuterClass.RatingAndComment.Builder builder = DataModelOuterClass.RatingAndComment.newBuilder();
            for( int i = 0; i < rowDataModel.getColumnsList().size(); i++ ) {
                var column = rowDataModel.getColumnsList().get( i );
                if( column.getColumnName().equalsIgnoreCase( DataModelUserComment.id.getName() ) ) {
                    builder.setId( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelUserComment.userId.getName() ) ) {
                    builder.setUser(
                            UserOuterClass.User.newBuilder()
                                    .setId( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() )
                                    .build() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelUserComment.rating.getName() ) ) {
                    builder.setRating( rowDataModel.getRows( rowIdx ).getCell( i ).getInt32Value() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelUserComment.comment.getName() ) ) {
                    builder.setComment( rowDataModel.getRows( rowIdx ).getCell( i ).getStringValue() );
                } else if( column.getColumnName().equalsIgnoreCase( DataModelUserComment.time.getName() ) ) {
                    builder.setLastModifiedAt(
                            rowDataModel.getRows( rowIdx ).getCell( i ).getTimeValue() );
                }
            }
            ratingAndCommentsBuilder.addRatingAndComment( builder.build() );
        }
        // Calc Avg Rating
        for( DataModelOuterClass.RatingAndComment ratingAndComment :
                ratingAndCommentsBuilder.getRatingAndCommentList() ) {
            ratingAndCommentsBuilder.setAvgRating(
                    ratingAndCommentsBuilder.getAvgRating() + ratingAndComment.getRating() );
        }
        if( ratingAndCommentsBuilder.getRatingAndCommentCount() == 0 ) {
            ratingAndCommentsBuilder.setAvgRating( 0 );
        } else {
            ratingAndCommentsBuilder.setAvgRating(
                    ratingAndCommentsBuilder.getAvgRating() /
                            ratingAndCommentsBuilder.getRatingAndCommentCount() );
        }
        modelBuilder.setRatingAndComments( ratingAndCommentsBuilder.build() );
    }

    public boolean updateDataModelMetadata( DataModelOuterClass.ReqMetaUpdate request ) {
        DataModelOuterClass.DataModel.Builder dataModelBuilder = getDataModel( request.getId() );
        if( dataModelBuilder == null ) return false;

        List<String> sqlList = new ArrayList<>();
        for( Utilities.Meta meta : request.getUserMetaList() ) {
            var insertSql = String.format( "INSERT INTO data_metadata (%s, %s, %s, %s) VALUES('%s','false','%s','%s')" +
                            "ON CONFLICT ON CONSTRAINT data_metadata_un DO UPDATE SET %s='%s'",
                    DataModelMetadata.id.getName(),
                    DataModelMetadata.isSystem.getName(),
                    DataModelMetadata.key.getName(),
                    DataModelMetadata.value.getName(),
                    request.getId(),
                    meta.getKey(),
                    meta.getValue(),
                    DataModelMetadata.value.getName(),
                    meta.getValue() );
            sqlList.add( insertSql );
        }
        var result = dataLayerConnection.executeBatch( sqlList );
        log.info( "[ Data-Model ] User Metadata Update Result : " + result.getDataList() );
        return true;
    }

}
