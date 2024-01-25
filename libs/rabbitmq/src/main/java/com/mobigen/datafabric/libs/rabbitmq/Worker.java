package com.mobigen.datafabric.libs.rabbitmq;

public interface Worker {
    boolean doWork( String exchange, String routingKey, byte[] message );
}
