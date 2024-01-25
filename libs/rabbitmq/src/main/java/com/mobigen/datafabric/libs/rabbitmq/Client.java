package com.mobigen.datafabric.libs.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Client {
    private static volatile Client instance = null;
    Map<ChannelInfo, Channel> producerChannels;
    Map<Integer, Channel> consumerChannels;
    AtomicInteger consumerChannelId;
    // singleton pattern
    private Client() {
        producerChannels = new HashMap<ChannelInfo, Channel>();
        consumerChannels = new HashMap<Integer, Channel>();
        consumerChannelId = new AtomicInteger(0);
    }

    public static Client getInstance() {
        if( instance == null ) {
            synchronized( Client.class ) {
                if( instance == null )
                    instance = new Client();
            }
        }
        return instance;
    }

    Configuration config;
    Connection conn;

    // 기본 옵션
    private static final int DEFAULT_PREFETCH_SIZE = 10;

    public void initialize( Configuration config) throws IOException, TimeoutException {
        this.config = config;
        // Factory
        var factory = new ConnectionFactory();
        factory.setHost( config.getHost() );
        factory.setPort( config.getPort() );

        if( config.getThreadPoolSize() != null && config.getThreadPoolSize() > 0 ) {
            factory.setSharedExecutor( Executors.newFixedThreadPool( config.getThreadPoolSize() ) );
        }
        // Connection
        conn = factory.newConnection();
        // loop for each queue config
        for( Configuration.QueueConfig queueConfig : config.queueConfigs ) {
            if( queueConfig.isPublisher ) {
                createChannelForPublisher( conn, queueConfig );
                continue;
            }
            createChannelForConsumer( conn, queueConfig );
        }
    }

    private void createChannelForPublisher( Connection conn, Configuration.QueueConfig queueConfig ) throws IOException {
        String exchangeName;
        String queueName;
        var channel = conn.createChannel();
        // Send Mode(Blocking or Non-Blocking) : v1.0.0에서는 Blocking 모드만 지원
        // if( queueConfig.getIsNonBlock() != null && queueConfig.getIsNonBlock() ) {
        channel.confirmSelect();
        // }
        if( queueConfig.getExchangeType().equals( ExchangeType.WORK_QUEUE ) ) {
            if( queueConfig.getQueueName() == null || queueConfig.getQueueName().isBlank() ) {
                throw new IllegalArgumentException( "Exchange WorkQueue Type QueueName Can't Null(Blank)");
            }
            // For WorkQueue Type
            // queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException
            channel.queueDeclare( queueConfig.getQueueName(), true, false, false, null );
            queueName = queueConfig.getQueueName();
            exchangeName = "";
        } else {
            if( queueConfig.getExchangeName() == null || queueConfig.getExchangeName().isBlank() ) {
                throw new IllegalArgumentException( "ExchangeName Can't Null(Blank)" );
            }
            // For Another Type
            // exchangeDeclare(String exchange, String type) ->
            // exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable(default: false), boolean autoDelete(default: false), Map<String, Object> arguments(default: null))
            channel.exchangeDeclare( queueConfig.getExchangeName(), queueConfig.getExchangeType().getType() );
            exchangeName = queueConfig.getExchangeName();
            queueName = "";
        }
        // ChannelInfo
        ChannelInfo channelInfo = ChannelInfo.builder()
                .id( queueConfig.getId() )
                .exchangeType( queueConfig.getExchangeType() )
                .exchangeName( exchangeName )
                .queueName( queueName )
                .build();
        if( checkDuplicateChannel( producerChannels, channelInfo ) ) {
            // Producer 가 하나의 Exchange 에 여러개의 Channel 을 생성하는 것은 지원하지 않음(v1.0.0) 기준.
            throw new IllegalArgumentException( "Duplicate Channel ID(ID, Exchange). Not Supported Multiple Channel For Producer." );
        }
        producerChannels.put( channelInfo, channel );
    }

    private boolean checkDuplicateChannel( Map<ChannelInfo, Channel> storage, ChannelInfo channelInfo ) {
        if( storage.isEmpty() ) {
            return false;
        }
        for( ChannelInfo info : storage.keySet() ) {
            if( info.getId().equals( channelInfo.getId() ) ) {
                return true;
            }
        }
        return false;
    }

    private void createChannelForConsumer( Connection conn, Configuration.QueueConfig queueConfig ) throws IOException {
        var channelCount = queueConfig.getNumChannel() == null ? 1 : queueConfig.getNumChannel();
        for( int i = 0; i < channelCount; i++ ) {
            String queueName;
            var channel = conn.createChannel();
            if( queueConfig.getExchangeType().equals( ExchangeType.WORK_QUEUE )) {
                if( queueConfig.getQueueName() == null || queueConfig.getQueueName().isBlank() ) {
                    throw new IllegalArgumentException( "Exchange WorkQueue Type QueueName Can't Null(Blank)" );
                }
                // For Work Queue
                // queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
                channel.queueDeclare( queueConfig.getQueueName(), true, false, false, null );
                // prefetchCount
                if( queueConfig.getPrefetchSize() == null || queueConfig.getPrefetchSize() < 1 ) {
                    channel.basicQos( DEFAULT_PREFETCH_SIZE );
                } else {
                    channel.basicQos( queueConfig.getPrefetchSize() );

                }
                // Queue name
                queueName = queueConfig.getQueueName();

                consumerChannels.put( consumerChannelId.getAndIncrement(), channel );

            } else {
                if( queueConfig.getExchangeName() == null || queueConfig.getExchangeName().isBlank() ) {
                    throw new IllegalArgumentException( "ExchangeName Can't Null(Blank)" );
                }
                // exchangeDeclare(String exchange, String type) ->
                // exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable(default: false), boolean autoDelete(default: false), Map<String, Object> arguments(default: null)
                channel.exchangeDeclare( queueConfig.getExchangeName(), queueConfig.getExchangeType().getType() );
                queueName = channel.queueDeclare().getQueue();
                if( queueConfig.getExchangeType().equals( ExchangeType.FANOUT ) ) {
                    channel.queueBind( queueName, queueConfig.getExchangeName(), "" );
                } else if( queueConfig.getExchangeType().equals( ExchangeType.DIRECT  ) ||
                        queueConfig.getExchangeType().equals( ExchangeType.TOPIC ) ) {
                    if( queueConfig.getRoutingKeys() != null ) {
                        for( String routingKey : queueConfig.getRoutingKeys() ) {
                            channel.queueBind( queueName, queueConfig.getExchangeName(), routingKey );
                        }
                    }
                } else {
                    throw new IllegalArgumentException( "Not Supported Exchange Type" );
                }
                consumerChannels.put( consumerChannelId.getAndIncrement(), channel );
            }
            // basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException {
            channel.basicConsume( queueName, false, new Consumer( queueConfig.worker, channel ) );
        }
    }

    public void shutdown() throws IOException {
        if( consumerChannels != null && !consumerChannels.isEmpty() ) {
            consumerChannels.forEach( ( id, channel ) -> {
                try {
                    channel.close();
                } catch( IOException | TimeoutException e ) {
                    log.error( "Failed to close consumer channel", e );
                }
            } );
            consumerChannels.clear();
        }
        if( producerChannels != null && !producerChannels.isEmpty() ) {
          producerChannels.forEach( (info, channel) -> {
              try {
                  channel.close();
              } catch( IOException | TimeoutException e ) {
                  log.error( "Failed to close producer channel", e );
              }
          } );
            producerChannels.clear();
        }
        conn.close();
    }

    public Channel getChannel( Integer qID ) {
        if( qID == null ) {
            throw new IllegalArgumentException( "No Channel ID" );
        }
        // Get Channel
        var channel = getProducerChannel( qID );
        if( channel == null ) {
            throw new IllegalArgumentException( "No Channel By qID" );
        }
        return channel;
    }

    public void publish( Integer qID, String routingKey, String message ) throws IOException, InterruptedException, TimeoutException {
        if( qID == null ) {
            throw new IllegalArgumentException( "No Channel ID" );
        }
        // Get ChannelInfo
        var channelInfo = getProducerChannelInfo( qID );
        if( channelInfo == null ) {
            throw new IllegalArgumentException( "No ChannelInfo By qID" );
        }
        // Get Channel
        var channel = getProducerChannel( qID );
        if( channel == null ) {
            throw new IllegalArgumentException( "No Channel By qID" );
        }
        // Exchange Type 별 메시지 전송
        if( channelInfo.getExchangeType().equals( ExchangeType.WORK_QUEUE ) ) {
            // For Work Queue
            channel.basicPublish( "", channelInfo.getQueueName(), null, message.getBytes() );
            channel.waitForConfirmsOrDie(5_000);
            return;
        }
        channel.basicPublish( channelInfo.getExchangeName(), routingKey, null, message.getBytes() );
        channel.waitForConfirmsOrDie(5_000);

        // Message Properties
//            public static final AMQP.BasicProperties MINIMAL_BASIC =
//                  new AMQP.BasicProperties((String)null, (String)null, (Map)null, (Integer)null, (Integer)null, (String)null, (String)null,
//                          (String)null, (String)null, ( Date )null, (String)null, (String)null, (String)null, (String)null);
//            public static final AMQP.BasicProperties MINIMAL_PERSISTENT_BASIC =
//                  new AMQP.BasicProperties((String)null, (String)null, (Map)null, 2, (Integer)null, (String)null, (String)null, (String)null,
//                          (String)null, (Date)null, (String)null, (String)null, (String)null, (String)null);
//            public static final AMQP.BasicProperties BASIC =
//                  new AMQP.BasicProperties("application/octet-stream", (String)null, (Map)null, 1, 0, (String)null, (String)null, (String)null,
//                          (String)null, (Date)null, (String)null, (String)null, (String)null, (String)null);
//            public static final AMQP.BasicProperties PERSISTENT_BASIC =
//                  new AMQP.BasicProperties("application/octet-stream", (String)null, (Map)null, 2, 0, (String)null, (String)null, (String)null,
//                          (String)null, (Date)null, (String)null, (String)null, (String)null, (String)null);
//            public static final AMQP.BasicProperties TEXT_PLAIN = new AMQP.BasicProperties("text/plain", (String)null, (Map)null, 1, 0, (String)null, (String)null, (String)null, (String)null, (Date)null, (String)null, (String)null, (String)null, (String)null);
//            public static final AMQP.BasicProperties PERSISTENT_TEXT_PLAIN = new AMQP.BasicProperties("text/plain", (String)null, (Map)null, 2, 0, (String)null, (String)null, (String)null, (String)null, (Date)null, (String)null, (String)null, (String)null, (String)null);
    }

    // Queue ID를 이용해 ChannelInfo 를 찾는다.
    private ChannelInfo getProducerChannelInfo( Integer qID ) {
        for( ChannelInfo info : producerChannels.keySet() ) {
            if( info.getId().equals( qID ) ) {
                return info;
            }
        }
        return null;
    }

    // Queue ID를 이용해 Channel 을 찾는다.
    private Channel getProducerChannel( Integer qID ) {
        for( ChannelInfo info : producerChannels.keySet() ) {
            if( info.getId().equals( qID ) ) {
                return producerChannels.get( info );
            }
        }
        return null;
    }
}