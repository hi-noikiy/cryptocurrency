package com.chen.cryptocurrency.service;

import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.service.cache.KLineCache;
import com.chen.cryptocurrency.util.MACDUtil;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CoinService {
    @Resource
    private KLineCache kLineCache;

    public List<KLineItem> queryKLine() {
        return kLineCache.get("btc_usd", "2hour");
    }

    public List<Map<String, Double>> macd(Integer n) {
        List<Double> list =
                queryKLine().stream().map(item -> Double.valueOf(item.getCloseValue())).collect(Collectors.toList());

        List<Map<String, Double>> result = Lists.newArrayList();
        for (int i = n; i >= 0; i--) {
            List<Double> temp = list.subList(0, list.size() - i);
            result.add(MACDUtil.getMACD(temp, 12, 26, 9));
        }
        return result;
    }
}
