package com.mobigen.datafabric.core.worker.timer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TimerTest {

    @Test
    void add() throws InterruptedException {
        Timer t = new Timer( 2, null );
        Runnable timerRunnable = t::Start;
        Thread timerThread = new Thread( timerRunnable );
        timerThread.start();

        long runTime = System.currentTimeMillis() + 5000;
        runner r = new runner();
        int id01 = t.Add( runTime, null, false, r );
        assertTrue( id01 >= 0 );

        runTime += 1000;
        r = new runner();
        int id02 = t.Add( runTime, null, false, r );
        assertTrue( id02 >= 0 );

        runTime += 1000;
        r = new runner();
        int id03 = t.Add( runTime, null, false, r );
        assertTrue( id03 < 0 );

        assertTrue( t.Del( id02 ) );
        r = new runner();
        id03 = t.Add( runTime, null, false, r );
        assertTrue( id03 >= 0 );
        assertTrue( t.Del( id03 ) );

        runTime = System.currentTimeMillis() + 1000;
        r = new runner();
        int id04 = t.Add( runTime, null, false, r );
        assertTrue( id04 >= 0 );
        // 다른 녀석들보다 시간이 빠르므로 제일 먼저 들어가 있어야 함.
        assertEquals( runTime, t.getTimerList().get( 0 ).getRunTime());

        t.Stop();
        timerThread.join();
    }

    @Test
    void del() {
    }

    @Test
    void start() {
    }

    @Test
    void stop() {
    }

    public static class runner implements TimerCallback {
        @Override
        public void callback( TimerData data ) {
            System.out.println( data.toString() );
        }
    }
}