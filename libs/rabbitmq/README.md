# RabbitMQ Library

## 1. 소개
RabbitMQ 사용을 위한 라이브러리입니다.
동기화 메시지 전송과 하나의 큐를 대상으로 다중 채널을 이용한 메시지 수신을 돕기위해 작성되었습니다. 

## 2. 버전 정보 - 변경/개선 정보
1. Version : 1.0.0
    * 주요 기능
      * 동기화 방식의 전송
      * 다중 채널을 이용한 메시지 수신

## 3. 사용법  
### 3.1. 라이브러리 추가
```kotlin
dependencies {
    implementation("com.mobigen.datafabric.libs:rabbitmq")
}
```

### 3.2. Exchange Type 별 사용 예제 
#### 3.2.1. RabbitMQ Lib 의 환경 설정 필드 별 설명   
```java
public class Configuration {
    // 연결 정보
    String host;
    Integer port;       // DEFAULT_AMQP_PORT = 5672, DEFAULT_AMQP_OVER_SSL_PORT = 5671;
    
    Integer threadPoolSize;             // Consumer 스레드의 수 
    // 연결 관리 옵션
    // Integer heartbeat;               // DEFAULT_HEARTBEAT = 60;
    // Integer connectionTimeout;       // DEFAULT_CONNECTION_TIMEOUT = 60000;
    // Integer handshakeTimeout;        // DEFAULT_HANDSHAKE_TIMEOUT = 10000;
    // Integer shutdownTimeout;         // DEFAULT_SHUTDOWN_TIMEOUT = 10000;

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

```

### 3.2.2. RabbitMQClient 를 사용 예제
1. WorkQueue 예제   
    * Producer  
      Configuration, Start  
      ```java
      final Integer queueId = 1;
      final String queueName = "work-queue-test";
      RabbitMQClient client = RabbitMQClient.getInstance();
      Configuration conf = Configuration.builder()
              .host( rabbitMQContainerInfo.getHost() )
              .port( rabbitMQContainerInfo.getPort() )
              .queueConfigs( new ArrayList<>(){{
                  add( Configuration.QueueConfig.builder()
                          .isPublisher( true )
                          .id( queueId )
                          .exchangeType( ExchangeType.WORK_QUEUE )
                          .queueName( queueName)
                          .numChannel( 1 )
                          .build());
              }} )
              .build();
      ```
      Send Message  
      ```java
      client.publish( queueId, "", "hello world : ack true" );
      ```
    * Consumer  
      Worker 구현 
      ```java
      public class WorkerImpl implements Worker {
          @Override
          public boolean work( String message ) {
              System.out.println( message );
              return true;
          }
      }
      ```
      Configuration, Start
      ```java
      WorkerImpl worker = new WorkerImpl();
      Configuration conf = Configuration.builder()
              .host( rabbitMQContainerInfo.getHost() )
              .port( rabbitMQContainerInfo.getPort() )
              .threadPoolSize( 5 )
              .queueConfigs( new ArrayList<>(){{
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
      ```

2. Publish/Subscribe 예제  
    * Producer  
      Configuration, Start  
      ```java
      final Integer queueId = 1;
      final String exchangeName = "pubsub-test";
      RabbitMQClient client = RabbitMQClient.getInstance();
      Configuration conf = Configuration.builder()
              .host( rabbitMQContainerInfo.getHost() )
              .port( rabbitMQContainerInfo.getPort() )
              .queueConfigs( new ArrayList<>(){{
                  add( Configuration.QueueConfig.builder()
                          .isPublisher( true )
                          .id( queueId )
                          .exchangeType( ExchangeType.FANOUT )
                          .exchangeName( exchangeName)
                          .build());
              }} )
              .build();
      ```
      Send Message    
      ```java
      client.publish( queueId, "", "hello world : ack true" );
      ```
    * Consumer  
      Worker 구현
      ```java
      public class WorkerImpl implements Worker {
          @Override
          public boolean work( String message ) {
              System.out.println( message );
              return true;
          }
      }
      ```
      Configuration, Start  
      ```java
      RabbitMQClient client = RabbitMQClient.getInstance();
      WorkerImpl worker01 = new WorkerImpl();
      WorkerImpl worker02 = new WorkerImpl();
      Configuration conf = Configuration.builder()
              .host( rabbitMQContainerInfo.getHost() )
              .port( rabbitMQContainerInfo.getPort() )
              .threadPoolSize( 5 )
              .queueConfigs( new ArrayList<>(){{
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
              }} )
              .build();
      ```
 
3. Routing 예제
   * Producer
     ```java
     final Integer queueId = 1;
     final String exchangeName = "routing";
     final String routing01 = "routing01";
     final String routing0201 = "routing02-01";
     final String routing0202 = "routing02-02";
     RabbitMQClient client = RabbitMQClient.getInstance();
     Configuration conf = Configuration.builder()
             .host( rabbitMQContainerInfo.getHost() )
             .port( rabbitMQContainerInfo.getPort() )
             .queueConfigs( new ArrayList<>(){{
                 add( Configuration.QueueConfig.builder()
                         .isPublisher( true )
                         .id( queueId )
                         .exchangeType( ExchangeType.DIRECT )
                         .exchangeName( exchangeName)
                         .build());
             }} )
             .build();
     ```
     Send Message  
     ```java
     client.publish( queueId, routing01, "hello world : ack true" );
     client.publish( queueId, routing0201, "hello world : ack true" );
     client.publish( queueId, routing0202, "hello world : ack true" );
     ```
   * Consumer  
     Worker 구현  
     ```java
     public class WorkerImpl implements Worker {
         @Override
         public boolean work( String message ) {
             System.out.println( message );
             return true;
         }
     }
     ```
     Configuration, Start  
     ```java
     final String exchangeName = "routing";
     final String routing01 = "routing01";
     final String routing0201 = "routing02-01";
     final String routing0202 = "routing02-02";
     RabbitMQClient client = RabbitMQClient.getInstance();
     WorkerImpl worker01 = new WorkerImpl();
     WorkerImpl worker02 = new WorkerImpl();
     Configuration conf = Configuration.builder()
             .host( rabbitMQContainerInfo.getHost() )
             .port( rabbitMQContainerInfo.getPort() )
             .threadPoolSize( 5 )
             .queueConfigs( new ArrayList<>(){{
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
     ```
## 4. 참고 사이트  
[RabbitMQ - Refer](https://www.rabbitmq.com/)
