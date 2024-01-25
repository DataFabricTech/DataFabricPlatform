
package com.mobigen.datafabric.libs.rabbitmq;

import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Testcontainers
class TestContainerTest {

    static final int RABBIT_MQ_PORT = 5672;
    static GenericContainer rabbitMQContainer = new GenericContainer(
            DockerImageName.parse( "rabbitmq:3-alpine" ) ).withExposedPorts( RABBIT_MQ_PORT );
    static RabbitMQContainerInfo rabbitMQContainerInfo;

    @BeforeAll
    static void init() {
        try {
            rabbitMQContainer.start();
            System.out.println( "RabbitMQ Container Started" );
        } catch( Exception e) {
            throw new RuntimeException( "RabbitMQ Container Start Failed" );
        }
    }

    @AfterAll
    static void shutdown() {
        rabbitMQContainer.stop();
    }


    @BeforeEach
    void setUp() {
        rabbitMQContainerInfo = RabbitMQContainerInfo.builder()
                .host( rabbitMQContainer.getHost() )
                .port( rabbitMQContainer.getMappedPort( RABBIT_MQ_PORT ) )
                .build();
    }

    @Getter
    @Builder
    static class RabbitMQContainerInfo {
        String host;
        Integer port;
    }

    @Test
    void TestContainerConnectDisconnect() {
        final Integer queueId = 1;
        final String queueName = "work-queue-test";
        Client client = Client.getInstance();
        WorkerImpl worker = new WorkerImpl();
        Configuration conf = Configuration.builder()
                .host( rabbitMQContainerInfo.getHost() )
                .port( rabbitMQContainerInfo.getPort() )
                .threadPoolSize( 5 )
                .queueConfigs( new ArrayList<>(){{
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( true )
                            .id( queueId )
                            .exchangeType( ExchangeType.WORK_QUEUE )
                            .queueName( queueName)
                            .numChannel( 1 )
                            .build());
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( false )
                            .exchangeType( ExchangeType.WORK_QUEUE )
                            .queueName( queueName )
                            .prefetchSize( 10 )
                            .numChannel( 2 )
                            .worker( worker )
                            .build());
                }} )
                .build();
        try {
            client.initialize( conf );
            client.shutdown();
        } catch( IOException | TimeoutException e ) {
            throw new RuntimeException( "Client Initialize : ", e );
        }
    }

    static class WorkerImpl implements Worker {
        AtomicInteger receiveCount;

        public WorkerImpl( ) {
            this.receiveCount = new AtomicInteger(0);
        }

        @Override
        public boolean doWork( String exchange, String routingKey, byte[] body ) {
            var msg = new String(body);
            System.out.printf( "Thread[ %s ] E[ %s ] R[ %s ] Msg[ %s ] Count[ %d ]\n",
                    Thread.currentThread().getName(), exchange, routingKey, msg, receiveCount.getAndIncrement() );
            // 메시지에 'true' 가 포함되었는가 아닌가를 이용해 반환 처리 : true 를 반환하면 consumer ack 를 정상으로 전송
            return msg.contains( "true" );
        }

        public int getReceiveCount() {
            return receiveCount.get();
        }
    }

}
