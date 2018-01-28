package com.chen.cryptocurrency.service.bean;

public enum  Exchange {
    OKEX("okex"),
    OKCOIN("okcoin");

    String name;

    Exchange(String name) {
        this.name = name;
    }
}
