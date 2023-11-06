package com.mobigen.datafabric.core.worker.timer;

import com.mobigen.datafabric.core.worker.Worker;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TimerTest {

    @Test
    void add() throws InterruptedException {
        Timer t = new Timer( 2, null );
        Runnable timerRunnable = t::Start;
        Thread timerThread = new Thread( timerRunnable );
        timerThread.start();

        int id01 = t.Add( null, null, false, null );
        assertTrue( id01 < 0 );

        id01 = t.Add( null, null, false, null );
        assertTrue( id01 < 0 );

        id01 = t.Add( null, 100L, false, null );
        assertTrue( id01 < 0 );

        id01 = t.Add( null, 300L, false, null );
        assertTrue( id01 < 0 );

        long runTime = System.currentTimeMillis() + 5000;
        runner r = new runner();
        id01 = t.Add( runTime, null, false, r );
        assertTrue( id01 >= 0 );

        r = new runner();
        int id02 = t.Add( null, 300L, false, r );
        assertTrue( id02 >= 0 );

        r = new runner();
        int id03 = t.Add( null, 400L, true, r );
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
        assertEquals( runTime, t.getTimerList().get( 0 ).getStartTime());

        t.Stop();
        timerThread.join();
    }

    @Test
    void del() throws InterruptedException {
        Worker w = new Worker( 5 );
        Timer timer = new Timer( 50, w);
        Runnable timerRunnable = timer::Start;
        Thread timerThread = new Thread( timerRunnable );
        timerThread.start();

        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);

        for( int i = 0; i < 50; i++) {
            long runTime = System.currentTimeMillis() + (rand.nextLong(900) + 101);
            runner r = new runner();
            int id01 = timer.Add( runTime, null, false, r );
            assertTrue( id01 >= 0 );
            System.out.println("add id : " + id01);
        }

        for( int i = 0; i < 10; i++) {
            int delId = i + 1;
            System.out.println("delete id : " + delId);
            assertTrue(timer.Del( delId ));
        }

        Thread.sleep(2000);

        for( int i = 10; i < 20; i++) {
            assertFalse(timer.Del( i + 1 ));
        }

        timer.Stop();
        timerThread.join();
        w.shutdown();
    }

    @Test
    void start() throws InterruptedException {
        Worker w = new Worker( 5 );
        Timer timer = new Timer( 50, w);
        Runnable timerRunnable = timer::Start;
        Thread timerThread = new Thread( timerRunnable );
        timerThread.start();

        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);

        for( int i = 0; i < 50; i++) {
            long runTime = System.currentTimeMillis() + (rand.nextLong(900) + 101);
            runner r = new runner();
            int id01 = timer.Add( runTime, null, false, r );
            assertTrue( id01 >= 0 );
            System.out.println("add id : " + id01);
        }

        for( int i = 0; i < 10; i++) {
            int old = timer.getNumTimer();
            Thread.sleep(100);
            int now = timer.getNumTimer();
            assertTrue(old >= now );
        }

        timer.Stop();
        timerThread.join();
        w.shutdown();
    }

    @Test
    void fullTest() throws InterruptedException {
        Worker w = Mockito.mock(Worker.class);
        Timer timer = new Timer( 100, w);
        Runnable timerRunnable = timer::Start;
        Thread timerThread = new Thread( timerRunnable );
        timerThread.start();

        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);

        for( int i = 0; i < 100; i++) {
            runner r = new runner();
            int id01 = timer.Add( null, rand.nextLong(900) + 101, false, r );
            assertTrue( id01 >= 0 );
        }

        for( int i = 0; i < 50; i++) {
            Thread.sleep(1);
            int delId = rand.nextInt(100) + 1;
            timer.Del( delId );
        }

        Mockito.verify(w, Mockito.after(2000).atLeast(50)).runTask(Mockito.any());

        timer.Stop();
        timerThread.join();
        w.shutdown();

    }


    public static class runner implements TimerCallback {
        @Override
        public void callback( TimerData data ) {
            System.out.println( data.toString() );
        }
    }
}