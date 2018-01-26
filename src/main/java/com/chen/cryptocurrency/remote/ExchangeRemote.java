package com.chen.cryptocurrency.remote;

import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.util.HttpUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Service
public class ExchangeRemote {

    @Value("${exchange.okex.domain}")
    private String okexDomain;
    @Value("${exchange.okex.api.k}")
    private String okexApiK;
    @Value("${exchange.okcoin.domain}")
    private String okcoinDomain;
    @Value("${exchange.okcoin.api.k}")
    private String okcoinApiK;

    public List<KLineItem> kLine(String symbol, String type, String exchange) {
        HttpUtil httpUtil = HttpUtil.getInstance();
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
}
