package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.cache.KLineCache;
import com.chen.cryptocurrency.util.MailUtil;
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
    @Resource
    private CoinService coinService;

    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void reportCurrentTime() {
        System.out.println("开始执行检查！");
        checkMACD("btc_usd", "1hour");
        checkMACD("btc_usd", "2hour");
        checkMACD("eth_usd", "1hour");
        checkMACD("eth_usd", "2hour");
        System.out.println("检查完毕！");
    }

    private void checkMACD(String symbol,String type) {
        String subject = "金叉！";
        String text =  "金叉！注意买入！";

        List<KLineItem> list = coinService.queryKLine(symbol, type);
        List<Map<String, Double>> macdList= coinService.macd(list, 5);

        Map<String, Double> macd;
        boolean lowBefore = true;
        boolean highNow = true;

        for (int i = 0; i < 4; i++) {
            macd = macdList.get(i);
            if (macd.get("DIF") > macd.get("DEA")) {
                lowBefore = false;
            }
        }
        macd = macdList.get(4);
        if (macd.get("DIF") < macd.get("DEA")) {
            highNow = false;
        }

        if (lowBefore && highNow) {
            MailUtil.sendMail(symbol +"_"+ type +"_"+ subject, text);
        }
    }
}
