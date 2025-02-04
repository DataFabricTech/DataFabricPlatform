package com.mobigen.datafabric.relationship.clients;

import ch.qos.logback.core.joran.conditional.ElseAction;
import com.mobigen.datafabric.relationship.configurations.StorageConfiguration;
import com.mobigen.datafabric.relationship.models.DataRelationshipFileType;
import com.mobigen.datafabric.relationship.models.exception.DataRelationshipException;
import com.mobigen.datafabric.relationship.utils.PathUtil;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "StorageClient")
public class StorageClient {
    private final MinioClient minioClient;
    private final StorageConfiguration config;

    public StorageClient(StorageConfiguration config) {
        try {
            this.config = config;
            this.minioClient = MinioClient.builder()
                    .endpoint(config.getHost(), config.getPort(), false)
                    .credentials(config.getUsername(), config.getPassword())
                    .region("ap-northeast-2")
                    .build();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(config.getBucket()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(config.getBucket()).build());
            } else {
                log.info("Bucket already exists");
            }
        } catch (IOException e) {
            log.error("IO Exception : ", e);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("No Such Algorithm : ", e);
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error("Invalid Key: ", e);
            throw new RuntimeException(e);
        } catch (ServerException e) {
            log.error("Server Exception : ", e);
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            log.error("Insufficient Data : ", e);
            throw new RuntimeException(e);
        } catch (ErrorResponseException e) {
            log.error("Error Response Exception : ", e);
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            log.error("Invalid Response Exception : ", e);
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            log.error("Xml Parser Exception : ", e);
            throw new RuntimeException(e);
        } catch (InternalException e) {
            log.error("Internal Exception : ", e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, List<String>> uploadObject(Map<String, List<String>> metaPath, String remotePrefix) {
        String remotePrefixPath;
        if( !this.config.getPrefix().isBlank() ) {
            remotePrefixPath = PathUtil.combinePath(this.config.getPrefix(), remotePrefix);
        } else {
            remotePrefixPath = remotePrefix;
        }
        Map<String, List<String>> uploadedPaths = new HashMap<>();
        metaPath.forEach((key, value) -> {
            List<String> uploadList = new ArrayList<>();
            for(String path : value) {
                log.debug("Type : {}, File : {}", key, path);
                String fileName = PathUtil.getFileName(path);
                String objectName;
                if( key.equals(DataRelationshipFileType.METADATA)) {
                    objectName = String.format("%s/meta/%s", remotePrefixPath, fileName);
                } else {
                    objectName = String.format("%s/%s", remotePrefixPath, fileName);
                }
                try {
                    _upload(path, objectName);
                } catch (DataRelationshipException e) {
                    log.error("Error while uploading object : {} -> {}", path, objectName, e);
                    continue;
                }
                log.debug("Uploaded {} to {}", path, objectName);
                uploadList.add(objectName);
            }
            uploadedPaths.put(key, uploadList);
        });
        return uploadedPaths;
    }

    public void _upload(String local, String remote) throws DataRelationshipException {
        try {
            ObjectWriteResponse response = this.minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(this.config.getBucket())
                            .object(remote)
                            .filename(local)
                            .build());
            log.info(response.toString());
        } catch (ErrorResponseException e) {
            throw new DataRelationshipException("Error Response", e);
        } catch (InsufficientDataException e) {
            throw new DataRelationshipException("Insufficient Data", e);
        } catch (InternalException e) {
            throw new DataRelationshipException("Internal", e);
        } catch (InvalidKeyException e) {
            throw new DataRelationshipException("Invalid Key", e);
        } catch (InvalidResponseException e) {
            throw new DataRelationshipException("Invalid Response", e);
        } catch (IOException e) {
            throw new DataRelationshipException("IOException", e);
        } catch (NoSuchAlgorithmException e) {
            throw new DataRelationshipException("No Such Algorithm", e);
        } catch (ServerException e) {
            throw new DataRelationshipException("Server Exception", e);
        } catch (XmlParserException e) {
            throw new DataRelationshipException("Xml Parser Exception", e);
        }
    }

    public void close() {
        try {
            this.minioClient.close();
        } catch (Exception e) {
            log.error("Error while closing MinioClient", e);
        }
    }
}
