package com.example.myapplication;

public class CurrencyRVModal {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    private String name;
    private String symbol;
    private double price;

    public CurrencyRVModal(String name, String symbol, double price) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
    }


}
