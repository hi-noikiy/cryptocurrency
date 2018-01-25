package com.chen.cryptocurrency.util;

import com.chen.cryptocurrency.service.bean.MACDItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class MACDUtil {
    /**
     * Calculate EMA,
     *
     * @param list
     *            :Price list to calculate，the first at head, the last at tail.
     * @return
     */
    public static final Double getEXPMA(final List<Double> list, final int number) {
        // 开始计算EMA值，
        Double k = 2.0 / (number + 1.0);// 计算出序数
        Double ema = list.get(0);// 第一天ema等于当天收盘价
        for (int i = 1; i < list.size(); i++) {
            // 第二天以后，当天收盘 收盘价乘以系数再加上昨天EMA乘以系数-1
            ema = list.get(i) * k + ema * (1 - k);
        }
        return ema;
    }

    /**
     * calculate MACD values
     *
     * @param list
     *            :Price list to calculate，the first at head, the last at tail.
     * @param shortPeriod
     *            :the short period value.
     * @param longPeriod
     *            :the long period value.
     * @param midPeriod
     *            :the mid period value.
     * @return
     */
    public static final MACDItem getMACD(final List<Double> list, final int shortPeriod, final int longPeriod, int midPeriod) {
        List<Double> diffList = new ArrayList<Double>();
        Double shortEMA;
        Double longEMA;
        Double dif = 0.0;
        Double dea;

        for (int i = list.size() - 1; i >= 0; i--) {
            List<Double> sublist = list.subList(0, list.size() - i);
            shortEMA = MACDUtil.getEXPMA(sublist, shortPeriod);
            longEMA = MACDUtil.getEXPMA(sublist, longPeriod);
            dif = shortEMA - longEMA;
            diffList.add(dif);
        }
        dea = MACDUtil.getEXPMA(diffList, midPeriod);
        MACDItem item = new MACDItem();
        item.setDea(dea);
        item.setDif(dif);
        item.setMacd((dif - dea) * 2);
        return item;
    }
}
