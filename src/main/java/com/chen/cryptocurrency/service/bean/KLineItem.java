package com.chen.cryptocurrency.service.bean;

import java.util.List;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class KLineItem {
    private Double timeStamp;
    private String openValue;
    private String highValue;
    private String lowValue;
    private String closeValue;
    private String tradeVolume;

    public static KLineItem of(List item) {
        KLineItem kLineItem = new KLineItem();
        if (item.size() != 6) {
            throw new RuntimeException("[KLineItem]格式不对，无法转换。");
        }
        kLineItem.setTimeStamp(Double.valueOf(String.valueOf(item.get(0))));
        kLineItem.setOpenValue(String.valueOf(item.get(1)));
        kLineItem.setHighValue(String.valueOf(item.get(2)));
        kLineItem.setLowValue(String.valueOf(item.get(3)));
        kLineItem.setCloseValue(String.valueOf(item.get(4)));
        kLineItem.setTradeVolume(String.valueOf(item.get(5)));

        return kLineItem;
    }

    public Double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getOpenValue() {
        return openValue;
    }

    public void setOpenValue(String openValue) {
        this.openValue = openValue;
    }

    public String getHighValue() {
        return highValue;
    }

    public void setHighValue(String highValue) {
        this.highValue = highValue;
    }

    public String getLowValue() {
        return lowValue;
    }

    public void setLowValue(String lowValue) {
        this.lowValue = lowValue;
    }

    public String getCloseValue() {
        return closeValue;
    }

    public void setCloseValue(String closeValue) {
        this.closeValue = closeValue;
    }

    public String getTradeVolume() {
        return tradeVolume;
    }

    public void setTradeVolume(String tradeVolume) {
        this.tradeVolume = tradeVolume;
    }
}
