package com.mobigen.datafabric.core.worker.timer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class TimerData {
    private final int id;
    private long runTime;
    private final long period;
    private final boolean isRepeat;
    private final TimerCallback callback;

    public TimerData( int id, long runTime, long period, boolean isRepeat, TimerCallback callback ) {
        this.id = id;
        this.runTime = runTime;
        this.period = period;
        this.isRepeat = isRepeat;
        this.callback = callback;
    }
}
