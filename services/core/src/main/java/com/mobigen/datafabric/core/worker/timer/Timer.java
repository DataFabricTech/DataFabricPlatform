package com.mobigen.datafabric.core.worker.timer;

import com.mobigen.datafabric.core.worker.Worker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Getter
public class Timer {
    private final LinkedList<TimerData> timerList;
    private final int[] idArray;
    private final int maxSize;
    private boolean isStop;
    AtomicInteger idGenerator;
    AtomicInteger numTimer;
    private final Worker worker;
    private Semaphore mutex = new Semaphore( 1 );

    public Timer( int maxSize, Worker worker ) {
        this.timerList = new LinkedList<>();
        this.maxSize = maxSize;
        numTimer = new AtomicInteger(0);
        idGenerator = new AtomicInteger(0);
        this.worker = worker;
        idArray = new int[ maxSize ];
    }

    public int getId() {
        // 빈 아이디를 찾기위해 전체를 순회를 하지 않기 위함.
        int idx = idGenerator.getAndIncrement() % this.maxSize;
        for( int i = 0; i < this.maxSize; i++ ) {
            if( idArray[ idx ] == 0 ) {
                idArray[ idx ] = 1;
                return idx + 1;
            }
            idx = ( idx + 1 ) % this.maxSize;
        }
        return -1;
    }

    public void delId( int id ) {
        idArray[ id - 1 ] = 0;
    }

    public int Add( Long utcMilliSecond, Integer delayMilliSecond, Boolean isRepeat, TimerCallback callback ) {
        try {
            mutex.acquire();
            if( numTimer.get() + 1 > this.maxSize ) return -1;
            if( delayMilliSecond != null && delayMilliSecond < 500 ) return -1;
            int id = getId();
            TimerData timer = null;
            long runTime;
            if( utcMilliSecond != null ) {
                runTime = utcMilliSecond;
                timer = TimerData.builder()
                        .id( id )
                        .runTime( utcMilliSecond )
                        .period( 0 )
                        .isRepeat( false )
                        .callback( callback )
                        .build();
            } else {
                runTime = System.currentTimeMillis() + delayMilliSecond;
                timer = TimerData.builder()
                        .id( id )
                        .runTime( runTime )
                        .period( delayMilliSecond )
                        .isRepeat( isRepeat )
                        .callback( callback )
                        .build();
            }
            if( timerList.isEmpty() ) {
                timerList.add( timer );
            } else {
                int insertIdx = 0;
                for( insertIdx = 0; insertIdx < timerList.size(); insertIdx++ ) {
                    if( timerList.get( insertIdx ).getRunTime() < runTime ) continue;
                    break;
                }
                timerList.add( insertIdx, timer );
            }
            numTimer.incrementAndGet();
            return id;
        } catch( InterruptedException e ) {
            log.error( "[ Timer ] Error : Add Time Callback Msg[ {} ]", e.getMessage() );
        } finally {
            mutex.release();
        }
        return -1;
    }

    public boolean Del( int id ) {
        try {
            mutex.acquire();
            for( TimerData timer : timerList ) {
                if( timer.getId() != id ) continue;
                timerList.remove( timer );
                numTimer.decrementAndGet();
                delId( id );
                return true;
            }
        } catch( InterruptedException e ) {
            log.error( "[ Timer ] Error . Delete Timer. Input[ {} ]Msg[ {} ]", id, e.getMessage() );
        } finally {
            mutex.release();
        }
        return false;
    }


    public void Start() {
        long now;
        while( isStop ) {
            // Get Now UTC MilliSecond
            now = System.currentTimeMillis();

            try {
                mutex.acquire();
                // 실행 & 재설정
                for( TimerData t : timerList ) {
                    if( t.getRunTime() > now ) break;
                    timerList.remove( t );
                    if( t.isRepeat() ) {
                        t.setRunTime( now + t.getPeriod() );
                        timerList.add( t );
                    } else {
                        numTimer.decrementAndGet();
                        delId( t.getId() );
                    }
                    Runnable r = () -> t.getCallback().callback( t );
                    worker.runTask( r );
                }
                mutex.release();

                // 대기
                Thread.sleep( 100 );
            } catch( InterruptedException ignored ) {
                // ignored
            }
        }
    }

    public void Stop() {
        isStop = true;
    }
}
