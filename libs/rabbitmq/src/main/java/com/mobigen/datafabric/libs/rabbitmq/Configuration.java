
package com.mobigen.datafabric.libs.rabbitmq;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Configuration {
    // 연결 정보
    String host;
    Integer port;       // DEFAULT_AMQP_PORT = 5672, DEFAULT_AMQP_OVER_SSL_PORT = 5671;

    // 연결 관리 옵션
    // Integer heartbeat;               // DEFAULT_HEARTBEAT = 60;
    // Integer connectionTimeout;       // DEFAULT_CONNECTION_TIMEOUT = 60000;
    // Integer handshakeTimeout;        // DEFAULT_HANDSHAKE_TIMEOUT = 10000;
    // Integer shutdownTimeout;         // DEFAULT_SHUTDOWN_TIMEOUT = 10000;

    // ThreadPool Size
    Integer threadPoolSize;             // DEFAULT_THREAD_POOL_SIZE = 10;

    List<QueueConfig> queueConfigs;
    @Getter
    @Builder
    static class QueueConfig {
        Boolean isPublisher;            // 이 channel 의 역할
        Integer id;                     // Producer 는 필요에 따라 Channel 직접 사용(getChannel)을 위해 ID 설정
        // Channel 과 Queue 의 설정
        ExchangeType exchangeType;      // 메시지 큐 동작 방식 : direct, topic, fanout(pub/sub), work-queue
        String exchangeName;            // Exchange Type 이 direct, topic, fanout 인 경우 Exchange 이름 설정
        String queueName;               // Exchange Type 이 Work Queue 의 경우 메시지 큐 이름 설정
        List<String> routingKeys;       // Exchange Type 이 topic, direct 라면 필요에 따라 다중 Routing 설정
        // Channel, Queue 특성
        // Boolean durable = false;             // 서버 재시작 시에도 메시지 큐가 유지되어야 하는지 여부
        // Boolean autoDelete = false;          // 메시지 큐가 사용되지 않을 때 삭제할 것인지 여부
        // 전송 옵션 : 아직 구현되지 못함.
        // Boolean isNonBlock;

        // For Consumer
        Integer numChannel;             // 하나의 큐에 연결 할 Channel 수 - 전체 Thread 수를 고려
        Integer prefetchSize;           // 큐 내에 소비자가 확인(처리)하지 못한 메시지의 보관 수
        // Boolean autoAck = true;      // 메시지 큐에서 메시지를 받은 후 자동으로 확인할 것인지 여부
        Worker worker;                  // 메시지 수신 시 메시지를 받아 처리할 콜백
    }
}