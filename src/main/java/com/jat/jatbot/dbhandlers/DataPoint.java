package com.jat.jatbot.dbhandlers;

import java.time.LocalDateTime;
import java.util.Objects;

public class DataPoint {
    LocalDateTime dateTime;
    double open;
    double high;
    double low;
    double close;
    double volume;
    double vwap;
    double rsi;
    double macd;
    double roc;
    double emaClose;
    double atr;
    double tema;
    double smaTr;
    double nw_smooth;
    double nw_smooth2;
    double nw_smooth3;
    double nw_derivative;
    public DataPoint(LocalDateTime dt, double open, double high, double low, double close, double volume)
    {
        this.dateTime = dt;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;

    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getOpen() {
        return this.open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return this.high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return this.low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return this.close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVolume() {
        return this.volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getVwap() {
        return this.vwap;
    }

    public void setVwap(double vwap) {
        this.vwap = vwap;
    }

    public double getRsi() {
        return this.rsi;
    }

    public void setRsi(double rsi) {
        this.rsi = rsi;
    }

    public double getMacd() {
        return this.macd;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }

    public double getRoc() {
        return this.roc;
    }

    public void setRoc(double roc) {
        this.roc = roc;
    }

    public double getEmaClose() {
        return this.emaClose;
    }

    public void setEmaClose(double emaClose) {
        this.emaClose = emaClose;
    }

    public double getAtr() {
        return this.atr;
    }

    public void setAtr(double atr) {
        this.atr = atr;
    }

    public double getTema() {
        return this.tema;
    }

    public void setTema(double tema) {
        this.tema = tema;
    }

    public double getSmaTr() {
        return this.smaTr;
    }

    public void setSmaTr(double smaTr) {
        this.smaTr = smaTr;
    }


}
