package com.chen.cryptocurrency.service.bean;

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
}
