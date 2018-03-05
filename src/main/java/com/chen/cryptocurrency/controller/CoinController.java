package com.chen.cryptocurrency.controller;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.*;
import com.chen.cryptocurrency.util.BotUtil;
import com.chen.cryptocurrency.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.Trade;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */

@RestController
@EnableAutoConfiguration
@RequestMapping("/coin")
public class CoinController {
    @Resource
    private CoinService coinService;
    @Resource
    private ExchangeRemote exchangeRemote;

    @RequestMapping("/trade")
    String trade(@RequestParam String symbol,
                 @RequestParam String type,
                 @RequestParam String price,
                 @RequestParam String amount) {
        coinService.trade(symbol, type, price, amount);
        return "ok";
    }

    @RequestMapping("/current")
    String current() {
        Trade btcTrade = BotUtil.current(Constant.BTC_FILE_NAME, CoinService.bestCoinRange.get(Coin.BTC));
        Trade eosTrade = BotUtil.current(Constant.EOS_FILE_NAME, CoinService.bestCoinRange.get(Coin.EOS));
        Trade neoTrade = BotUtil.current(Constant.NEO_FILE_NAME, CoinService.bestCoinRange.get(Coin.NEO));

        return btcTrade.toString() + "\n"
                + eosTrade.toString() + "\n"
                + neoTrade.toString() + "\n";
    }

    @RequestMapping("/test/check")
    String check() {
        BotUtil.check("btc.csv", 34);
        return "ok";
    }

    @RequestMapping("/test/trade")
    String trade(@RequestParam String price, @RequestParam String amount) {
        coinService.trade("btc_usdt", "sell", price, amount);
        return "ok";
    }

    @RequestMapping("/test/writeCSV")
    String writeCSV() {
        coinService.csvSync();

        return "ok";
    }

    @RequestMapping("/test/checkBS")
    String checkBS() {
        Integer btcBestRange = coinService.checkRange(Coin.BTC);
        Integer eosBestRange = coinService.checkRange(Coin.EOS);
        Integer neoBestRange = coinService.checkRange(Coin.NEO);

        return "btc:" + btcBestRange
                + "eos:" + eosBestRange
                + "neo:" + neoBestRange;
    }

    @RequestMapping("/test/tradeMarket")
    String tradeMarket(@RequestParam() String symbol,
                       @RequestParam(required = false) String price,
                       @RequestParam(required = false) String amount) {
        if (StringUtils.isNotEmpty(price)) {
            exchangeRemote.buyMarket(symbol, price);
        }
        if (StringUtils.isNotEmpty(amount)) {
            exchangeRemote.sellMarket(symbol, amount);
        }
        return "ok";
    }

    @RequestMapping("/test/trade/task")
    String tradeTask() {
        CheckResult btcCheckResult = BotUtil.check("btc.csv", 10);

        exchangeRemote.trade(Coin.BTC.getSymbol() + "_usdt", "sell", btcCheckResult.getPrice().multipliedBy(2).toString(), exchangeRemote.getTradeAmount("btc"));
        return "ok";
    }

    @RequestMapping("/test/status")
    String status() {
        String result = ("ACCOUNT_STATUS : " + ExchangeRemote.ACCOUNT_STATUS.toString());
        result += "\n";
        result += ("TRADE_STATUS" + ExchangeRemote.TRADE_STATUS);
        return result;
    }

    @RequestMapping("/task/add")
    String add(@RequestParam String symbol,
               @RequestParam String type) {

        coinService.addTask(symbol, type);
        return "ok";
    }

    @RequestMapping("/task/del")
    String del(@RequestParam String symbol,
               @RequestParam String type) {
        coinService.delTask(symbol, type);
        return "ok";
    }

    @RequestMapping("/task/list")
    List<TaskItem> list() {
        return coinService.listTask();
    }

    @RequestMapping("/kline")
    void kline() {
        List<KLineItem> neoLine = exchangeRemote.kLine(Coin.NEO.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        for (int i = 1; i < 5; i++) {
            System.out.println(neoLine.get(neoLine.size() - i));
        }
    }
}
