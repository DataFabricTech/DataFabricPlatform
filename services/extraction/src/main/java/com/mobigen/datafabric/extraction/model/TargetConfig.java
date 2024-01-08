package com.mobigen.datafabric.extraction.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TargetConfig {
    // storageType 은, connectConfig 를 set 혹은 get 할 때 어떤 storage 를 선택할 지 정하기 위한 값
    public static StorageType storageType;
    private ConnectInfo connectInfo;
    public static DataFormat dataFormat;
    // url 혹은 filepath
    private String target;

    @Setter
    @Getter
    public static class ConnectInfo {
        private MinIO minioConnectInfo;
        private HDFS hdfsConnectInfo;
        private RDBMS rdbmsConnectInfo;
    }

    @Setter
    @Getter
    public static class MinIO {
        private String host;
        private String port;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String objectKey;
        private String region = "us-east-1";
    }

    @Setter
    @Getter
    public static class HDFS{
        private String host;
        private String restPort;
        private String port;
        private String group = "supergroup";
        private String user;
        private String pw;
        private String filePath;
    }

    @Setter
    @Getter
    public static class RDBMS {
        private String host;
        private String jdbc_driver;
        private String url;
        private String username;
        private String password;
    }
}