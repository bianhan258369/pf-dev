package com.ECNU.bean;
public class StateMachine {
    String from;
    String to;
    String trans;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public boolean isAlternate( StateMachine temp){
        return (this.getFrom().equals(temp.getTo()) && this.getTo().equals(temp.getFrom()));
    }

    @Override
    public String toString() {
        return ("from " + from + " to " + to + " trans "+ trans);
    }
}

