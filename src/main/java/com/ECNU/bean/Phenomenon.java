package com.ECNU.bean;

import lombok.Data;

@Data
public class Phenomenon {
    private String name;
    private String state;
    private Rect from;
    private Rect to;
    private boolean constraining = false;
    private Oval requirement;
    private int biaohao;

    public Phenomenon(String name, String state, Rect from, Rect to) {
        this.name = name;
        this.state = state;
        this.from = from;
        this.to = to;
        this.biaohao = com.ECNU.bean.Data.first;
        com.ECNU.bean.Data.first += 1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Phenomenon){
            Phenomenon temp = (Phenomenon)obj;
            return this.getBiaohao() == temp.getBiaohao() && this.getState().equals(temp.getState()) && this.getName().equals(temp.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31;
    }
}
