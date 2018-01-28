package com.chen.cryptocurrency.service.bean;

import java.util.List;

public class CoinDataGet {
    private List<List<Double>> market_cap_by_available_supply;
    private List<List<Double>> price_usd;
    private List<List<Double>> price_btc;
    private List<List<Double>> vol_usd;


    public List<List<Double>> getMarket_cap_by_available_supply() {
        return market_cap_by_available_supply;
    }

    public void setMarket_cap_by_available_supply(List<List<Double>> market_cap_by_available_supply) {
        this.market_cap_by_available_supply = market_cap_by_available_supply;
    }

    public List<List<Double>> getPrice_usd() {
        return price_usd;
    }

    public void setPrice_usd(List<List<Double>> price_usd) {
        this.price_usd = price_usd;
    }

    public List<List<Double>> getPrice_btc() {
        return price_btc;
    }

    public void setPrice_btc(List<List<Double>> price_btc) {
        this.price_btc = price_btc;
    }

    public List<List<Double>> getVol_usd() {
        return vol_usd;
    }

    public void setVol_usd(List<List<Double>> vol_usd) {
        this.vol_usd = vol_usd;
    }

    @Override
    public String toString() {
        return "CoinDataGet{" +
                "market_cap_by_available_supply=" + market_cap_by_available_supply +
                ", price_usd=" + price_usd +
                ", price_btc=" + price_btc +
                ", vol_usd=" + vol_usd +
                '}';
    }
}
