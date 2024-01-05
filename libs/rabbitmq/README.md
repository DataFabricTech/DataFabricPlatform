# RabbitMQ Library

## 소개
RabbitMQ 사용을 위한 라이브러리입니다.
ThreadPool 을 활성화하여 사용할 수 있으며, 메시지 수신 콜백을 등록하여 사용할 수 있습니다.
메시지 수신 콜백의 리턴 값이 false인 경우 메시지가 재전송(requeue)됩니다.

## 사용법  
### 1. 라이브러리 추가
```kotlin
dependencies {
    implementation("com.mobigen.datafabric.libs:rabbitmq")
}
```

### 2. Configuration 생성  
```java
RabbitMQConfiguration config = RabbitMQConfiguration.builder()
    .host("localhost")      // RabbitMQ host
    .queueConfig( new ArrayList<QueueConfig>() {
        {
            add( new QueueConfig( "queue1", "exchange1", "routingKey1" ) );
            add( new QueueConfig( "queue2", "exchange2", "routingKey2" ) );
        }
    } )
    .build();
```

### 3. Consumer의 경우 Worker를 구현
```java
public class Worker implements rabbitmq.Worker {
    @Override
    public boolean work( String message ) {
        System.out.println( message );
        return true;
    }
}
```
```java
public class RabbitMQClient {
    private RabbitMQClient client = null;
    public void sample() {
        RabbitMQClient client = RabbitMQClient.getInstance();
        client.Initialize(config);
    }
}
```
