package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.CheckResult;
import com.chen.cryptocurrency.service.bean.Coin;
import com.chen.cryptocurrency.util.BotUtil;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.MailUtil;
import com.chen.cryptocurrency.util.SMSUtil;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.Decimal;
import org.ta4j.core.Trade;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CoinSchedule {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ExchangeRemote exchangeRemote;
    @Resource
    private CoinService coinService;

    @Scheduled(cron = "0 0/30 * * * ? ")
    public void syncStatusTask() {
        logger.info("sync account status, begin !");

        exchangeRemote.syncStatus();
    }

    @Scheduled(cron = "30 1 0/1 * * ?")
    public void writeTask() {
        logger.info("write csv task, begin !");
        try {
            coinService.csvSync();
        } catch (Exception e) {
            try {
                TimeUnit.SECONDS.sleep(10);
                coinService.csvSync();
            } catch (Exception finalE) {
                logger.error("write csv task error!!!", finalE);
                MailUtil.sendMail("同步数据出错", "从交易所读取数据出错，请检查网络！");
                SMSUtil.sendError();
            }
        }
    }

    @Scheduled(cron = "0 30 0/6 * * ?")
    public void checkBestRange() {
        logger.info("check best range !");

        if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 0) {
            Integer btcBestRange = coinService.checkRange(Coin.BTC);
            CoinService.bestCoinRange.put(Coin.BTC, btcBestRange);
        }
        if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 0) {
            Integer eosBestRange = coinService.checkRange(Coin.EOS);
            CoinService.bestCoinRange.put(Coin.EOS, eosBestRange);

        }
        if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 0) {
            Integer neoBestRange = coinService.checkRange(Coin.NEO);
            CoinService.bestCoinRange.put(Coin.NEO, neoBestRange);
        }
    }

    private Map<Coin, Decimal> tradeRecord = Maps.newHashMap();

    @Scheduled(cron = "0 2 0/1 * * ? ")
    public void checkBuySell() {
        logger.info("check buy/sell task begin !");

        coinService.checkCSV();

        CheckResult btcCheckResult = BotUtil.check(Coin.getFileName(Coin.BTC), coinService.rangeGet(Coin.BTC));
        CheckResult eosCheckResult = BotUtil.check(Coin.getFileName(Coin.EOS), coinService.rangeGet(Coin.EOS));
        CheckResult neoCheckResult = BotUtil.check(Coin.getFileName(Coin.NEO), coinService.rangeGet(Coin.NEO));

        Trade btcTrade = BotUtil.current(Constant.BTC_FILE_NAME, coinService.rangeGet(Coin.BTC));
        Trade eosTrade = BotUtil.current(Constant.EOS_FILE_NAME, coinService.rangeGet(Coin.EOS));
        Trade neoTrade = BotUtil.current(Constant.NEO_FILE_NAME, coinService.rangeGet(Coin.NEO));

        double cashTotal = Double.valueOf(exchangeRemote.getTradeAmount("usdt"));
        int cashPiece = 3 - ExchangeRemote.TRADE_STATUS.buyTotal();

        // 有未持有项，检查是否buy
        if (ExchangeRemote.TRADE_STATUS.buyTotal() < 3) {
            double cash = (cashTotal - 1) / cashPiece;

            if (btcCheckResult.shouldBuy() || btcTrade.getEntry() != null) {
                logger.info("symbol:{},price:{},amount:{}", Coin.BTC.getSymbol() + "_usdt", btcCheckResult.getPrice(), cash);
                if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 0) {
                    MailUtil.sendMail("Should buy BTC!", "price : " + btcCheckResult.getPrice());
                    SMSUtil.sendNotify("Buy BTC", String.valueOf(btcCheckResult.getPrice()));
                    exchangeRemote.buyMarket(Coin.BTC.getSymbol() + "_usdt", String.valueOf(cash));
                    tradeRecord.put(Coin.BTC, btcCheckResult.getPrice());
                }
            }
            if (eosCheckResult.shouldBuy() || eosTrade.getEntry() != null) {
                logger.info("symbol:{},price:{},amount:{}", Coin.EOS.getSymbol() + "_usdt", eosCheckResult.getPrice(), cash);
                if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 0) {
                    MailUtil.sendMail("Should buy EOS!", "price : " + eosCheckResult.getPrice());
                    SMSUtil.sendNotify("Buy EOS", String.valueOf(eosCheckResult.getPrice()));
                    exchangeRemote.buyMarket(Coin.EOS.getSymbol() + "_usdt", String.valueOf(cash));
                    tradeRecord.put(Coin.EOS, eosCheckResult.getPrice());
                }
            }
            if (neoCheckResult.shouldBuy() || neoTrade.getEntry() != null) {
                logger.info("symbol:{},price:{},amount:{}", Coin.NEO.getSymbol() + "_usdt", neoCheckResult.getPrice(), cash);
                if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 0) {
                    MailUtil.sendMail("Should buy NEO!", "price : " + neoCheckResult.getPrice());
                    SMSUtil.sendNotify("Buy NEO", String.valueOf(neoCheckResult.getPrice()));
                    exchangeRemote.buyMarket(Coin.NEO.getSymbol() + "_usdt", String.valueOf(cash));
                    tradeRecord.put(Coin.NEO, neoCheckResult.getPrice());
                }
            }
        }


        // 已经持有，检查是否sell
        if (ExchangeRemote.TRADE_STATUS.buyTotal() > 0) {
            if (btcCheckResult.shouldSell()
                    || btcTrade.getExit() != null
                    || (btcTrade.getEntry() == null && btcTrade.getExit() == null)) {
                logger.info("symbol:{},price:{},amount:{}", Coin.BTC.getSymbol() + "_usdt", btcCheckResult.getPrice(), exchangeRemote.getTradeAmount("btc"));
                if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 1) {
                    MailUtil.sendMail("Should sell BTC!", "buy:" + tradeRecord.get(Coin.BTC) + ",sell:" + btcCheckResult.getPrice());
                    SMSUtil.sendNotify("Sell BTC", String.valueOf(btcCheckResult.getPrice()));
                    exchangeRemote.sellMarket(Coin.BTC.getSymbol() + "_usdt", exchangeRemote.getTradeAmount("btc"));
                    tradeRecord.remove(Coin.BTC);
                }
            }
            if (eosCheckResult.shouldSell()
                    || eosTrade.getExit() != null
                    || (eosTrade.getEntry() == null && eosTrade.getExit() == null)) {
                logger.info("symbol:{},price:{},amount:{}", Coin.EOS.getSymbol() + "_usdt", eosCheckResult.getPrice(), exchangeRemote.getTradeAmount("eos"));
                if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 1) {
                    MailUtil.sendMail("Should sell EOS!", "buy:" + tradeRecord.get(Coin.EOS) + ",sell:" + eosCheckResult.getPrice());
                    SMSUtil.sendNotify("Sell EOS", String.valueOf(eosCheckResult.getPrice()));
                    exchangeRemote.sellMarket(Coin.EOS.getSymbol() + "_usdt", exchangeRemote.getTradeAmount("eos"));
                    tradeRecord.remove(Coin.EOS);
                }
            }
            if (neoCheckResult.shouldSell()
                    || neoTrade.getExit() != null
                    || (neoTrade.getEntry() == null && neoTrade.getExit() == null)) {
                logger.info("symbol:{},price:{},amount:{}", Coin.NEO.getSymbol() + "_usdt", neoCheckResult.getPrice(), exchangeRemote.getTradeAmount("neo"));
                if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 1) {
                    MailUtil.sendMail("Should sell NEO!", "buy:" + tradeRecord.get(Coin.NEO) + ",sell:" + neoCheckResult.getPrice());
                    SMSUtil.sendNotify("Sell NEO", String.valueOf(neoCheckResult.getPrice()));
                    exchangeRemote.sellMarket(Coin.NEO.getSymbol() + "_usdt", exchangeRemote.getTradeAmount("neo"));
                    tradeRecord.remove(Coin.NEO);
                }
            }
        }
    }
}
