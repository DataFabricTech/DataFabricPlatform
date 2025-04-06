package com.mobigen.monitoring.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ScheduledFuture;

@Setter
@Getter
public class DynamicScheduledTask {
    private String taskId;
    private ScheduledFuture<?> future;
    private Runnable task;
    private String cronExpression;
}
