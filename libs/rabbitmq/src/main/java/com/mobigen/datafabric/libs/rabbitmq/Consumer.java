package com.mobigen.datafabric.libs.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Consumer extends DefaultConsumer {
    final Channel channel;
    final Worker worker;
    public Consumer( Worker worker, Channel channel ) {
        super( channel );
        this.worker = worker;
        this.channel = channel;
    }

    @Override
    public void handleDelivery( String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body ) {
        if( worker.doWork( envelope.getExchange(), envelope.getRoutingKey(), body ) ) {
            try {
                channel.basicAck( envelope.getDeliveryTag(), false );
            } catch( Exception e ) {
                log.error( "Exchange[{}] RoutingKey[{}] Msg[{}] Failed To BasicAck[ {} ]",
                        envelope.getExchange(), envelope.getRoutingKey(), new String( body ), e.getMessage() );
            }
        } else {
            try {
                channel.basicReject( envelope.getDeliveryTag(), false );
            } catch( Exception e ) {
                log.error( "Exchange[{}] RoutingKey[{}] Msg[{}] Failed To BasicAck[ {} ]",
                        envelope.getExchange(), envelope.getRoutingKey(), new String( body ), e.getMessage() );
            }
        }
    }
}
