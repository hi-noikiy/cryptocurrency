package com.chen.cryptocurrency.service;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.bean.*;
import com.chen.cryptocurrency.service.task.MacdSchedule;
import com.chen.cryptocurrency.util.BotUtil;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.FileUtil;
import com.chen.cryptocurrency.util.IndexUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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
    public static Map<Coin, Integer> bestCoinRange = Maps.newHashMap();

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
        return MacdSchedule.taskItems;
    }

    public void addTask(String symbol, String type) {
        TaskItem taskItem = new TaskItem(symbol, type);
        MacdSchedule.taskItems.add(taskItem);
    }

    public void delTask(String symbol, String type) {
        for (TaskItem taskItem : MacdSchedule.taskItems) {
            if (taskItem.getSymbol().equalsIgnoreCase(symbol)
                    && taskItem.getType().equalsIgnoreCase(type)) {
                MacdSchedule.taskItems.remove(taskItem);
            }
        }
    }

    public void csvSync() {
        List<KLineItem> btcLine = queryKLine(Coin.BTC.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.BTC_FILE_NAME, btcLine);
        logger.info("csv sync , btc last line :");
        logger.info("last 2:{}", btcLine.get(btcLine.size() - 2));
        logger.info("last 1:{}", btcLine.get(btcLine.size() - 1));

        List<KLineItem> eosLine = queryKLine(Coin.EOS.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.EOS_FILE_NAME, eosLine);
        logger.info("csv sync , eos last line :");
        logger.info("last 2:{}", eosLine.get(eosLine.size() - 2));
        logger.info("last 1:{}", eosLine.get(eosLine.size() - 1));

        List<KLineItem> neoLine = queryKLine(Coin.NEO.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.NEO_FILE_NAME, neoLine);
        logger.info("csv sync , neo last line :");
        logger.info("last 2:{}", neoLine.get(neoLine.size() - 2));
        logger.info("last 1:{}", neoLine.get(neoLine.size() - 1));
    }

    public void trade(String symbol, String type, String price, String amount) {
        exchangeRemote.trade(symbol, type, price, amount);
    }

    public Integer checkRange(Coin coin) {
        logger.info("check range, coin:{}", coin.getSymbol());

        TimeSeries series = BotUtil.loadCSV(Coin.getFileName(coin));

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);

        List<Decimal> amountList = Lists.newArrayList();

        int baseInt = 6;
        for (int i = 0; i < 75; i++) {
            int longInt = baseInt + i;
            SMAIndicator longSma = new SMAIndicator(closePrice, longInt);

            Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma);
            Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma);

            // Running our juicy trading strategy...
            TimeSeriesManager seriesManager = new TimeSeriesManager(series);
            TradingRecord tradingRecord = seriesManager.run(new BaseStrategy(buyingRule, sellingRule));

            Decimal amount = Decimal.valueOf(10000);
            Decimal count;

            for (Trade trade : tradingRecord.getTrades()) {
                int entryIndex = trade.getEntry().getIndex();
                int exitIndex = trade.getExit().getIndex();

                count = amount.multipliedBy(0.9985).dividedBy(series.getBar(entryIndex).getClosePrice().multipliedBy(1.003));
                amount = count.multipliedBy(series.getBar(exitIndex).getClosePrice().multipliedBy(0.997)).multipliedBy(0.998);
            }
            logger.info("longInt : " + longInt);
            logger.info("amount : " + amount);
            amountList.add(amount);
        }

        int highCount = 0;
        Decimal highAmount = Decimal.valueOf(0);

        for (int i = 1; i < 74; i++) {
            Decimal thisAmount = amountList.get(i - 1).
                    plus(amountList.get(i))
                    .plus(amountList.get(i + 1));
            if (thisAmount.isGreaterThan(highAmount)) {
                highCount = i + baseInt;
                highAmount = thisAmount;
            }
        }

        logger.info("highAmount : " + highAmount.dividedBy(3));
        logger.info("highCount : " + highCount);

        return highCount;
    }
}
