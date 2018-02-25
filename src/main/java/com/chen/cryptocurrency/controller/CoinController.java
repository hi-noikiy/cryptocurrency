package com.chen.cryptocurrency.controller;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.CheckResult;
import com.chen.cryptocurrency.service.bean.Coin;
import com.chen.cryptocurrency.service.bean.TaskItem;
import com.chen.cryptocurrency.util.BotUtil;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/test/trade/task")
    String tradeTask() {
        CheckResult btcCheckResult = BotUtil.check("btc.csv", 34);

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

}
