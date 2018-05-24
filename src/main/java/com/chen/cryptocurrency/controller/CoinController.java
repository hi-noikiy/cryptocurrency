package com.chen.cryptocurrency.controller;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.Coin;
import com.chen.cryptocurrency.util.BotUtil;
import com.chen.cryptocurrency.util.Constant;
import com.google.common.collect.Maps;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.Trade;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */

@RestController
@EnableAutoConfiguration
@RequestMapping("/door")
public class CoinController {
    @Resource
    private CoinService coinService;
    @Resource
    private ExchangeRemote exchangeRemote;

    @RequestMapping("/test/writeCSV")
    String writeCSV() {
        coinService.csvSync();
        return "ok";
    }

    @RequestMapping("/test/syncStatus")
    String syncStatus() {
        exchangeRemote.syncStatus();
        return "ok";
    }

    @RequestMapping("/trade/current")
    @ResponseBody
    Map<Coin, Trade> current() {
        Trade btcTrade = BotUtil.current(Constant.BTC_FILE_NAME, coinService.rangeGet(Coin.BTC));
        Trade eosTrade = BotUtil.current(Constant.EOS_FILE_NAME, coinService.rangeGet(Coin.EOS));
        Trade neoTrade = BotUtil.current(Constant.NEO_FILE_NAME, coinService.rangeGet(Coin.NEO));

        Map<Coin, Trade> result = Maps.newTreeMap();
        result.put(Coin.BTC, btcTrade);
        result.put(Coin.EOS, eosTrade);
        result.put(Coin.NEO, neoTrade);

        return result;
    }

    @RequestMapping("/trade/checkBS")
    @ResponseBody
    Map<Coin, Integer> checkBS() {

        Integer btcBestRange = coinService.rangeGet(Coin.BTC);
        Integer eosBestRange = coinService.rangeGet(Coin.EOS);
        Integer neoBestRange = coinService.rangeGet(Coin.NEO);

        Map<Coin, Integer> result = Maps.newTreeMap();
        result.put(Coin.BTC, btcBestRange);
        result.put(Coin.EOS, eosBestRange);
        result.put(Coin.NEO, neoBestRange);

        return result;
    }

    @RequestMapping("/trade/status")
    @ResponseBody
    Map<String, Object> status() {
        Map<String, Object> result = Maps.newTreeMap();
        result.put("ACCOUNT_STATUS", ExchangeRemote.ACCOUNT_STATUS.toString());
        result.put("TRADE_STATUS", ExchangeRemote.TRADE_STATUS);
        return result;
    }
}
