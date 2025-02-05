package com.mobigen.datafabric.relationship.controllers;

import com.mobigen.datafabric.relationship.clients.DataRelationshipClient;
import com.mobigen.datafabric.relationship.clients.StorageClient;
import com.mobigen.datafabric.relationship.configurations.Configurations;
import com.mobigen.datafabric.relationship.models.CommonResponse;
import com.mobigen.datafabric.relationship.models.DataRelationshipFileType;
import com.mobigen.datafabric.relationship.services.DataCollector;
import com.mobigen.datafabric.relationship.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class CollectController {

    final DataCollector collectorService;
    final Configurations configurations;

    public CollectController(DataCollector collectorService, Configurations configurations) {
        this.collectorService = collectorService;
        this.configurations = configurations;
    }

    @GetMapping("/interaction")
    public Object getInteraction() {
        String interactionFilePath = collectorService.collectUserData(DateUtil.getStrCurrentDateTime("yyMMdd-HHmm"));
        log.info("interactionFilePath : {}", interactionFilePath);
        return CommonResponse.builder().code(200).message(interactionFilePath).build();
    }

    @GetMapping("/fusion")
    public Object getFusion() {
        String fusionFilePath = collectorService.collectFusionData(DateUtil.getStrCurrentDateTime("yyMMdd-HHmm"));
        return CommonResponse.builder().code(200).message(fusionFilePath).build();
    }

    @GetMapping("/meta-data")
    public Object getMetadata() throws IOException {
        List<String> files = collectorService.collectMetaData(DateUtil.getStrCurrentDateTime("yyMMdd-HHmm"));
        return CommonResponse.builder().code(200).message(files.toString()).build();
    }

    @PostMapping("/data-relationship/request")
    public Object requestRelationship() {
        String strDate = DateUtil.getStrCurrentDateTime("yyMMdd-HHmm");
        String interactionFilePath = collectorService.collectUserData(strDate);
        String fusionFilePath = collectorService.collectFusionData(strDate);
        List<String> metaPaths = collectorService.collectMetaData(strDate);

        Map<String, List<String>> paths = new HashMap<>();
        paths.put(DataRelationshipFileType.INTERACTION, List.of(interactionFilePath));
        paths.put(DataRelationshipFileType.FUSION, List.of(fusionFilePath));
        paths.put(DataRelationshipFileType.METADATA, metaPaths);

        // Copy To MinIO
        StorageClient storageClient = new StorageClient(configurations.getStorage());
        Map<String, List<String>> uploadPaths = storageClient.uploadObject(paths, strDate);
        storageClient.close();

        // Trigger Data Relationship
        DataRelationshipClient dataRelationshipClient = new DataRelationshipClient(configurations.getDataRelationship());

        return CommonResponse.builder().code(200).message("Success").build();
    }
}
