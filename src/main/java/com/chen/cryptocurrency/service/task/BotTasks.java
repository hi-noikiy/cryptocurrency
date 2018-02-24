package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class BotTasks {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Scheduled(cron = "0 * * * * ? ")
    public void reportCurrentTime() {

    }
}
