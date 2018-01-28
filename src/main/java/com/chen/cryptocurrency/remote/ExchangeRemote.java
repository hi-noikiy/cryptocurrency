package com.chen.cryptocurrency.remote;

import com.chen.cryptocurrency.service.bean.CoinDataGet;
import com.chen.cryptocurrency.service.bean.CoinDataItem;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.util.HttpUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Service
public class ExchangeRemote {
    private HttpUtil httpUtil = HttpUtil.getInstance();

    @Value("${exchange.okex.domain}")
    private String okexDomain;
    @Value("${exchange.okex.api.k}")
    private String okexApiK;
    @Value("${exchange.okcoin.domain}")
    private String okcoinDomain;
    @Value("${exchange.okcoin.api.k}")
    private String okcoinApiK;
    @Value("${url.feixiaohao.domain}")
    private String coinDataDomain;
    @Value("${url.coindata.api}")
    private String coinDataApi;

    public List<KLineItem> kLine(String symbol, String type, String exchange) {
        String param = "?symbol=" + symbol + "&type=" + type;

        String response;
        if ("okcoin".equalsIgnoreCase(exchange)) {
            response = httpUtil.requestHttpGet(okcoinDomain, okcoinApiK, param);
        } else {
            response = httpUtil.requestHttpGet(okexDomain, okexApiK, param);
        }
        Gson gson = new Gson();
        List<List> resList = gson.fromJson(response, List.class);

        return resList.stream()
                .map(KLineItem::of)
                .collect(Collectors.toList());
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
