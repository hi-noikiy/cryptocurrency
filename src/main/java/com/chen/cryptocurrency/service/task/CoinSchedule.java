package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.CheckResult;
import com.chen.cryptocurrency.service.bean.Coin;
import com.chen.cryptocurrency.util.BotUtil;
import com.chen.cryptocurrency.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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

    @Scheduled(cron = "30 1 0/2 * * ?")
    public void writeTask() {
        logger.info("write csv task, begin !");
        try {
            coinService.csvSync();
        } catch (Exception e) {
            try{
                coinService.csvSync();
            }catch (Exception finalE){
                logger.error("write csv task error!!!",finalE);
                MailUtil.sendMail("同步数据出错","从交易所读取数据出错，请检查网络！");
            }
        }
    }

    @Scheduled(cron = "0 30 0/12 * * ?")
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

    @Scheduled(cron = "0 2 0/2 * * ? ")
    public void checkBuySell() {
        logger.info("check buy/sell task begin !");

        coinService.checkCSV();

        CheckResult btcCheckResult = BotUtil.check(Coin.getFileName(Coin.BTC), coinService.rangeGet(Coin.BTC));
        CheckResult eosCheckResult = BotUtil.check(Coin.getFileName(Coin.EOS), coinService.rangeGet(Coin.EOS));
        CheckResult neoCheckResult = BotUtil.check(Coin.getFileName(Coin.NEO), coinService.rangeGet(Coin.NEO));

        double cashTotal = Double.valueOf(exchangeRemote.getTradeAmount("usdt"));
        int cashPiece = 3 - ExchangeRemote.TRADE_STATUS.buyTotal();

        // 有未持有项，检查是否buy
        if (ExchangeRemote.TRADE_STATUS.buyTotal() < 3) {
            double cash = (cashTotal - 1) / cashPiece;

            if (btcCheckResult.shouldBuy()) {
                logger.info("check result : Should buy BTC!");
                logger.info("symbol:{},price:{},amount:{}", Coin.BTC.getSymbol() + "_usdt", btcCheckResult.getPrice(), cash);
                MailUtil.sendMail("Should buy BTC!price : " + btcCheckResult.getPrice(), "look up");
                if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 0) {
                    exchangeRemote.buyMarket(Coin.BTC.getSymbol() + "_usdt", String.valueOf(cash));
                }
            }
            if (eosCheckResult.shouldBuy()) {
                logger.info("check result : Should buy EOS!");
                logger.info("symbol:{},price:{},amount:{}", Coin.EOS.getSymbol() + "_usdt", eosCheckResult.getPrice(), cash);
                MailUtil.sendMail("Should buy EOS!price : " + eosCheckResult.getPrice(), "look up");
                if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 0) {
                    exchangeRemote.buyMarket(Coin.EOS.getSymbol() + "_usdt", String.valueOf(cash));
                }
            }
            if (neoCheckResult.shouldBuy()) {
                logger.info("check result : Should buy NEO!");
                logger.info("symbol:{},price:{},amount:{}", Coin.NEO.getSymbol() + "_usdt", neoCheckResult.getPrice(), cash);
                MailUtil.sendMail("Should buy NEO!price : " + neoCheckResult.getPrice(), "look up");
                if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 0) {
                    exchangeRemote.buyMarket(Coin.NEO.getSymbol() + "_usdt", String.valueOf(cash));
                }
            }
        }


        // 已经持有，检查是否sell
        if (ExchangeRemote.TRADE_STATUS.buyTotal() > 0) {
            if (btcCheckResult.shouldSell()) {
                logger.info("check result : Should sell BTC!");
                logger.info("symbol:{},price:{},amount:{}", Coin.BTC.getSymbol() + "_usdt", btcCheckResult.getPrice(), exchangeRemote.getTradeAmount("btc"));
                MailUtil.sendMail("Should sell BTC!price : " + btcCheckResult.getPrice(), "look up!");
                if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 1) {
                    exchangeRemote.sellMarket(Coin.BTC.getSymbol() + "_usdt", exchangeRemote.getTradeAmount("btc"));
                }
            }
            if (eosCheckResult.shouldSell()) {
                logger.info("check result : Should sell EOS!");
                logger.info("symbol:{},price:{},amount:{}", Coin.EOS.getSymbol() + "_usdt", eosCheckResult.getPrice(), exchangeRemote.getTradeAmount("eos"));
                MailUtil.sendMail("Should sell EOS!price : " + eosCheckResult.getPrice(), "look up!");

                if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 1) {
                    exchangeRemote.sellMarket(Coin.EOS.getSymbol() + "_usdt", exchangeRemote.getTradeAmount("eos"));
                }
            }
            if (neoCheckResult.shouldSell()) {
                logger.info("check result : Should sell NEO!");
                logger.info("symbol:{},price:{},amount:{}", Coin.NEO.getSymbol() + "_usdt", neoCheckResult.getPrice(), exchangeRemote.getTradeAmount("neo"));
                MailUtil.sendMail("Should sell NEO!price : " + neoCheckResult.getPrice(), "look up!");

                if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 1) {
                    exchangeRemote.sellMarket(Coin.NEO.getSymbol() + "_usdt", exchangeRemote.getTradeAmount("neo"));
                }
            }
        }
    }
}
