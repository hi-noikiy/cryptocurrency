package com.chen.cryptocurrency.service;

import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.MACDItem;
import com.chen.cryptocurrency.service.bean.TaskItem;
import com.chen.cryptocurrency.service.cache.KLineCache;
import com.chen.cryptocurrency.service.task.ScheduledTasks;
import com.chen.cryptocurrency.util.MACDUtil;
import com.chen.cryptocurrency.util.MailUtil;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CoinService {
    @Resource
    private KLineCache kLineCache;

    public List<KLineItem> queryKLine(String symbol, String type) {
        if (StringUtils.isEmpty(symbol)) {
            symbol = "btc_usdt";
        }
        if (StringUtils.isEmpty(type)) {
            type = "2hour";
        }
        return kLineCache.get(symbol, type);
    }

    public List<MACDItem> macd(List<KLineItem> kLineItemList, Integer n) {
        List<Double> list =
                kLineItemList.stream().map(item -> Double.valueOf(item.getCloseValue())).collect(Collectors.toList());

        List<MACDItem> result = Lists.newArrayList();
        for (int i = n; i >= 0; i--) {
            List<Double> temp = list.subList(0, list.size() - i);
            result.add(MACDUtil.getMACD(temp, 12, 26, 9));
        }
        return result;
    }

    public List<TaskItem> listTask() {
        return ScheduledTasks.taskItems;
    }

    public void addTask(String symbol, String type) {
        TaskItem taskItem = new TaskItem(symbol, type);
        ScheduledTasks.taskItems.add(taskItem);
    }

    public void delTask(String symbol, String type) {
        for (TaskItem taskItem :
                ScheduledTasks.taskItems) {
            if (taskItem.getSymbol().equalsIgnoreCase(symbol)
                    && taskItem.getType().equalsIgnoreCase(type)) {
                ScheduledTasks.taskItems.remove(taskItem);
            }
        }
    }

    public void mailTest() {
        String symbol = "btc_usdt";
        String type = "1hour";
        String sellSign = "呈现金叉";

        String subject = "币种" + symbol + sellSign;

        String text = "币种：" + symbol + "\n" +
                "时间线：" + type + "\n" +
                "信号：" + sellSign;

        MailUtil.sendMail(subject, text);
    }
}
