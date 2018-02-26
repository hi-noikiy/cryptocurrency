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
import org.ta4j.core.Decimal;

import javax.annotation.Resource;
import java.text.DecimalFormat;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class BotTasks {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ExchangeRemote exchangeRemote;
    @Resource
    private CoinService coinService;

    @Scheduled(cron = "0 1 0/2 * * ? ")
    public void checkTask() {
        logger.info("check task begin !");

        CheckResult btcCheckResult = BotUtil.check("btc.csv", 10);
        CheckResult eosCheckResult = BotUtil.check("eos.csv", 16);
        CheckResult neoCheckResult = BotUtil.check("neo.csv", 16);

        double cashTotal = Double.valueOf(exchangeRemote.getTradeAmount("usdt"));

        DecimalFormat decimalFormat = new DecimalFormat("#####.####");
        if (cashTotal > 10) {
            int cashPiece = 3 - ExchangeRemote.TRADE_STATUS.buyTotal();
            double cash = (cashTotal - 1) / cashPiece;

            if (btcCheckResult.shouldBuy()) {
                logger.info("check result : Should buy BTC!");
                logger.info("symbol:{},price:{},amount:{}", Coin.BTC.getSymbol() + "_usdt", btcCheckResult.getPrice(), decimalFormat.format(cash / btcCheckResult.getPrice().doubleValue()));
                MailUtil.sendMail("Should buy BTC!price : " + btcCheckResult.getPrice(), "look up");
                if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 0) {
                    exchangeRemote.trade(Coin.BTC.getSymbol() + "_usdt", "buy_market", String.valueOf(cash), decimalFormat.format(cash / btcCheckResult.getPrice().doubleValue()));
                }
            }
            if (eosCheckResult.shouldBuy()) {
                logger.info("check result : Should buy EOS!");
                logger.info("symbol:{},price:{},amount:{}", Coin.EOS.getSymbol() + "_usdt", eosCheckResult.getPrice(), cash);
                MailUtil.sendMail("Should buy EOS!price : " + eosCheckResult.getPrice(), "look up");
                if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 0) {
                    exchangeRemote.trade(Coin.EOS.getSymbol() + "_usdt", "buy_market", String.valueOf(cash), decimalFormat.format(cash/eosCheckResult.getPrice().doubleValue()));
                }
            }
            if (neoCheckResult.shouldBuy()) {
                logger.info("check result : Should buy NEO!");
                logger.info("symbol:{},price:{},amount:{}", Coin.NEO.getSymbol() + "_usdt", neoCheckResult.getPrice(), cash);
                MailUtil.sendMail("Should buy NEO!price : " + neoCheckResult.getPrice(), "look up");
                if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 0) {
                    exchangeRemote.trade(Coin.NEO.getSymbol() + "_usdt", "buy_market", String.valueOf(cash), decimalFormat.format(cash/neoCheckResult.getPrice().doubleValue()));
                }
            }
        } else {
            if (btcCheckResult.shouldSell()) {
                logger.info("check result : Should sell BTC!");
                logger.info("symbol:{},price:{},amount:{}", Coin.BTC.getSymbol() + "_usdt", btcCheckResult.getPrice(), exchangeRemote.getTradeAmount("btc"));
                MailUtil.sendMail("Should sell BTC!price : " + btcCheckResult.getPrice(), "look up!");
                if (ExchangeRemote.TRADE_STATUS.getBtcStatus() == 1) {
                    exchangeRemote.trade(Coin.BTC.getSymbol() + "_usdt", "sell_market", btcCheckResult.getPrice().toString(), exchangeRemote.getTradeAmount("btc"));
                }
            }
            if (eosCheckResult.shouldSell()) {
                logger.info("check result : Should sell EOS!");
                logger.info("symbol:{},price:{},amount:{}", Coin.EOS.getSymbol() + "_usdt", eosCheckResult.getPrice(), exchangeRemote.getTradeAmount("eos"));
                MailUtil.sendMail("Should sell EOS!price : " + eosCheckResult.getPrice(), "look up!");

                if (ExchangeRemote.TRADE_STATUS.getEosStatus() == 1) {
                    exchangeRemote.trade(Coin.EOS.getSymbol() + "_usdt", "sell_market", eosCheckResult.getPrice().toString(), exchangeRemote.getTradeAmount("eos"));
                }
            }
            if (neoCheckResult.shouldSell()) {
                logger.info("check result : Should sell NEO!");
                logger.info("symbol:{},price:{},amount:{}", Coin.NEO.getSymbol() + "_usdt", neoCheckResult.getPrice(), exchangeRemote.getTradeAmount("neo"));
                MailUtil.sendMail("Should sell NEO!price : " + neoCheckResult.getPrice(), "look up!");

                if (ExchangeRemote.TRADE_STATUS.getNeoStatus() == 1) {
                    exchangeRemote.trade(Coin.NEO.getSymbol() + "_usdt", "sell_market", neoCheckResult.getPrice().toString(), exchangeRemote.getTradeAmount("neo"));
                }
            }
        }
    }

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void syncStatusTask() {
        logger.info("sync task begin !");

        exchangeRemote.syncStatus();
    }
}
