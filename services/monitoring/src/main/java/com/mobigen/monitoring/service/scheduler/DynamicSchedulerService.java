package com.mobigen.monitoring.service.scheduler;

import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import com.mobigen.monitoring.vo.DynamicScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/* *
 * TODO
 *  1. 계속 돈다.
 *  2. map 확인해서 cron 이 맞는지 확인
 *  3. Thread pool 에 작업 던지기
 *  4. 모니터링 시작
 *  5. thread pool 은 10으로 유지
 *  6. 실제 모니터링 작업은 async 비동기
 * */
@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, DynamicScheduledTask> tasks = new ConcurrentHashMap<>();
    private final DatabaseManagementService databaseManagementService;

    @Scheduled(fixedRate = 1000)
    private void schedule() {
        // Map 확인 후 cron 규칙에 맞는지 확인 후에 실행
        for(DynamicScheduledTask task : tasks.values()) {

        }
    }

    public void addTask(String taskId, Runnable task, String cronExpression) {
        if (tasks.containsKey(taskId)) {
            removeTask(taskId); // 기존 task 제거
        }

        CronTrigger trigger = new CronTrigger(cronExpression);
        ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);

        DynamicScheduledTask scheduledTask = new DynamicScheduledTask();
        scheduledTask.setTaskId(taskId);
        scheduledTask.setTask(task);
        scheduledTask.setFuture(future);
        scheduledTask.setCronExpression(cronExpression);

        tasks.put(taskId, scheduledTask);
    }

    public void removeTask(String taskId) {
        DynamicScheduledTask scheduledTask = tasks.remove(taskId);
        if (scheduledTask != null && scheduledTask.getFuture() != null) {
            scheduledTask.getFuture().cancel(true);
        }
    }

    public void updateTask(String taskId, String newCron) {
        DynamicScheduledTask scheduledTask = tasks.get(taskId);
        if (scheduledTask != null) {
            addTask(taskId, scheduledTask.getTask(), newCron); // 기존 task로 재등록
        }
    }

    public Set<String> getRunningTaskIds() {
        return tasks.keySet();
    }
}
