
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
class RunTest {

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
    void PubSub() {
        final Integer queueId = 1;
        final String exchangeName = "pubsub-test";
        Client client = Client.getInstance();
        WorkerImpl worker01 = new WorkerImpl();
        WorkerImpl worker02 = new WorkerImpl();
        WorkerImpl worker03 = new WorkerImpl();
        Configuration conf = Configuration.builder()
                .host( rabbitMQContainerInfo.getHost() )
                .port( rabbitMQContainerInfo.getPort() )
                .threadPoolSize( 5 )
                .queueConfigs( new ArrayList<>(){{
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( true )
                            .id( queueId )
                            .exchangeType( ExchangeType.FANOUT )
                            .exchangeName( exchangeName)
                            .build());
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( false )
                            .exchangeType( ExchangeType.FANOUT )
                            .exchangeName( exchangeName)
                            .prefetchSize( 10 )
                            .numChannel( 1 )
                            .worker( worker01 )
                            .build());
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( false )
                            .exchangeType( ExchangeType.FANOUT )
                            .exchangeName( exchangeName)
                            .prefetchSize( 10 )
                            .numChannel( 1 )
                            .worker( worker02 )
                            .build());
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( false )
                            .exchangeType( ExchangeType.FANOUT )
                            .exchangeName( exchangeName)
                            .prefetchSize( 10 )
                            .numChannel( 1 )
                            .worker( worker03 )
                            .build());
                }} )
                .build();
        try {
            client.initialize( conf );
            var sendCount = 100;
            for( int i = 0; i < sendCount; i++) {
                try {
                    // Pub/Sub 의 경우 routingKey 는 ""
                    client.publish( queueId, "", "hello world : ack true" );
                } catch( InterruptedException | TimeoutException e ) {
                    System.out.printf("No.Msg[ %03d ] Publish Error : " + e.getMessage(), i );
                    Assertions.fail("Pub/Sub Publish Error");
                }
            }
            // sleep 이 없을 경우 consumer 의 receive count 가 빠짐.
            Thread.sleep( 500 );
            System.out.println( "receiveCount: " + worker01.getReceiveCount() );
            System.out.println( "receiveCount: " + worker02.getReceiveCount() );
            System.out.println( "receiveCount: " + worker03.getReceiveCount() );
            Assertions.assertEquals( sendCount, worker01.getReceiveCount(), "Not Equals Send: " + sendCount + " And Receive: " + worker01.getReceiveCount() );
            Assertions.assertEquals( sendCount, worker02.getReceiveCount(), "Not Equals Send: " + sendCount + " And Receive: " + worker02.getReceiveCount() );
            Assertions.assertEquals( sendCount, worker03.getReceiveCount(), "Not Equals Send: " + sendCount + " And Receive: " + worker03.getReceiveCount() );

            client.shutdown();
        } catch( IOException | TimeoutException e ) {
            throw new RuntimeException( "Client Initialize : ", e );
        } catch( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }

    @Test
    void Routing() {
        final Integer queueId = 2;
        final String exchangeName = "routing";
        final String routing01 = "routing01";
        final String routing0201 = "routing02-01";
        final String routing0202 = "routing02-02";
        Client client = Client.getInstance();
        WorkerImpl worker01 = new WorkerImpl();
        WorkerImpl worker02 = new WorkerImpl();
        Configuration conf = Configuration.builder()
                .host( rabbitMQContainerInfo.getHost() )
                .port( rabbitMQContainerInfo.getPort() )
                .threadPoolSize( 5 )
                .queueConfigs( new ArrayList<>(){{
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( true )
                            .id( queueId )
                            .exchangeType( ExchangeType.DIRECT )
                            .exchangeName( exchangeName)
                            .build());
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( false )
                            .exchangeType( ExchangeType.DIRECT )
                            .exchangeName( exchangeName)
                            .routingKeys( new ArrayList<>(){{
                                add( routing01 );
                            }} )
                            .prefetchSize( 5 )
                            .numChannel( 1 )
                            .worker( worker01 )
                            .build());
                    add( Configuration.QueueConfig.builder()
                            .isPublisher( false )
                            .exchangeType( ExchangeType.DIRECT )
                            .exchangeName( exchangeName )
                            .routingKeys( new ArrayList<>(){{
                                add( routing0201 );
                                add( routing0202 );
                            }} )
                            .prefetchSize( 5 )
                            .numChannel( 1 )
                            .worker( worker02 )
                            .build());
                }} )
                .build();
        try {
            client.initialize( conf );
            var sendCount = 100;
            for( int i = 0; i < sendCount; i++) {
                try {
                    // Routing 을 위한 Routing 키 설정
                    var routingIdx = i % 3;
                    switch( routingIdx ) {
                        case 0: client.publish( queueId, routing01, "hello world : ack true" ); break;
                        case 1: client.publish( queueId, routing0201, "hello world : ack true" ); break;
                        case 2: client.publish( queueId, routing0202, "hello world : ack true" ); break;
                        default:
                            System.out.println( "Routing Idx Overflow Error" );
                            Assertions.fail( "Routing Idx Overflow Error" );
                    }
                } catch( InterruptedException | TimeoutException e ) {
                    System.out.printf("No.Msg[ %03d ] Publish Error : " + e.getMessage(), i );
                    Assertions.fail("Routing Publish Error");
                }
            }
            // sleep 이 없을 경우 consumer 의 receive count 가 빠짐.
            Thread.sleep( 500 );
            System.out.println( "receiveCount: " + worker01.getReceiveCount() );
            System.out.println( "receiveCount: " + worker02.getReceiveCount() );
            Assertions.assertEquals( (sendCount / 3) + 1, worker01.getReceiveCount(),
                    "Not Equals Send: " + ((sendCount / 3)+1) + " And Receive: " + worker01.getReceiveCount() );
            Assertions.assertEquals( (sendCount / 3) * 2, worker02.getReceiveCount(),
                    "Not Equals Send: " + ((sendCount/3)*2) + " And Receive: " + worker02.getReceiveCount() );

            client.shutdown();
        } catch( IOException | TimeoutException e ) {
            throw new RuntimeException( "Client Initialize : ", e );
        } catch( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }

    @Test
    void WorkQueue() {
        final Integer queueId = 3;
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
            var sendCount = 100;
            for( int i = 0; i < sendCount; i++) {
                try {
                    client.publish( queueId, "", "hello world : ack true" );
                } catch( InterruptedException | TimeoutException e ) {
                    System.out.printf("No.Msg[ %03d ] Publish Error : " + e.getMessage(), i );
                    Assertions.fail("Work-Queue Publish Error");
                }
            }
            // sleep 이 없을 경우 consumer 의 receive count 가 빠짐.
            Thread.sleep( 500 );
            Assertions.assertEquals( sendCount, worker.getReceiveCount(), "Not Equals Send: " + sendCount + " And Receive: " + worker.getReceiveCount() );

            client.shutdown();
        } catch( IOException | TimeoutException e ) {
            throw new RuntimeException( "Client Initialize : ", e );
        } catch( InterruptedException e ) {
            throw new RuntimeException( e );
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
