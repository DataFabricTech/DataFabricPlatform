package com.mobigen.datafabric.core.worker.timer;

import com.mobigen.datafabric.core.worker.Worker;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Timer {
    private final List<TimerData> timerList;
    private final int maxSize;
    private boolean isStop;
    AtomicInteger numTimer;
    private final Worker worker;

    public Timer( int maxSize, Worker worker ) {
        this.timerList = Collections.synchronizedList( new ArrayList<>() );
        this.maxSize = maxSize;
        numTimer = new AtomicInteger();
        this.worker = worker;
    }

    public int Add( Long utcMilliSecond, Integer delayMilliSecond, Boolean isRepeat, TimerCallback callback ) {
        if( numTimer.get() + 1 >= this.maxSize ) return -1;
        int id = numTimer.incrementAndGet() % this.maxSize;
        TimerData timer = TimerData.builder()
                .id( id )
                .runTime( utcMilliSecond )
                .period( delayMilliSecond )
                .isRepeat( isRepeat )
                .callback( callback )
                .build();
        timerList.add( timer );
        return id;
    }

    public boolean Del( int id ) {
        for( TimerData timer : timerList ) {
            if( timer.getId() != id ) continue;
            timerList.remove( timer );
            numTimer.decrementAndGet();
            return true;
        }
        return false;
    }


    public void Start() {
        long now;
        while( isStop ) {
            // Get Now UTC MilliSecond
            now = System.currentTimeMillis();

            // 삭제 리스트
            List<TimerData> delList = new ArrayList<>( );

            // 실행 & 재설정
            for( TimerData timer : timerList ) {
                if( timer.getRunTime() > now ) break;
                if( timer.isRepeat() ) {
                    timer.setRunTime( now + timer.getPeriod() );
                } else {
                    delList.add( timer );
                }
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        timer.getCallback().callback( timer );
                    }
                };
                worker.runTask( r );
            }

            // Delete
            for( TimerData timer : delList ) {
                timerList.remove( timer );
                numTimer.decrementAndGet();
            }

            // 대기
            try {
                Thread.sleep( 100);
            } catch( InterruptedException e ) {
                log.error( "[ Timer ] Error: {}", e.getMessage() );
            }
        }
    }

    public void Stop() {
        isStop = true;
    }
}
