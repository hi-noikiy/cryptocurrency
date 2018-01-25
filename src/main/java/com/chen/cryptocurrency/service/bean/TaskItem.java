package com.chen.cryptocurrency.service.bean;

public class TaskItem {
    private String symbol;
    private String type;

    public TaskItem() {
    }

    public TaskItem(String symbol, String type) {
        this.symbol = symbol;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
