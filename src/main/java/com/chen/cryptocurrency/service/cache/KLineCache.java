package com.chen.cryptocurrency.service.cache;

import com.chen.cryptocurrency.remote.ExchangeRemote;
import com.chen.cryptocurrency.service.bean.KLineItem;
import com.chen.cryptocurrency.util.Constant;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class KLineCache {

    @Resource
    private ExchangeRemote exchangeRemote;

    LoadingCache<String, List<KLineItem>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<String, List<KLineItem>>() {
                @Override
                public List<KLineItem> load(String key) {
                    List<String> keyList = Constant.key_splitter.splitToList(key);
                    return exchangeRemote.kLine(keyList.get(0), keyList.get(1));
                }
            });

    public List<KLineItem> get(String symbol, String type) {
        try {
            return cache.get(genKey(symbol, type));
        } catch (ExecutionException e) {
            return exchangeRemote.kLine(symbol, type);
        }
    }

    private String genKey(String symbol, String type) {
        return Constant.key_joiner.join(symbol, type);
    }
}
