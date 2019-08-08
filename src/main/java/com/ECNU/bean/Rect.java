package com.ECNU.bean;

import lombok.Data;

@Data
public class Rect extends Shape{
    private String text;
    private int state;//2-machine
    private char cxb = '\000';
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int length;
    private String shortName;

    public Rect(int middlex, int middley) {
        this.state = 0;
        this.text = "";
        setSize(middlex, middley);
        this.setShape(0);
    }

    private void setSize(int middlex, int middley) {
        this.length = (this.text.length() * 7 + 25);
        this.y2 = 40;
        this.x2 = this.length;

        this.x1 = (middlex - this.x2 / 2);
        this.y1 = (middley - this.y2 / 2);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Rect){
            Rect temp = (Rect)obj;
            return (this.getText().equals(temp.getText()) && this.getState() == temp.getState());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = getText().hashCode() * 31 + state;
        return result;
    }
}
