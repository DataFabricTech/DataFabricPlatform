package com.mobigen.monitoring.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Builder
public class Target {
    private String serviceId;
    private AtomicInteger period;
}
