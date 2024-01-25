package com.mobigen.datafabric.libs.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigurationTest {

    @Test
    void buildTest() {
        Configuration c = Configuration.builder()
                .host( "localhost" )
                .queueConfigs( new ArrayList<>() {{
                    Configuration.QueueConfig q1 = Configuration.QueueConfig.builder()
                            .exchangeName( "hello" )
                            .exchangeType( ExchangeType.TOPIC )
                            .routingKeys( new ArrayList<>() {{
                                add( "r1" );
                                add( "r2" );
                            }} )
                            .build();
                    add( q1 );
                    Configuration.QueueConfig q2 = Configuration.QueueConfig.builder()
                            .exchangeName( "world" )
                            .exchangeType( ExchangeType.FANOUT )
                            .numChannel( 2 )
                            .build();
                    add( q2 );
                }} )
                .build();
        assertEquals( "localhost", c.getHost() );
        assertEquals( "hello", c.getQueueConfigs().get( 0 ).getExchangeName() );
        assertEquals( BuiltinExchangeType.TOPIC, c.getQueueConfigs().get( 0 ).getExchangeType() );
        assertEquals( "r1", c.getQueueConfigs().get( 0 ).getRoutingKeys().get( 0 ) );
        assertEquals( "r2", c.getQueueConfigs().get( 0 ).getRoutingKeys().get( 1 ) );
        assertNull( c.getQueueConfigs().get( 0 ).getNumChannel(), "Channel Count Should Be Null" );

        assertEquals( "world", c.getQueueConfigs().get( 1 ).getExchangeName() );
        assertEquals( BuiltinExchangeType.FANOUT, c.getQueueConfigs().get( 1 ).getExchangeType(), "exchangeType of second queue should be FANOUT" );
        assertNull( c.getQueueConfigs().get( 1 ).getRoutingKeys(), "routingKeys should be null" );
        assertEquals( 2, c.getQueueConfigs().get( 1 ).getNumChannel(), "Channel Count Should Be 2" );
    }
}