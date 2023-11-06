package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.worker.Job;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoAddTask implements Runnable {
    private final Job job;

    public AutoAddTask( Job job ) {
        this.job = job;
    }

    @Override
    public void run() {
        log.error( "[ Auto Add Task ] Start : OK" );
        log.error( "[ Auto Add Task ] Job Info[ {} ]", job.toString() );

        // TODO : 필요한 정보 불러오기
        // TODO : 실행 취소 확인

        // TODO : 실행 결과 저장
        // TODO : 필요한 경우 알림 전송 개발 추가 - 아마도 많은 변경 필요
    }
}
