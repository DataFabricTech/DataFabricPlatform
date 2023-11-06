package com.mobigen.datafabric.core.worker.timer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimerData {
    private Integer timerId;
    private Long startTime;
    private final Long period;
    private final Boolean isRepeat;
    private final TimerCallback callback;

    public TimerData( Long startTime, Long period, Boolean isRepeat, TimerCallback callback ) {
        this.startTime = startTime;
        this.period = period;
        this.isRepeat = isRepeat;
        this.callback = callback;
    }
}
