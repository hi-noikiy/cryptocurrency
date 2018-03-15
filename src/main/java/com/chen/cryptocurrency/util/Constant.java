package com.chen.cryptocurrency.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class Constant {
    public final static Splitter KEY_SPLITTER = Splitter.on("|");
    public final static Joiner KEY_JOINER = Joiner.on("|");

    public final static String BTC_FILE_NAME = "btc.csv";
    public final static String EOS_FILE_NAME = "eos.csv";
    public final static String NEO_FILE_NAME = "neo.csv";
}
