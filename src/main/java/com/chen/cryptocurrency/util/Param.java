package com.chen.cryptocurrency.util;

import org.apache.commons.lang3.StringUtils;

public class Param {
    private String param;

    public Param() {
    }

    public Param(String param) {
        this.param = param;
    }

    public Param(String key, String value) {
        this.param = keyValueString(key, value);
    }

    public Param add(String key, String value) {
        if (StringUtils.isEmpty(this.param)) {
            this.param = keyValueString(key, value);
        } else {
            this.param += ("&" + keyValueString(key, value));
        }
        return this;
    }

    public String build() {
        return this.param;
    }

    private String keyValueString(String key, String value) {
        return key + "=" + value;
    }
}
