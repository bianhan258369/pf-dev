package com.ECNU.bean;

import lombok.Data;

@Data
public class Colour {
    private int r;
    private int g;
    private int b;

    public Colour(int r, int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Colour(){
        this.r = 0;
        this.g = 0;
        this.b = 0;
    }

    @Override
    public String toString() {
        return ("(" + this.r + "," + this.g + "," + this.b + ")");
    }
}
