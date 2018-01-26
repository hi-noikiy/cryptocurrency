package com.chen.cryptocurrency.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class Constant {
    public static Splitter key_splitter = Splitter.on("|");
    public static Joiner key_joiner = Joiner.on("|");

    public static String EXCHANGE_OKEX = "okex";
    public static String EXCHANGE_OKCOIN = "okcoin";
}
