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
    /* Service Dependency code */
    private String timerDataType;
    private String dataId;      // Storage ID, Data Model ID
    public static final String TIMER_DATA_TYPE_STORAGE = "storage";
    public static final String TIMER_DATA_TYPE_DATA_MODEL = "data_model";

    public TimerData( Long startTime, Long period, Boolean isRepeat, TimerCallback callback ) {
        this.startTime = startTime;
        this.period = period;
        this.isRepeat = isRepeat;
        this.callback = callback;
    }
}
