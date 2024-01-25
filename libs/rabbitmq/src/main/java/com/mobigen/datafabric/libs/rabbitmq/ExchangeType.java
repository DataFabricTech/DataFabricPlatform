package com.mobigen.datafabric.libs.rabbitmq;

import lombok.Getter;

@Getter
public enum ExchangeType {
    DIRECT( "direct" ),
    FANOUT( "fanout" ),
    HEADERS( "headers" ),
    TOPIC( "topic" ),
    // Work Queue 는 기본 Exchange Type 이 아님.
    WORK_QUEUE( "work_queue" );

    private final String type;

    ExchangeType( String type ) {
        this.type = type;
    }

}
