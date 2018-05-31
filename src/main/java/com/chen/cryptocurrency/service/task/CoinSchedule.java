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

    private static boolean btcCheck = true;
    private static boolean eosCheck = true;
    private static boolean neoCheck = true;

    @Resource
    private ExchangeRemote exchangeRemote;
    @Resource
    private CoinService coinService;

    public static void switchCheck(Coin coin, boolean v) {
        switch (coin) {
            case BTC:
                btcCheck = v;
                break;
            case EOS:
                eosCheck = v;
                break;
            case NEO:
                neoCheck = v;
                break;
            default:
                break;
        }
    }

    @Scheduled(cron = "0 0/10 * * * ? ")
    public void syncStatusTask() {
        logger.info("sync account status, begin !");

        exchangeRemote.syncStatus();
    }

    @Scheduled(cron = "45 0/30 * * * ? ")
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
            CoinService.BEST_COIN_RANGE.put(Coin.BTC, btcBestRange);
        }
        if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 0) {
            Integer eosBestRange = coinService.checkRange(Coin.EOS);
            CoinService.BEST_COIN_RANGE.put(Coin.EOS, eosBestRange);

        }
        if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 0) {
            Integer neoBestRange = coinService.checkRange(Coin.NEO);
            CoinService.BEST_COIN_RANGE.put(Coin.NEO, neoBestRange);
        }
    }

    private Map<Coin, Decimal> tradeRecord = Maps.newHashMap();
    private volatile boolean sync = false;

    @Scheduled(cron = "0 1 0/2 * * ? ")
    public void checkBuySell() {
        logger.info("check buy/sell task begin !");
        int retryCount = 0;
        final int maxCount = 100;
        while (!sync && retryCount < maxCount) {
            retryCount++;
            try {
                coinService.csvSync();
                sync = true;
            } catch (Exception e) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ie) {
                    logger.error("check buy sell error.", ie);
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!sync) {
            logger.error("Write csv before buy sell error");
            MailUtil.sendMail("同步数据出错", "从交易所读取数据出错，请检查网络！");
            SMSUtil.sendError();
            return;
        }

        coinService.checkCSV();

        CheckResult btcCheckResult = BotUtil.check(Coin.getFileName(Coin.BTC), coinService.rangeGet(Coin.BTC));
        CheckResult eosCheckResult = BotUtil.check(Coin.getFileName(Coin.EOS), coinService.rangeGet(Coin.EOS));
        CheckResult neoCheckResult = BotUtil.check(Coin.getFileName(Coin.NEO), coinService.rangeGet(Coin.NEO));

        Trade btcTrade = BotUtil.current(Constant.BTC_FILE_NAME, coinService.rangeGet(Coin.BTC));
        Trade eosTrade = BotUtil.current(Constant.EOS_FILE_NAME, coinService.rangeGet(Coin.EOS));
        Trade neoTrade = BotUtil.current(Constant.NEO_FILE_NAME, coinService.rangeGet(Coin.NEO));

        double cashTotal = Double.parseDouble(exchangeRemote.getTradeAmount("usdt"));
        int cashPiece = 3 - ExchangeRemote.TRADE_STATUS.buyTotal();

        String loggerTemplate = "symbol:{},price:{},amount:{}";
        // 有未持有项，检查是否buy
        if (ExchangeRemote.TRADE_STATUS.buyTotal() < 3) {
            double cash = (cashTotal - 1) / cashPiece;

            if (btcCheck) {
                if (btcCheckResult.shouldBuy() || btcTrade.getEntry() != null) {
                    logger.info(loggerTemplate, Coin.BTC.getTradeSymbol(), btcCheckResult.getPrice(), cash);
                    if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 0) {
                        MailUtil.sendMail("Should buy BTC!", "price : " + btcCheckResult.getPrice());
                        SMSUtil.sendNotify("Buy BTC", String.valueOf(btcCheckResult.getPrice()));
                        exchangeRemote.buyMarket(Coin.BTC.getTradeSymbol(), String.valueOf(cash));
                        tradeRecord.put(Coin.BTC, btcCheckResult.getPrice());
                    }
                }
            }

            if (eosCheck) {
                if (eosCheckResult.shouldBuy() || eosTrade.getEntry() != null) {
                    logger.info(loggerTemplate, Coin.EOS.getTradeSymbol(), eosCheckResult.getPrice(), cash);
                    if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 0) {
                        MailUtil.sendMail("Should buy EOS!", "price : " + eosCheckResult.getPrice());
                        SMSUtil.sendNotify("Buy EOS", String.valueOf(eosCheckResult.getPrice()));
                        exchangeRemote.buyMarket(Coin.EOS.getTradeSymbol(), String.valueOf(cash));
                        tradeRecord.put(Coin.EOS, eosCheckResult.getPrice());
                    }
                }
            }

            if (neoCheck) {
                if (neoCheckResult.shouldBuy() || neoTrade.getEntry() != null) {
                    logger.info(loggerTemplate, Coin.NEO.getTradeSymbol(), neoCheckResult.getPrice(), cash);
                    if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 0) {
                        MailUtil.sendMail("Should buy NEO!", "price : " + neoCheckResult.getPrice());
                        SMSUtil.sendNotify("Buy NEO", String.valueOf(neoCheckResult.getPrice()));
                        exchangeRemote.buyMarket(Coin.NEO.getTradeSymbol(), String.valueOf(cash));
                        tradeRecord.put(Coin.NEO, neoCheckResult.getPrice());
                    }
                }
            }
        }


        // 已经持有，检查是否sell
        if (ExchangeRemote.TRADE_STATUS.buyTotal() > 0) {
            if (btcCheck) {
                if (btcCheckResult.shouldSell()
                        || btcTrade.getExit() != null
                        || (btcTrade.getEntry() == null && btcTrade.getExit() == null)) {
                    logger.info(loggerTemplate, Coin.BTC.getTradeSymbol(), btcCheckResult.getPrice(), exchangeRemote.getTradeAmount("btc"));
                    if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 1) {
                        MailUtil.sendMail("Should sell BTC!", "buy:" + tradeRecord.get(Coin.BTC) + ",sell:" + btcCheckResult.getPrice());
                        SMSUtil.sendNotify("Sell BTC", String.valueOf(tradeRecord.get(Coin.BTC)) + "-" + btcCheckResult.getPrice());
                        exchangeRemote.sellMarket(Coin.BTC.getTradeSymbol(), exchangeRemote.getTradeAmount("btc"));
                        tradeRecord.remove(Coin.BTC);
                    }
                }
            }

            if (eosCheck) {
                if (eosCheckResult.shouldSell()
                        || eosTrade.getExit() != null
                        || (eosTrade.getEntry() == null && eosTrade.getExit() == null)) {
                    logger.info(loggerTemplate, Coin.EOS.getTradeSymbol(), eosCheckResult.getPrice(), exchangeRemote.getTradeAmount("eos"));
                    if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 1) {
                        MailUtil.sendMail("Should sell EOS!", "buy:" + tradeRecord.get(Coin.EOS) + ",sell:" + eosCheckResult.getPrice());
                        SMSUtil.sendNotify("Sell EOS", String.valueOf(tradeRecord.get(Coin.EOS)) + "-" + eosCheckResult.getPrice());
                        exchangeRemote.sellMarket(Coin.EOS.getTradeSymbol(), exchangeRemote.getTradeAmount("eos"));
                        tradeRecord.remove(Coin.EOS);
                    }
                }
            }

            if (neoCheck) {
                if (neoCheckResult.shouldSell()
                        || neoTrade.getExit() != null
                        || (neoTrade.getEntry() == null && neoTrade.getExit() == null)) {
                    logger.info(loggerTemplate, Coin.NEO.getTradeSymbol(), neoCheckResult.getPrice(), exchangeRemote.getTradeAmount("neo"));
                    if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 1) {
                        MailUtil.sendMail("Should sell NEO!", "buy:" + tradeRecord.get(Coin.NEO) + ",sell:" + neoCheckResult.getPrice());
                        SMSUtil.sendNotify("Sell NEO", String.valueOf(tradeRecord.get(Coin.NEO)) + "-" + neoCheckResult.getPrice());
                        exchangeRemote.sellMarket(Coin.NEO.getTradeSymbol(), exchangeRemote.getTradeAmount("neo"));
                        tradeRecord.remove(Coin.NEO);
                    }
                }
            }
        }

        sync = false;
    }
}
