package com.mobigen.datafabric.core.worker.timer;

import com.mobigen.datafabric.core.worker.Worker;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Timer {
    private static Timer instance = null;
    private final LinkedList<TimerData> timerList;
    private final int[] idArray;
    private final int maxSize;
    private boolean isStop = false;
    private final AtomicInteger idGenerator;
    private final AtomicInteger numTimer;
    private final Worker worker;
    private final Semaphore mutex;

    public Timer( int maxSize, Worker worker ) {
        this.timerList = new LinkedList<>();
        this.maxSize = maxSize;
        numTimer = new AtomicInteger( 0 );
        idGenerator = new AtomicInteger( 0 );
        this.worker = worker;
        idArray = new int[ maxSize ];
        mutex = new Semaphore( 1 );

        instance = this;
    }

    public static Timer getInstance() {
        if( instance == null ) {
            log.error( "[ Timer ] Not Init" );
            return null;
        }
        return instance;
    }

    public int getNumTimer() {
        return numTimer.get();
    }

    public List<TimerData> getTimerList() {
        return timerList;
    }

    private int getId() {
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

    private void delId( int id ) {
        idArray[ id - 1 ] = 0;
    }

    public int Add( Long utcMilliSecond, Long delayMilliSecond, Boolean isRepeat, TimerCallback callback ) {
        // Parameter Check
        if( utcMilliSecond == null && delayMilliSecond == null ) return -1;
        if( utcMilliSecond != null && utcMilliSecond < System.currentTimeMillis() ) return -1;
        if( delayMilliSecond != null && delayMilliSecond < 100 ) return -1;
        if( isRepeat != null && ( isRepeat && delayMilliSecond == null ) ) return -1;
        if( callback == null ) return -1;

        // Timer Size Check
        if( numTimer.get() + 1 > this.maxSize ) return -1;

        try {
            mutex.acquire();
            int id = getId();
            Long startTime = null;
            if( utcMilliSecond != null ) {
                startTime = utcMilliSecond;
            } else {
                startTime = System.currentTimeMillis() + delayMilliSecond;
            }
            TimerData tData = new TimerData( startTime, delayMilliSecond, isRepeat, callback );
            tData.setTimerId( id );

            // 실행 시간 순서에 맞춰 삽입
            int insertIdx = 0;
            for( insertIdx = 0; insertIdx < timerList.size(); insertIdx++ ) {
                if( timerList.get( insertIdx ).getStartTime() < startTime ) continue;
                break;
            }
            timerList.add( insertIdx, tData );
            numTimer.incrementAndGet();
            return id;
        } catch( InterruptedException e ) {
            log.error( "[ Timer ] Error : Add Time Callback Msg[ {} ]", e.getMessage() );
        } finally {
            mutex.release();
        }
        return -1;
    }

    public Integer Add( TimerData timerData ) {
        // Parameter Check
        if( timerData.getStartTime() == null && timerData.getPeriod() == null ) return -1;
        if( timerData.getStartTime() != null && timerData.getStartTime() < System.currentTimeMillis() ) return -1;
        if( timerData.getPeriod() != null && timerData.getPeriod() < 100 ) return -1;
        if( timerData.getIsRepeat() != null && ( timerData.getIsRepeat().equals( true ) && timerData.getPeriod() == null ) )
            return -1;
        if( timerData.getCallback() == null ) return -1;

        // Timer Size Check
        if( numTimer.get() + 1 > this.maxSize ) return -1;

        try {
            mutex.acquire();
            timerData.setTimerId( getId() );
            if( timerData.getStartTime() == null ) {
                timerData.setStartTime( System.currentTimeMillis() + timerData.getPeriod() );
            }

            // 실행 시간 순서에 맞춰 삽입
            int insertIdx = 0;
            for( insertIdx = 0; insertIdx < timerList.size(); insertIdx++ ) {
                if( timerList.get( insertIdx ).getStartTime() < timerData.getStartTime() ) continue;
                break;
            }
            timerList.add( insertIdx, timerData );
            numTimer.incrementAndGet();
            return timerData.getTimerId();
        } catch( InterruptedException e ) {
            log.error( "[ Timer ] Error : Add Time Callback Msg[ {} ]", e.getMessage() );
        } finally {
            mutex.release();
        }
        return -1;
    }


    public boolean Update( TimerData data ) {
        try {
            mutex.acquire();
            // 실행 시간 순서에 맞춰 삽입
            int insertIdx = 0;
            for( insertIdx = 0; insertIdx < timerList.size(); insertIdx++ ) {
                if( timerList.get( insertIdx ).getStartTime() < data.getStartTime() ) continue;
                break;
            }
            timerList.add( insertIdx, data );
            numTimer.incrementAndGet();
            return true;
        } catch( InterruptedException e ) {
            log.error( "[ Timer ] Error : Add Time Callback Msg[ {} ]", e.getMessage() );
        } finally {
            mutex.release();
        }
        return false;
    }

    public boolean Del( int id ) {
        try {
            mutex.acquire();
            for( TimerData timer : timerList ) {
                if( timer.getTimerId() != id ) continue;
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
        long sleepTime = 0;
        while( !isStop ) {
            // Get Now UTC MilliSecond
            now = System.currentTimeMillis();

            try {
                // 실행 & 재설정
                mutex.acquire();
                Iterator<TimerData> it = timerList.iterator();
                List<TimerData> delList = new LinkedList<>();
                List<TimerData> repeatList = new LinkedList<>();
                while( it.hasNext() ) {
                    TimerData td = it.next();
                    if( td.getStartTime() > now ) {
                        sleepTime = td.getStartTime() - now;
                        break;
                    }
                    // Run
                    Runnable r = () -> td.getCallback().callback( td );
                    System.out.println( "Run : " + td.getTimerId() );
                    worker.runTask( r );

                    // Add Delete List
                    delList.add( td );
                    // Add Repeat List
                    if( td.getIsRepeat() ) {
                        td.setStartTime( now + td.getPeriod() );
                        repeatList.add( td );
                    }
                }
                mutex.release();

                // 업데이트
                delList.forEach( timerData -> Del( timerData.getTimerId() ) );
                repeatList.forEach( this::Update );

                // 대기
                Thread.sleep( sleepTime == 0 ? 100 : sleepTime );
            } catch( InterruptedException ignored ) {
                // ignored
            }
        }
    }

    public void Stop() {
        isStop = true;
    }

    public void monitoring() {
        log.error( "[ Timer ] Reservation Count [ {} / {} ]", this.timerList.size(), this.maxSize );
    }
}
