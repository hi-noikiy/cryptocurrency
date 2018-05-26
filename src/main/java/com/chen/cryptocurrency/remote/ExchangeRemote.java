package com.chen.cryptocurrency.remote;

import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.TradeStatus;
import com.chen.cryptocurrency.util.*;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Service
public class ExchangeRemote {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CoinHttpClient httpUtil = ShellHttpClient.getInstance();

    @Value("${exchange.okex.domain}")
    private String okexDomain;
    @Value("${exchange.okex.api.kline}")
    private String okexApiKline;
    @Value("${exchange.okex.api.key}")
    private String okexApiKey;
    @Value("${exchange.okex.secret.key}")
    private String okexSecretKey;

    @Value("${exchange.okcoin.domain}")
    private String okcoinDomain;
    @Value("${exchange.okcoin.api.kline}")
    private String okcoinApiK;

    @Value("${url.feixiaohao.domain}")
    private String coinDataDomain;
    @Value("${url.coindata.api}")
    private String coinDataApi;


    public static final Map<String, String> ACCOUNT_STATUS = Maps.newHashMap();
    public static final TradeStatus TRADE_STATUS = new TradeStatus(1, 1, 1);

    public List<KLineItem> kLine(String symbol, String type, String exchange) {
        logger.info("[REMOTE]check kline, begin.");
        String param = "?symbol=" + symbol + "&type=" + type;

        String response;
        if ("okcoin".equalsIgnoreCase(exchange)) {
            response = httpUtil.requestHttpGet(okcoinDomain, okcoinApiK, param);
        } else {
            response = httpUtil.requestHttpGet(okexDomain, okexApiKline, param);
        }
        logger.info("[REMOTE]check kline, result:{}", response.substring(0, 256));

        Gson gson = new Gson();
        List<List> resList = gson.fromJson(response, List.class);

        return resList.stream()
                .map(KLineItem::of)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String path = "/api/v1/userinfo.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String okexApiKey = "f515b319-90e9-4cf3-8f0e-6fae75aaed29";
        String okexSecretKey = "0FC8FE7D669C4F7F46519188BF27D02F";

        String param = new Param()
                .add("api_key", okexApiKey)
                .add("secret_key", okexSecretKey)
                .build();

        String sign = MD5.encrypt(param);
        CoinHttpClient httpUtil = ShellHttpClient.getInstance();
        param = new Param(param).add("sign", sign).build();

        String okexDomain = "https://www.okex.com";

        String response = httpUtil.requestHttpPost(okexDomain, path, param, paramMap);
        System.out.println(response);
    }

    public void syncStatus() {
        logger.info("[REMOTE]sync status, begin.");

        String path = "/api/v1/userinfo.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String param = new Param()
                .add("api_key", okexApiKey)
                .add("secret_key", okexSecretKey)
                .build();

        String sign = MD5.encrypt(param);

        param = new Param(param).add("sign", sign).build();

        String response = httpUtil.requestHttpPost(okexDomain, path, param, paramMap);

        logger.info("[REMOTE]sync status, result:{}", response);

        Gson gson = new Gson();
        JsonObject resJson = gson.fromJson(response, JsonObject.class);

        JsonObject funds = resJson.getAsJsonObject("info").getAsJsonObject("funds");
        JsonObject freeFunds = funds.getAsJsonObject("free");

        String usdt = freeFunds.get("usdt").getAsString();
        String btc = freeFunds.get("btc").getAsString();
        String eos = freeFunds.get("eos").getAsString();
        String neo = freeFunds.get("neo").getAsString();

        ACCOUNT_STATUS.put("usdt", usdt);
        ACCOUNT_STATUS.put("btc", btc);
        ACCOUNT_STATUS.put("eos", eos);
        ACCOUNT_STATUS.put("neo", neo);

        if (Double.valueOf(btc) > 0.001) {
            TRADE_STATUS.setBtcStatus(1);
        } else {
            TRADE_STATUS.setBtcStatus(0);
        }

        if (Double.valueOf(eos) > 1) {
            TRADE_STATUS.setEosStatus(1);
        } else {
            TRADE_STATUS.setEosStatus(0);
        }

        if (Double.valueOf(neo) > 0.1) {
            TRADE_STATUS.setNeoStatus(1);
        } else {
            TRADE_STATUS.setNeoStatus(0);
        }
    }

    public String getTradeAmount(String key) {
        return ACCOUNT_STATUS.get(key);
    }

    public void sellMarket(String symbol, String amount) {
        logger.info("[REMOTE]sell market, begin.");
        logger.info("sell market, symbol:{}, amount:{}.", symbol, amount);

        String path = "/api/v1/trade.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String param = new Param()
                .add("amount", amount)
                .add("api_key", okexApiKey)
                .add("symbol", symbol)
                .add("type", "sell_market")
                .add("secret_key", okexSecretKey)
                .build();

        String sign = MD5.encrypt(param);

        param = new Param(param).add("sign", sign).build();

        String response = httpUtil.requestHttpPost(okexDomain, path, param, paramMap);
        logger.info("[REMOTE]sell market, result:{}", response);
    }

    public void buyMarket(String symbol, String price) {
        logger.info("[REMOTE]buy market, begin.");
        logger.info("buy market, symbol:{}, price:{}.", symbol, price);

        String path = "/api/v1/trade.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String param = new Param()
                .add("api_key", okexApiKey)
                .add("price", price)
                .add("symbol", symbol)
                .add("type", "buy_market")
                .add("secret_key", okexSecretKey)
                .build();

        String sign = MD5.encrypt(param);

        param = new Param(param).add("sign", sign).build();

        String response = httpUtil.requestHttpPost(okexDomain, path, param, paramMap);
        logger.info("[REMOTE]buy market, result:{}", response);
    }

    public void trade(String symbol, String type, String price, String amount) {
        logger.info("trade, symbol:{}, type:{}, price:{}, amount:{}.", symbol, type, price, amount);

        String path = "/api/v1/trade.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String param = new Param()
                .add("amount", amount)
                .add("api_key", okexApiKey)
                .add("price", price)
                .add("symbol", symbol)
                .add("type", type)
                .add("secret_key", okexSecretKey)
                .build();

        String sign = MD5.encrypt(param);

        param = new Param(param).add("sign", sign).build();

        String response = httpUtil.requestHttpPost(okexDomain, path, param, paramMap);
        logger.info("trade result : ");
        logger.info(response);
    }
}
