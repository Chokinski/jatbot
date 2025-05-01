package com.jat.jatbot.ai;



public class StockData {
    private String timestamp;
    private float open;
    private float high;
    private float low;
    private float close;
    private Long volume;
    private float vwap;
    private float rsi;
    private float macd;
    private float roc;
    private float emaClose;
    private float atr;
    private float tema;
    private float smaTr;
    private float nwSmooth;
    private float nwSmoothOne;
    private float nwSmoothTwo;
    private float nwDerivative;
    private Integer id = null;



    public StockData(float float1, float float2, float float3, float float4, Long Lon,
            float float5, float float6, float float7, float float8, float float9, float float10,
            float float11, float float12, float float13, float float14, float float15, float float16) {
        
                this.open = float1;
                this.high = float2;
                this.low = float3;
                this.close = float4;
                this.volume = Lon;
                this.vwap = float5;
                this.rsi = float6;
                this.macd = float7;
                this.roc = float8;
                this.emaClose = float9;
                this.atr = float10;
                this.tema = float11;
                this.smaTr = float12;
                this.nwSmooth = float13;
                this.nwSmoothOne = float14;
                this.nwSmoothTwo = float15;
                this.nwDerivative = float16;


    }
    public StockData(float float1, float float2, float float3, float float4, Long Lon,
            float float5, float float6, float float7, float float8, float float9, float float10,
            float float11, float float12, float float13, float float14, float float15, float float16, String ts) {
        
                this.open = float1;
                this.high = float2;
                this.low = float3;
                this.close = float4;
                this.volume = Lon;
                this.vwap = float5;
                this.rsi = float6;
                this.macd = float7;
                this.roc = float8;
                this.emaClose = float9;
                this.atr = float10;
                this.tema = float11;
                this.smaTr = float12;
                this.nwSmooth = float13;
                this.nwSmoothOne = float14;
                this.nwSmoothTwo = float15;
                this.nwDerivative = float16;
                this.timestamp = ts;

    }

    public int getId() {
        return this.id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getOpen() {
        return this.open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return this.high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return this.low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return this.close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public Long getVolume() {
        return this.volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public float getVwap() {
        return this.vwap;
    }

    public void setVwap(float vwap) {
        this.vwap = vwap;
    }

    public float getRsi() {
        return this.rsi;
    }

    public void setRsi(float rsi) {
        this.rsi = rsi;
    }

    public float getMacd() {
        return this.macd;
    }

    public void setMacd(float macd) {
        this.macd = macd;
    }

    public float getRoc() {
        return this.roc;
    }

    public void setRoc(float roc) {
        this.roc = roc;
    }

    public float getEmaClose() {
        return this.emaClose;
    }

    public void setEmaClose(float emaClose) {
        this.emaClose = emaClose;
    }

    public float getAtr() {
        return this.atr;
    }

    public void setAtr(float atr) {
        this.atr = atr;
    }

    public float getTema() {
        return this.tema;
    }

    public void setTema(float tema) {
        this.tema = tema;
    }

    public float getSmaTr() {
        return this.smaTr;
    }

    public void setSmaTr(float smaTr) {
        this.smaTr = smaTr;
    }

    public float getNwSmooth() {
        return this.nwSmooth;
    }

    public void setNwSmooth(float nwSmooth) {
        this.nwSmooth = nwSmooth;
    }

    public float getNwSmoothOne() {
        return this.nwSmoothOne;
    }

    public void setNwSmoothOne(float nwSmoothOne) {
        this.nwSmoothOne = nwSmoothOne;
    }

    public float getNwSmoothTwo() {
        return this.nwSmoothTwo;
    }

    public void setNwSmoothTwo(float nwSmoothTwo) {
        this.nwSmoothTwo = nwSmoothTwo;
    }

    public float getNwDerivative() {
        return this.nwDerivative;
    }

    public void setNwDerivative(float nwDerivative) {
        this.nwDerivative = nwDerivative;
    }


    // Constructor, getters, setters
}
