package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CsvWriteTasks{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CoinService coinService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void writeTask() {
        logger.info("write task begin !");
        coinService.csvSync();
    }
}
