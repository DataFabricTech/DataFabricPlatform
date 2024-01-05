
package com.mobigen.datafabric.libs.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Configuration {
    // 연결 정보
    String host;
    // DEFAULT_AMQP_PORT = 5672;
    // DEFAULT_AMQP_OVER_SSL_PORT = 5671;
    // Integer port;

    // 연결 관리 옵션
    // DEFAULT_HEARTBEAT = 60;
    // Integer heartbeat;
    // DEFAULT_CONNECTION_TIMEOUT = 60000;
    // Integer connectionTimeout;
    // DEFAULT_HANDSHAKE_TIMEOUT = 10000;
    // Integer handshakeTimeout;
    // DEFAULT_SHUTDOWN_TIMEOUT = 10000;
    // Integer shutdownTimeout;

    // DEFAULT_CHANNEL_MAX = 2047;
    // Integer channelMax;

    List<QueueConfig> queueConfigs;

    @Getter
    @Builder
    static class QueueConfig {
        // 메시지 큐 사용 프로세서의 역할
        Boolean isPublisher;
        // Channel 과 Queue 의 설정
        String exchangeName;
        BuiltinExchangeType exchangeType;
        List<String> routingKeys;
        // Boolean durable = false;
        // Boolean autoDelete = false;

        // Consumer Option
        // Boolean requeue = false;
        Boolean isMultiThread = false;
        Integer threadCount;
    }
}