package com.ECNU.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
@Data
public class Scenario implements Serializable, Cloneable{
    private int state;
    private LinkedList<String> turning = new LinkedList<>();
    private Interaction from;
    private Interaction to;
    private int x1;
    private int x2;
    private int y1;
    private int y2;

    public Scenario(LinkedList<String> turning, int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.turning = turning;
    }

    public Scenario(LinkedList<String> turning, Interaction from, Interaction to, int state) {
        this.turning = turning;
        this.state = state;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        Scenario changjing = (Scenario)obj;
        return (this.getFrom().equals(changjing.getFrom()) && this.getTo().equals(changjing.getTo()) && this.getState() == changjing.getState());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (Scenario)super.clone();
    }
}
