package com.mobigen.datafabric.extraction;

import com.mobigen.datafabric.libs.rabbitmq.Client;
import com.mobigen.datafabric.libs.rabbitmq.Configuration;
import com.mobigen.datafabric.libs.rabbitmq.ExchangeType;
import com.mobigen.datafabric.libs.rabbitmq.Worker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExtractionApplication {
    public static void main(String[] args) {
        log.debug("Extractor Main Start - receiver라고 생각하면 된다!");

//        final Integer queueId = 1;
        final WorkerImpl extractionWorker01 = new WorkerImpl();
        final String queueName = "test";
        var client = Client.getInstance();
        Configuration conf = Configuration.builder()
                .host("192.168.106.104")
                .port(5672)
                .threadPoolSize(5)
                .queueConfigs(new ArrayList<>() {{
//                    add(Configuration.QueueConfig.builder()
//                            .isPublisher(true)
//                            .id(queueId)
//                            .exchangeType(ExchangeType.WORK_QUEUE)
//                            .queueName(queueName)
//                            .numChannel(1)
//                            .build());
                    add(Configuration.QueueConfig.builder()
                            .isPublisher(false)
                            .exchangeType(ExchangeType.WORK_QUEUE)
                            .queueName(queueName)
                            .prefetchSize(10)
                            .numChannel(1)
                            .worker(extractionWorker01)
                            .build());
                }})
                .build();

        try {
            client.initialize( conf );
        } catch(IOException | TimeoutException e ) {
            System.out.println(e);
//            throw new RuntimeException( "Client Initialize : ", e );
        }
        while(true) {

        }
    }

    private static class WorkerImpl implements Worker {
        AtomicInteger receiveCount;

        public WorkerImpl() {
            this.receiveCount = new AtomicInteger(0);
        }

        @Override
        public boolean doWork(String exchange, String routingKey, byte[] message) {
            var msg = new String(message);
            /**
             * todo message 분석 및 그에 따른 함수 실행
             * storageMetadataExctract(UUID): void
             * modelMetadataExctract(UUID): void
             * upsertMetadata(dto.StorageMetadata): void
             * upsertMetadata(dto.ModelMetadata): void
             *
             * 여기서 이게 들어간다.
             */

            System.out.printf("Thread[ %s ] E[ %s ] R[ %s ] Msg[ %s ] Count[ %d ]\n",
                    Thread.currentThread().getName(), exchange, routingKey, msg, receiveCount.getAndIncrement());
            // 메시지에 'true' 가 포함되었는가 아닌가를 이용해 반환 처리 : true 를 반환하면 consumer ack 를 정상으로 전송
            System.out.println("Receiver receive the message");
            return msg.contains("true");
        }

        public int getReceiveCount() {
            return receiveCount.get();
        }
    }
}