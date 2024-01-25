package com.mobigen.datafabric.libs.rabbitmq;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChannelInfo {
    Integer id;
    ExchangeType exchangeType;
    String exchangeName;
    String queueName;
}
