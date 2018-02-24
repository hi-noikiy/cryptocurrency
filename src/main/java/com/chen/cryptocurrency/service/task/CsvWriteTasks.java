package com.chen.cryptocurrency.service.task;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.*;
import com.chen.cryptocurrency.util.Constant;
import com.chen.cryptocurrency.util.FileUtil;
import com.chen.cryptocurrency.util.MailUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
@Component
public class CsvWriteTasks {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CoinService coinService;

    @Scheduled(cron = "0 * * * * ? ")
    public void reportCurrentTime() {
        List<KLineItem> btcLine = coinService.queryKLine(Coin.BTC.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.btc_file_name, btcLine);
        List<KLineItem> eosLine = coinService.queryKLine(Coin.EOS.getSymbol() + "_usdt", "4hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.eos_file_name, eosLine);
        List<KLineItem> neoLine = coinService.queryKLine(Coin.NEO.getSymbol() + "_usdt", "2hour", Exchange.OKEX.name());
        FileUtil.writeCSV(Constant.neo_file_name, neoLine);
    }
}
