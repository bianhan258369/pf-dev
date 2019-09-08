package com.ECNU.bean;

import lombok.*;
import lombok.Data;

import java.io.Serializable;

@Data
public class Interaction implements Cloneable, Comparable<Interaction>, Serializable{
    private static final long serialVersionUID = -7665441956069222621L;

    private int number;
    private int x1,x2,y1,y2;//x1,y1 is the cordinate of the left ndoe and x2,y2 is width and height
    @NonNull
    private int state;
    private String name = "int";

    public Interaction(int number, int state){
        this.number = number;
        this.state = state;
    }

    public Interaction(int middleX,int middleY, int number,int state){
        this.number = number;
        this.state = state;
        setSize(middleX, middleY);
    }

    public void setSize(int middlex, int middley) {
        this.x2 = 60;
        this.y2 = 30;
        this.x1 = (middlex - this.x2 / 2);
        this.y1 = (middley - this.y2 / 2);
    }

    public int getMiddleX() {
        return this.x1 + this.x2 / 2;
    }

    public int getMiddleY() {
        return this.y1 + this.y2 / 2;
    }

    public int toNum(){
        if(state == 0) return (-1 * number);
        else return number;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Interaction){
            Interaction temp = (Interaction)obj;
            return (this.state == temp.state && this.number == temp.number);
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (Interaction)super.clone();
    }

    @Override
    public int compareTo(Interaction o) {
        return this.x1 - o.x1;
    }

}
