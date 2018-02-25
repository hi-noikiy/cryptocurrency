package com.chen.cryptocurrency.remote;

import com.chen.cryptocurrency.service.bean.CoinDataGet;
import com.chen.cryptocurrency.service.bean.CoinDataItem;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.bean.TradeStatus;
import com.chen.cryptocurrency.util.HttpUtil;
import com.chen.cryptocurrency.util.MD5;
import com.chen.cryptocurrency.util.Param;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.awt.SystemColor.info;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Service
public class ExchangeRemote {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpUtil httpUtil = HttpUtil.getInstance();

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
        String param = "?symbol=" + symbol + "&type=" + type;

        String response;
        if ("okcoin".equalsIgnoreCase(exchange)) {
            response = httpUtil.requestHttpGet(okcoinDomain, okcoinApiK, param);
        } else {
            response = httpUtil.requestHttpGet(okexDomain, okexApiKline, param);
        }
        Gson gson = new Gson();
        List<List> resList = gson.fromJson(response, List.class);

        return resList.stream()
                .map(KLineItem::of)
                .collect(Collectors.toList());
    }

    public void syncStatus() {
        String path = "/api/v1/userinfo.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String param = new Param()
                .add("api_key", okexApiKey)
                .add("secret_key", okexSecretKey)
                .build();

        String sign = MD5.encrypt(param);

        param = new Param(param).add("sign", sign).build();

        String response = httpUtil.requestHttpPost(okexDomain, path, param, paramMap);
        Gson gson = new Gson();
        JsonObject resJson = gson.fromJson(response, JsonObject.class);

        JsonObject funds = resJson.getAsJsonObject("info").getAsJsonObject("funds");
        JsonObject freeFunds = funds.getAsJsonObject("free");

        String usdt = freeFunds.get("usdt").getAsString();
        String btc = freeFunds.get("btc").getAsString();
        String eos = freeFunds.get("eos").getAsString();
        String neo = freeFunds.get("neo").getAsString();

        logger.info("account status sync.");
        logger.info("usdt : {}", usdt);
        logger.info("btc : {}", btc);
        logger.info("eos : {}", eos);
        logger.info("neo : {}", neo);

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

    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1.1");
        HttpUtil httpUtil = HttpUtil.getInstance();

        String domain = "https://www.okex.com";
        String path = "/api/v1/userinfo.do";
        Map<String, String> paramMap = Maps.newHashMap();

        String param = new Param()
                .add("api_key", "f515b319-90e9-4cf3-8f0e-6fae75aaed29")
                .add("secret_key", "0FC8FE7D669C4F7F46519188BF27D02F")
                .build();
        String sign = MD5.encrypt(param);
        param = new Param(param).add("sign", sign).build();

        String response = httpUtil.requestHttpPost(domain, path, param, paramMap);
        Gson gson = new Gson();
        System.out.println(gson.fromJson(response, Map.class));

    }

    public List<CoinDataItem> coinData(String coinDataName) {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.minusYears(1);

        long endTimeStamp = now.atStartOfDay().toEpochSecond(ZoneOffset.of("+8")) * 1000;
        long beginTimeStamp = begin.atStartOfDay().toEpochSecond(ZoneOffset.of("+8")) * 1000;

        String param = "/" + beginTimeStamp + "/" + endTimeStamp;
        HttpUtil httpUtil = HttpUtil.getInstance();
        String response = httpUtil.requestHttpGet(coinDataDomain, coinDataApi + "/" + coinDataName, param);
        Gson gson = new Gson();
        CoinDataGet coinDataGet = gson.fromJson(response, CoinDataGet.class);

        List<List<Double>> coinPriceData = coinDataGet.getPrice_usd();
        List<CoinDataItem> coinDataList = coinPriceData.stream()
                .map(coinData -> {
                            CoinDataItem coinDataItem = new CoinDataItem();
                            coinDataItem.setPrice(coinData.get(1));
                            return coinDataItem;
                        }
                )
                .collect(Collectors.toList());

        List<List<Double>> coinVolData = coinDataGet.getVol_usd();
        for (int i = 0; i < coinDataList.size(); i++) {
            CoinDataItem coinDataItem = coinDataList.get(i);
            List<Double> volData = coinVolData.get(i);
            coinDataItem.setVol(volData.get(1));
        }

        return coinDataList;
    }
}
