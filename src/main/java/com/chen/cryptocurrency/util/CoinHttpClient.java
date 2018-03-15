package com.chen.cryptocurrency.util;

import java.util.Map;

public interface CoinHttpClient {
    String requestHttpGet(String domain, String url, String param);

    String requestHttpPost(String domain, String path, String param, Map<String, String> params);
}
