package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.MACDItem;
import com.chen.cryptocurrency.service.bean.TaskItem;
import com.chen.cryptocurrency.util.MailUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class ScheduledTasks implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CoinService coinService;

    public static List<TaskItem> taskItems;
    private static List<String> mailRecord;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (taskItems == null) {
            taskItems = Lists.newArrayList();
        }
        if (mailRecord == null) {
            mailRecord = Lists.newArrayList();
        }
        taskItems.add(new TaskItem("btc_usd", "2hour"));
        taskItems.add(new TaskItem("btc_usd", "4hour"));
        taskItems.add(new TaskItem("btc_usd", "6hour"));

        taskItems.add(new TaskItem("eth_usd", "2hour"));
        taskItems.add(new TaskItem("eth_usd", "4hour"));
        taskItems.add(new TaskItem("eth_usd", "6hour"));
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void reportCurrentTime() {
        logger.info("开始执行检查！");
        for (TaskItem item :
                taskItems) {
            checkMACD(item.getSymbol(), item.getType());
        }
        logger.info("检查完毕！");
    }

    private void checkMACD(String symbol, String type) {
        logger.info("检查，币种：{}，时间：{}", symbol, type);


        List<KLineItem> list = coinService.queryKLine(symbol, type);
        List<MACDItem> macdList = coinService.macd(list, 5);

        for (MACDItem macd : macdList) {
            logger.info("结果:{}", macd.toString());
        }

        if (mailRecord.contains(macdList.toString())) {
            logger.info("结果与上次相同，直接返回");
            return;
        }

        checkCross(symbol, type, macdList);
        checkTrendency(symbol, type, macdList);

        mailRecord.add(macdList.toString());
        if (mailRecord.size() > 100) {
            mailRecord = mailRecord.subList(90, mailRecord.size());
        }
    }

    private void checkTrendency(String symbol, String type, List<MACDItem> macdList) {
        List<Double> difList = macdList.stream().map(MACDItem::getDif).collect(Collectors.toList());

        int size = difList.size();

        String buySign = "转折向上，请注意";
        String sellSign = "转折向下，请注意";

        if (difList.get(size - 1) > difList.get(size - 2)
                && difList.get(size - 2) > difList.get(size - 3)
                && difList.get(size - 3) < difList.get(size - 4)) {
            String subject = "币种" + symbol + buySign;

            String text = "币种：" + symbol + "\n" +
                    "时间线：" + type + "\n" +
                    "信号：" + buySign;

            MailUtil.sendMail(subject, text);
        }

        if (difList.get(size - 1) < difList.get(size - 2)
                && difList.get(size - 2) < difList.get(size - 3)
                && difList.get(size - 3) > difList.get(size - 4)) {
            String subject = "币种" + symbol + sellSign;

            String text = "币种：" + symbol + "\n" +
                    "时间线：" + type + "\n" +
                    "信号：" + sellSign;

            MailUtil.sendMail(subject, text);
        }
    }

    private void checkCross(String symbol, String type, List<MACDItem> macdList) {
        int size = macdList.size();

        boolean lowBefore = true;
        boolean lowNow = true;

        MACDItem macdBefore = macdList.get(size - 2);
        MACDItem macdNow = macdList.get(size - 1);

        if (macdBefore.getDif() > macdBefore.getDea()) {
            lowBefore = false;
        }
        if (macdNow.getDif() > macdNow.getDea()) {
            lowNow = false;
        }

        String buySign = "呈现金叉";
        String sellSign = "呈现死叉";

        if (lowBefore && !lowNow) {
            String subject = "币种" + symbol + buySign;

            String text = "币种：" + symbol + "\n" +
                    "时间线：" + type + "\n" +
                    "信号：" + buySign;

            MailUtil.sendMail(subject, text);
        }
        if (!lowBefore && lowNow) {
            String subject = "币种" + symbol + sellSign;

            String text = "币种：" + symbol + "\n" +
                    "时间线：" + type + "\n" +
                    "信号：" + sellSign;

            MailUtil.sendMail(subject, text);
        }
    }
}
