package com.chen.cryptocurrency.service;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.bean.Coin;
import com.chen.cryptocurrency.service.bean.Exchange;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.util.BotUtil;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.FileUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CoinService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private ExchangeRemote exchangeRemote;
    public static final Map<Coin, Integer> BEST_COIN_RANGE = Maps.newHashMap();

    public Integer rangeGet(Coin coin) {
        return BEST_COIN_RANGE.computeIfAbsent(coin, this::checkRange);
    }

    public List<KLineItem> queryKLine(String symbol, String type, String exchange) {
        return exchangeRemote.kLine(symbol, type, exchange);
    }

    public void csvSync() {
        List<KLineItem> btcLine = queryKLine(Coin.BTC.getTradeSymbol(), "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.BTC_FILE_NAME, btcLine);
        logger.info("csv sync , btc last line :");
        logger.info("last 2:{}", btcLine.get(btcLine.size() - 2));
        logger.info("last 1:{}", btcLine.get(btcLine.size() - 1));

        List<KLineItem> eosLine = queryKLine(Coin.EOS.getTradeSymbol(), "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.EOS_FILE_NAME, eosLine);
        logger.info("csv sync , eos last line :");
        logger.info("last 2:{}", eosLine.get(eosLine.size() - 2));
        logger.info("last 1:{}", eosLine.get(eosLine.size() - 1));

        List<KLineItem> neoLine = queryKLine(Coin.NEO.getTradeSymbol(), "2hour", Exchange.OKEX.name());
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

    public void checkCSV() {
        File btcFile = new File(Constant.BTC_FILE_NAME);
        File eosFile = new File(Constant.EOS_FILE_NAME);
        File neoFile = new File(Constant.NEO_FILE_NAME);

        try {
            List<String> btcLines = Files.readLines(btcFile, StandardCharsets.UTF_8);
            List<String> eosLines = Files.readLines(eosFile, StandardCharsets.UTF_8);
            List<String> neoLines = Files.readLines(neoFile, StandardCharsets.UTF_8);

            logger.info("Last line of BTC :{}", btcLines.get(btcLines.size() - 1));
            logger.info("Last line of EOS :{}", eosLines.get(eosLines.size() - 1));
            logger.info("Last line of NEO :{}", neoLines.get(neoLines.size() - 1));

        } catch (IOException e) {
            logger.error("check CSV error.", e);
        }
    }
}
