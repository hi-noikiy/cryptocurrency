package com.chen.cryptocurrency.remote;

import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.util.HttpUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Service
public class ExchangeRemote {

    @Value("${exchange.domain}")
    private String domain;

    @Value("${exchange.api.k}")
    private String apiK;

    public List<KLineItem> kLine() {
        return kLine("", "");
    }

    public List<KLineItem> kLine(String symbol, String type) {
        HttpUtil httpUtil = HttpUtil.getInstance();

        if (StringUtils.isEmpty(symbol)) {
            symbol = "btc_usd";
        }
        if (StringUtils.isEmpty(type)) {
            type = "2hour";
        }

        String param = "?symbol=" + symbol + "&type=" + type;
        String response = httpUtil.requestHttpGet(domain, apiK, param);
        Gson gson = new Gson();
        List<List> resList = gson.fromJson(response, List.class);

        return resList.stream()
                .map(KLineItem::of)
                .collect(Collectors.toList());
    }
}
