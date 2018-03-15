package com.chen.cryptocurrency.service.bean;

import static com.chen.cryptocurrency.util.Constant.*;

public enum Coin {
    BTC("btc","bitcoin"),
    ETH("eth","ethereum"),
    EOS("eos","eos"),
    NEO("neo","neo"),
    QTUM("qtum","qtum"),
    NAS("nas","nas"),
    RDN("rdn","rdn"),
    ZEC("zec","zec"),
    ;

    String exchangeName;
    String dataName;

    Coin() {
    }

    Coin(String exchangeName, String dataName) {
        this.exchangeName = exchangeName;
        this.dataName = dataName;
    }

    public String getSymbol() {
        return exchangeName;
    }
    public String getExchangeName() {
        return exchangeName;
    }

    public String getDataName() {
        return dataName;
    }


    public static String getFileName(Coin coin) {
        switch (coin) {
            case BTC:
                return BTC_FILE_NAME;
            case EOS:
                return EOS_FILE_NAME;
            case NEO:
                return NEO_FILE_NAME;

            default:
                throw new RuntimeException("未找到对应文件，coin:" + coin.getSymbol());
        }
    }
}
