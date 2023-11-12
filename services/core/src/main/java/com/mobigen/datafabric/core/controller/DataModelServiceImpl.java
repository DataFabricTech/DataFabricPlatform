package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.services.storage.DataModelService;
import com.mobigen.datafabric.core.services.storage.DataStorageService;
import com.mobigen.datafabric.share.protobuf.DataModelOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.grpc.DataModelServiceCallBack;

import java.util.List;

/**
 * gRPC 의 request 를 받아 response 를 생성하는 콜백 클래스의 구현부
 * Data Model 관련 서비스 제공
 * <p>
 * Created by jblim.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class DataModelServiceImpl implements DataModelServiceCallBack {
//    DataStorageService dataStorageService = new DataStorageService();
    DataModelService dataModelService = new DataModelService();
    @Override
    public DataModelOuterClass.DataModelPreview preview( Utilities.ReqId request ) {
        DataModelOuterClass.DataModel.Builder result = dataModelService.getDataModelPreview( request.getId() );
        if( result != null ) {
            return DataModelOuterClass.DataModelPreview.newBuilder()
                    .setCode( "200" )
                    .setData( DataModelOuterClass.DataModelPreview.Data.newBuilder()
                            .setDataPreview( result.build() ).build())
                    .build();
        } else {
            return DataModelOuterClass.DataModelPreview.newBuilder()
                    .setCode( "500" )
                    .setErrMsg( "Error. In Data Model Preview(Not Found)" ).build();
        }
    }

    @Override
    public DataModelOuterClass.DataModelDefault defaultInfo( Utilities.ReqId request ) {
        DataModelOuterClass.DataModel.Builder result = dataModelService.getDataModelDefault( request.getId() );
        if( result != null ) {
            return DataModelOuterClass.DataModelDefault.newBuilder()
                    .setCode( "200" )
                    .setData( DataModelOuterClass.DataModelDefault.Data.newBuilder()
                            .setDataModel( result.build() ).build())
                    .build();
        } else {
            return DataModelOuterClass.DataModelDefault.newBuilder()
                    .setCode( "500" )
                    .setErrMsg( "Error. In Data Model Default Info(Not Found)" )
                    .build();
        }
    }

    @Override
    public Utilities.CommonResponse updateMetadata( DataModelOuterClass.ReqMetaUpdate request ) {
        if( dataModelService.updateDataModelMetadata( request ) ) {
            return Utilities.CommonResponse.newBuilder()
                    .setCode( "200" ).build();
        }
        return Utilities.CommonResponse.newBuilder()
                .setCode( "500" )
                .setErrMsg( "Error. In Update Metadata(Not Found Data Model)" )
                .build();
    }

    @Override
    public Utilities.CommonResponse updateTag( DataModelOuterClass.ReqTagUpdate request ) {
        if( dataModelService.updateDataModelTag( request ) ) {
            return Utilities.CommonResponse.newBuilder()
                    .setCode( "200" ).build();
        }
        return Utilities.CommonResponse.newBuilder()
                .setCode( "500" )
                .setErrMsg( "Error. In Update Tag(Not Found Data Model)" )
                .build();
    }

    @Override
    public Utilities.CommonResponse downloadRequest( Utilities.ReqId request ) {
        return null;
    }

    @Override
    public Utilities.CommonResponse addComment( DataModelOuterClass.ReqRatingAndComment request ) {
        if( dataModelService.addComment( request ) ) {
            return Utilities.CommonResponse.newBuilder()
                    .setCode( "200" ).build();
        }
        return Utilities.CommonResponse.newBuilder()
                .setCode( "500" )
                .setErrMsg( "Error. In Add Comment(Not Found Data Model)" )
                .build();
    }

    @Override
    public Utilities.CommonResponse updateComment( DataModelOuterClass.ReqRatingAndComment request ) {
        if( dataModelService.updateComment( request ) ) {
            return Utilities.CommonResponse.newBuilder()
                    .setCode( "200" ).build();
        }
        return Utilities.CommonResponse.newBuilder()
                .setCode( "500" )
                .setErrMsg( "Error. In Update Comment(Not Found Data Model/Not Found Comment)" )
                .build();
    }

    @Override
    public Utilities.CommonResponse deleteComment( DataModelOuterClass.ReqRatingAndComment request ) {
        if( dataModelService.deleteComment( request ) ) {
            return Utilities.CommonResponse.newBuilder()
                    .setCode( "200" ).build();
        }
        return Utilities.CommonResponse.newBuilder()
                .setCode( "500" )
                .setErrMsg( "Error. In Delete Comment(Not Found Data Model/Not Found Comment)" )
                .build();
    }

    @Override
    public DataModelOuterClass.ResDataModels allDataSummary( DataModelOuterClass.DataModelSearch request ) {
        DataModelOuterClass.ResDataModels.Data result = dataModelService.getDataModelSummary( request );
        if( result != null ) {
            return DataModelOuterClass.ResDataModels.newBuilder()
                    .setCode( "200" )
                    .setData( result )
                    .build();
        } else {
            return DataModelOuterClass.ResDataModels.newBuilder()
                    .setCode( "500" )
                    .setErrMsg( "Error. In All Data Summary" ).build();
        }
    }

    @Override
    public DataModelOuterClass.ResDataModels allData( DataModelOuterClass.DataModelSearch request ) {
        return null;
    }
//    AdaptorService service = new AdaptorService();

}

