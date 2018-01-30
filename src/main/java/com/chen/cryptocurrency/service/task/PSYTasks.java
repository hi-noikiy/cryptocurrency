package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.Coin;
import com.chen.cryptocurrency.service.bean.Exchange;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.TaskItem;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.IndexUtil;
import com.chen.cryptocurrency.util.MailUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class PSYTasks implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CoinService coinService;
    private static List<TaskItem> taskItems = Lists.newArrayList();

    private static Set<String> mailRecord = Sets.newConcurrentHashSet();

    @Override
    public void afterPropertiesSet() throws Exception {
        taskItems.add(new TaskItem(Coin.BTC.getSymbol(), "12hour"));
        taskItems.add(new TaskItem(Coin.ETH.getSymbol(), "12hour"));
        taskItems.add(new TaskItem(Coin.EOS.getSymbol(), "12hour"));
        taskItems.add(new TaskItem(Coin.NEO.getSymbol(), "12hour"));
        taskItems.add(new TaskItem(Coin.QTUM.getSymbol(), "12hour"));
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void reportCurrentTime() {
        if (mailRecord.size() > 100) {
            mailRecord.clear();
        }
        logger.info("开始执行检查！");

        for (TaskItem item : taskItems) {
            checkPSY(item.getSymbol() + "_usd", item.getType(), Exchange.OKCOIN.name());
            checkPSY(item.getSymbol() + "_usdt", item.getType(), Exchange.OKEX.name());
        }
        logger.info("检查完毕！");
    }

    private void checkPSY(String symbol, String type, String exchange) {
        logger.info("检查PSY，交易所：{}，币种：{}，时间：{}", exchange, symbol, type);

        List<KLineItem> kLineItemList;
        try {
            kLineItemList = coinService.queryKLine(symbol, type, exchange);
        } catch (Exception e) {
            logger.error("查询出错，symbol:{}，type:{}，exchange:{}", symbol, type, exchange);
            return;
        }
        double psy = IndexUtil.culPSY(kLineItemList);

        String recordKey = Constant.key_joiner.join(symbol, type, exchange, LocalDate.now().toString(), psy);

        if (!mailRecord.contains(recordKey)) {
            double buySign = 0.4;
            String sign;

            if (psy < buySign) {
                sign = "PSY 很低（" + psy + "），请注意买入！";
            } else {
                sign = "PSY 正常（" + psy + "），请结合其他指标判断。";
            }

            sendMail(sign, exchange, symbol, type);
            mailRecord.add(recordKey);
        } else {
            logger.info("PSY 无变化。");
        }

        logger.info("PSY 检查完毕");
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


    public static void main(String[] args) {
        LocalDate date = LocalDate.now().minusDays(1);
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < 36; i++) {
            list.add("\"" + date.toString() + "\"");
            date = date.minusDays(1);
        }
        System.out.println(list);
    }
}
