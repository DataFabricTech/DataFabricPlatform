package com.mobigen.datafabric.relationship.clients;

import com.mobigen.datafabric.relationship.configurations.DataRelationshipServer;
import com.mobigen.datafabric.relationship.models.DataRelationshipExcuteRequest;
import com.mobigen.datafabric.relationship.models.DataRelationshipFileType;
import com.mobigen.datafabric.relationship.models.DataRelationshipResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
public class DataRelationshipClient {
    private final DataRelationshipServer config;
    private static final String RELATIONSHIP_ANALYSIS_EXECUTE = "/relationship-analysis/execute";
    public DataRelationshipClient(DataRelationshipServer config) {
        this.config = config;
    }

    public void dataRelationshipExecute(Map<String, List<String>> paths) {
        log.info("Data Relationship Analysis Execute");
        String interaction = paths.get(DataRelationshipFileType.INTERACTION).get(0);
        String integration = paths.get(DataRelationshipFileType.FUSION).get(0);
        String meta = paths.get(DataRelationshipFileType.METADATA).get(0);
        DataRelationshipExcuteRequest request = DataRelationshipExcuteRequest.builder()
                .integrationHistory(integration)
                .interaction_data(interaction)
                .metadata_path(meta)
                .build();
        execute(request);
    }

    public void execute(DataRelationshipExcuteRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://" + config.getHost() + ":" + config.getPort() + RELATIONSHIP_ANALYSIS_EXECUTE;
        log.info("Data Relationship Analysis URL: {}", url);
        DataRelationshipResponse response = restTemplate.postForObject(url, request, DataRelationshipResponse.class);
    }
}
