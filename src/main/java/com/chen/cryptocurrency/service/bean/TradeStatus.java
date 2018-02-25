package com.chen.cryptocurrency.service.bean;

public class TradeStatus {
    private int btcStatus;
    private int eosStatus;
    private int neoStatus;

    public int getBtcStatus() {
        return btcStatus;
    }

    public void setBtcStatus(int btcStatus) {
        this.btcStatus = btcStatus;
    }

    public int getEosStatus() {
        return eosStatus;
    }

    public void setEosStatus(int eosStatus) {
        this.eosStatus = eosStatus;
    }

    public int getNeoStatus() {
        return neoStatus;
    }

    public void setNeoStatus(int neoStatus) {
        this.neoStatus = neoStatus;
    }

    public TradeStatus() {
    }

    public TradeStatus(int btcStatus, int eosStatus, int neoStatus) {
        this.btcStatus = btcStatus;
        this.eosStatus = eosStatus;
        this.neoStatus = neoStatus;
    }

    public int buyTotal() {
        return btcStatus + eosStatus + neoStatus;
    }

    @Override
    public String toString() {
        return "TradeStatus{" +
                "btcStatus=" + btcStatus +
                ", eosStatus=" + eosStatus +
                ", neoStatus=" + neoStatus +
                '}';
    }
}
