package com.mobigen.datafabric.libs.rabbitmq;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RabbitMQClient {
    private static RabbitMQClient instance = null;

    // singleton pattern
    private RabbitMQClient() {
    }

    public RabbitMQClient getInstance() {
        if( instance == null ) {
            synchronized( RabbitMQClient.class ) {
                if( instance == null )
                    instance = new RabbitMQClient();
            }
            instance = new RabbitMQClient();
        }
        return instance;
    }

    Configuration config;
    Connection conn;
    Map<String, Channel> channels;

    public void Initialize( Configuration config ) throws IOException, TimeoutException {
        this.config = config;
        // Factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost( config.getHost() );
        // Connection
        conn = factory.newConnection();
        // Channel
        config.queueConfigs.forEach( queueConfig -> {
            try {
                // For Publisher
                Channel channel = conn.createChannel();
                channel.exchangeDeclare( queueConfig.getExchangeName(), queueConfig.getExchangeType() );

                // For Consumer
                if( !queueConfig.isPublisher ) {
                    // queueDeclare(String queue(name), boolean durable,
                    //      boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
                    String queueName = channel.queueDeclare( "", false, false, false, null ).getQueue();
                    if( queueConfig.getExchangeType().equals( BuiltinExchangeType.FANOUT ) ) {
                        // Broadcasting(Publisher -> Multiple Consumers(Same Message))
                        channel.queueBind( queueName, queueConfig.getExchangeName(), "" );
                    } else if( queueConfig.getExchangeType().equals( BuiltinExchangeType.DIRECT ) ||
                            queueConfig.getExchangeType().equals( BuiltinExchangeType.TOPIC ) ) {
                        // Topic/Routing
                        if( queueConfig.getRoutingKeys() != null ) {
                            queueConfig.getRoutingKeys().forEach( routingKey -> {
                                try {
                                    channel.queueBind( queueName, queueConfig.getExchangeName(), routingKey );
                                } catch( IOException e ) {
                                    log.error( "Failed to bind queue", e );
                                }
                            } );
                        }
                    }
                }
                channels.put( queueConfig.getExchangeName(), channel );
            } catch( IOException e ) {
                log.error( "Failed to create channel", e );
            }
        } );
    }

    public void setDeliverCallback( String exchangeName, String queueName, String routingKey, DeliverCallback callback ) throws IOException {
        Channel channel = channels.get( exchangeName );
        channel.basicConsume( queueName, true, callback, consumerTag -> { } );
    }

    public void shutdown() throws IOException {
        channels.forEach( ( exchangeName, channel ) -> {
            try {
                channel.close();
            } catch( IOException | TimeoutException e ) {
                log.error( "Failed to close channel", e );
            }
        } );
        conn.close();
    }

    public void publish( String exchangeName, String routingKey, String message ) throws IOException {
        Channel channel = channels.get( exchangeName );
        channel.basicPublish( exchangeName, routingKey, null, message.getBytes() );
    }

    public Channel getChannel( String exchangeName ) {
        return channels.get( exchangeName );
    }
}