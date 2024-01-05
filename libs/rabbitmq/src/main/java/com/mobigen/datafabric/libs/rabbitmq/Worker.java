package com.mobigen.datafabric.libs.rabbitmq;

public interface Worker {
    Boolean doWork( String exchange, String routingKey, byte[] message );
}
