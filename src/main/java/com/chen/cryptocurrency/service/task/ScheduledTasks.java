package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.MACDItem;
import com.chen.cryptocurrency.service.bean.TaskItem;
import com.chen.cryptocurrency.service.cache.KLineCache;
import com.chen.cryptocurrency.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class ScheduledTasks {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CoinService coinService;

    public static List<TaskItem> taskItems;

    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void reportCurrentTime() {
        logger.info("开始执行检查！");
        for (TaskItem item :
                taskItems) {
            checkMACD(item.getSymbol(), item.getType());
        }
        logger.info("检查完毕！");
    }

    private void checkMACD(String symbol, String type) {
        String buySign = "金叉！";
        String sellSign = "死叉！";

        String text = "注意观察！";

        List<KLineItem> list = coinService.queryKLine(symbol, type);
        List<MACDItem> macdList = coinService.macd(list, 5);

        logger.info("检查，币种：{}，时间：{}", symbol, type);
        for (MACDItem macd :
                macdList) {
            logger.info("结果:{}", macd.toString());
        }

        MACDItem macd;
        boolean lowBefore = true;
        boolean highNow = true;

        boolean highBefore = true;
        boolean lowNow = true;

        for (int i = 0; i < 4; i++) {
            macd = macdList.get(i);
            if (macd.getDif() > macd.getDea()) {
                lowBefore = false;
            }
            if (macd.getDif() < macd.getDea()) {
                highBefore = false;
            }
        }
        macd = macdList.get(4);
        if (macd.getDif() < macd.getDea()) {
            highNow = false;
        }
        if (macd.getDif() > macd.getDea()) {
            lowNow = false;
        }
        if (lowBefore && highNow) {
            MailUtil.sendMail(symbol + "_" + type + "_" + buySign, text);
        }
        if (highBefore && lowNow) {
            MailUtil.sendMail(symbol + "_" + type + "_" + sellSign, text);
        }
    }
}
