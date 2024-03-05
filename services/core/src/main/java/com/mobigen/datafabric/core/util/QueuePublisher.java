package com.mobigen.datafabric.core.util;

import com.google.gson.JsonObject;
import com.mobigen.datafabric.libs.rabbitmq.Client;
import com.mobigen.datafabric.libs.rabbitmq.Configuration;
import com.mobigen.datafabric.libs.rabbitmq.ExchangeType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@Getter
public class QueuePublisher {
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private int port;
    @Value("${spring.rabbitmq.queuename}")
    private String queueName;
    @Value("${spring.rabbitmq.queueId}")
    private int queueId;

    public Client getClient() {
        var client = Client.getInstance();
        System.out.println(this.host);
        System.out.println(this.port);
        var conf = Configuration.builder()
                .host(host)
                .port(port)
                .queueConfigs(new ArrayList<>() {{
                    add(Configuration.QueueConfig.builder()
                            .isPublisher(true)
                            .id(queueId)
                            .exchangeType(ExchangeType.WORK_QUEUE)
                            .queueName(queueName)
                            .numChannel(1)
                            .build());
                }})
                .build();
        try {
            client.initialize( conf );
            return client;
        } catch(IOException | TimeoutException e ) {
            e.printStackTrace(); // todo remove
            throw new RuntimeException( "Client Initialize : ", e );
        }
    }

    public String makeBody(String method, String id, String receiverName) {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("receiver_name", receiverName);
        var bodyObject = new JsonObject();
        bodyObject.addProperty("method", method);
        bodyObject.addProperty("id", id);
        jsonObject.add("body", bodyObject);
        jsonObject.addProperty("publisher_name", "core");
        jsonObject.addProperty("publish_time", LocalDateTime.now().toString());
        return jsonObject.toString();
    }
}
