package com.chen.cryptocurrency.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class Constant {
    public final static Splitter key_splitter = Splitter.on("|");
    public final static Joiner key_joiner = Joiner.on("|");

    public final static String btc_file_name = "btc.csv";
    public final static String eos_file_name = "eos.csv";
    public final static String neo_file_name = "neo.csv";
}
