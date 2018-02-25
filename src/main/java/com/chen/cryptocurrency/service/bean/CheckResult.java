package com.chen.cryptocurrency.service.bean;

import org.ta4j.core.Decimal;

public class CheckResult {
    private int sign;
    private Decimal price;

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public Decimal getPrice() {
        return price;
    }

    public void setPrice(Decimal price) {
        this.price = price;
    }

    public boolean shouldBuy() {
        return sign == 1;
    }

    public boolean shouldSell() {
        return sign == -1;
    }

    @Override
    public String toString() {
        return "CheckResult{" +
                "sign=" + sign +
                ", price=" + price +
                '}';
    }
}
