package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.MACDItem;
import com.chen.cryptocurrency.service.bean.TaskItem;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.MailUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
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
    private static Set<String> mailRecord;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (taskItems == null) {
            taskItems = Lists.newArrayList();
        }
        if (mailRecord == null) {
            mailRecord = Sets.newConcurrentHashSet();
        }
        taskItems.add(new TaskItem("btc", "2hour"));
        taskItems.add(new TaskItem("btc", "4hour"));
        taskItems.add(new TaskItem("btc", "6hour"));

        taskItems.add(new TaskItem("eth", "2hour"));
        taskItems.add(new TaskItem("eth", "4hour"));
        taskItems.add(new TaskItem("eth", "6hour"));
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void reportCurrentTime() {
        if (mailRecord.size() > 100) {
            mailRecord.clear();
        }
        logger.info("开始执行检查！");
        for (TaskItem item :
                taskItems) {
            checkMACD(item.getSymbol() + "_usd", item.getType(), Constant.EXCHANGE_OKCOIN);
            checkMACD(item.getSymbol() + "_usdt", item.getType(), Constant.EXCHANGE_OKEX);
        }
        logger.info("检查完毕！");
    }

    private void checkMACD(String symbol, String type, String exchange) {
        logger.info("检查，交易所：{}，币种：{}，时间：{}", exchange, symbol, type);

        List<KLineItem> kLineItemList = coinService.queryKLine(symbol, type, exchange);
        List<MACDItem> macdItemList = coinService.macd(kLineItemList, 5);

        if (mailRecord.contains(macdItemList.get(0).toString())) {
            logger.info("已发送过邮件，直接返回");
            return;
        }

        for (MACDItem macd : macdItemList) {
            logger.info("结果:{}", macd.toString());
        }

        checkCross(symbol, type, exchange, macdItemList);
        checkTendency(symbol, type, exchange, macdItemList);
    }

    private void checkTendency(String symbol, String type, String exchange, List<MACDItem> macdList) {
        logger.info("开始检查DIF趋势，交易所：{}，币种：{}，间隔：{}", exchange, symbol, type);

        List<Double> difList = macdList.stream().map(MACDItem::getDif).collect(Collectors.toList());

        int size = difList.size();

        String sign = "";

        if (difList.get(size - 1) > difList.get(size - 2)
                && difList.get(size - 2) > difList.get(size - 3)
                && difList.get(size - 3) < difList.get(size - 4)) {
            sign = "转折向上，请注意";
        }
        if (difList.get(size - 1) < difList.get(size - 2)
                && difList.get(size - 2) < difList.get(size - 3)
                && difList.get(size - 3) > difList.get(size - 4)) {
            sign = "转折向下，请注意";
        }

        if (!StringUtils.isEmpty(sign)) {
            sendMail(sign, exchange, symbol, type);
            addRecord(macdList.get(0).toString());
        }
        logger.info("DIF趋势检查完毕");

    }

    private void checkCross(String symbol, String type, String exchange, List<MACDItem> macdList) {
        logger.info("开始检查交叉线，交易所：{}，币种：{}，间隔：{}", exchange, symbol, type);

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

        String sign = "";

        if (lowBefore && !lowNow) {
            sign = "呈现金叉";
        }

        if (!lowBefore && lowNow) {
            sign = "呈现死叉";
        }
        if (!StringUtils.isEmpty(sign)) {
            sendMail(sign, exchange, symbol, type);
            addRecord(macdList.get(0).toString());
        }
        logger.info("交叉线检查完毕");
    }

    private void sendMail(String sign, String exchange, String symbol, String type) {
        logger.info("呈现趋势：{}，发送邮件", sign);

        String subject = "交易所" + exchange + "，币种" + symbol + sign;
        String text = "交易所：" + exchange + "\n" +
                "币种：" + symbol + "\n" +
                "时间线：" + type + "\n" +
                "信号：" + sign;

        MailUtil.sendMail(subject, text);
    }
    private void addRecord(String record) {
        if (!mailRecord.contains(record)) {
            mailRecord.add(record);
        }
    }
}
