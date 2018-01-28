package com.chen.cryptocurrency.service.bean;

public class CoinDataItem {
    private double price;
    private double vol;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    @Override
    public String toString() {
        return "CoinDataItem{" +
                "price=" + price +
                ", vol=" + vol +
                '}';
    }
}
