package com.chen.cryptocurrency.service.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class ScheduledTasks {
    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void reportCurrentTime() {

    }
}
