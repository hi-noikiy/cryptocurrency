package com.chen.cryptocurrency.service;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.bean.*;
import com.chen.cryptocurrency.service.task.MacdCheckTasks;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.FileUtil;
import com.chen.cryptocurrency.util.IndexUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CoinService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private ExchangeRemote exchangeRemote;

    public List<KLineItem> queryKLine(String symbol, String type, String exchange) {
        return exchangeRemote.kLine(symbol, type, exchange);
    }

    public List<MACDItem> macd(List<KLineItem> kLineItemList, Integer n) {
        List<Double> list =
                kLineItemList.stream().map(item -> Double.valueOf(item.getCloseValue())).collect(Collectors.toList());

        List<MACDItem> result = Lists.newArrayList();
        for (int i = n; i >= 0; i--) {
            List<Double> temp = list.subList(0, list.size() - i);
            result.add(IndexUtil.culMACD(temp, 12, 26, 9));
        }
        return result;
    }

    public List<TaskItem> listTask() {
        return MacdCheckTasks.taskItems;
    }

    public void addTask(String symbol, String type) {
        TaskItem taskItem = new TaskItem(symbol, type);
        MacdCheckTasks.taskItems.add(taskItem);
    }

    public void delTask(String symbol, String type) {
        for (TaskItem taskItem :
                MacdCheckTasks.taskItems) {
            if (taskItem.getSymbol().equalsIgnoreCase(symbol)
                    && taskItem.getType().equalsIgnoreCase(type)) {
                MacdCheckTasks.taskItems.remove(taskItem);
            }
        }
    }

    public void csvSync() {
        List<KLineItem> btcLine = queryKLine(Coin.BTC.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.btc_file_name, btcLine);
        logger.info("csv sync , btc last line :");
        logger.info("last 2:{}",btcLine.get(btcLine.size()-2));
        logger.info("last 1:{}",btcLine.get(btcLine.size()-1));

        List<KLineItem> eosLine = queryKLine(Coin.EOS.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.eos_file_name, eosLine);
        logger.info("csv sync , eos last line :");
        logger.info("last 2:{}",eosLine.get(eosLine.size()-2));
        logger.info("last 1:{}",eosLine.get(eosLine.size()-1));

        List<KLineItem> neoLine = queryKLine(Coin.NEO.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.neo_file_name, neoLine);
        logger.info("csv sync , neo last line :");
        logger.info("last 2:{}",neoLine.get(neoLine.size()-2));
        logger.info("last 1:{}",neoLine.get(neoLine.size()-1));
    }

    public void trade(String symbol, String type, String price, String amount) {
        exchangeRemote.trade(symbol, type, price, amount);
    }
}
