package com.chen.cryptocurrency.controller;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.KLineItem;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */

@RestController
@EnableAutoConfiguration
@RequestMapping("/coin")

public class CoinController {
    @Resource
    private CoinService coinService;

    @RequestMapping("k")
    List<KLineItem> kLine() {
        return coinService.queryKLine();
    }

    @RequestMapping("macd")
    List<Map<String, Double>> m(@RequestParam(required = false) Integer n) {
        if (Objects.isNull(n)) {
            n = 10;
        }
        return coinService.macd(n);
    }
}
