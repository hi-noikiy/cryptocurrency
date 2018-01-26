package com.chen.cryptocurrency.controller;

import com.chen.cryptocurrency.service.CoinService;
import com.chen.cryptocurrency.service.bean.TaskItem;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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

    @RequestMapping("/task/add")
    String add(@RequestParam String symbol,
               @RequestParam String type) {

        coinService.addTask(symbol, type);
        return "ok";
    }

    @RequestMapping("/task/del")
    String del(@RequestParam String symbol,
               @RequestParam String type) {
        coinService.delTask(symbol, type);
        return "ok";
    }

    @RequestMapping("/task/list")
    List<TaskItem> list() {
        return coinService.listTask();
    }

    @RequestMapping("/mail/test")
    String mailTest() {
        coinService.mailTest();
        return "ok";
    }
}
