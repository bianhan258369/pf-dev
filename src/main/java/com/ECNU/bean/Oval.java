package com.ECNU.bean;

import lombok.Data;

@Data
public class Oval extends Shape{
    private String text;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int length;
    private int biaohao;

    public Oval(int middlex, int middley) {
        setShape(1);
        this.text = "?Requirement";
        setSize(middlex, middley);
        this.setDes(2);
        this.biaohao = com.ECNU.bean.Data.firstq;
        com.ECNU.bean.Data.firstq += 1;
    }

    private void setSize(int middlex, int middley)
    {
        this.length = (this.text.length() * 7 + 25);
        this.length = 40;
        this.x2 = this.length;
        this.y2 = 50;
        this.x1 = (middlex - this.x2 / 2);
        this.y1 = (middley - this.y2 / 2);
    }

    public void setText(String text) {
        this.text = text;
        setDes(1);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Oval){
            Oval temp = (Oval)obj;
            return temp.getText().equals(this.getText());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getText().hashCode() * 31;
    }
}
