package com.johnsimon.payback.currency;

public class ConvertResult {

    private String from;
    private Number rate;
    private String to;

    public String getFrom(){
        return this.from;
    }
    public void setFrom(String from){
        this.from = from;
    }
    public Number getRate(){
        return this.rate;
    }
    public void setRate(Number rate){
        this.rate = rate;
    }
    public String getTo(){
        return this.to;
    }
    public void setTo(String to){
        this.to = to;
    }
}
