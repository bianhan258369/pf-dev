package com.ECNU.bean;

import lombok.Data;

@Data
public class Clock {
    private String name;
    private String unit;
    private int clockType;

    public static int PHYSICAL=0;
    public static int LOGICAL=1;

    private String resolution=null;
    private String max=null;
    private String offset=null;
    private String domainText = null;
    private Colour color;

    public Clock(String domainText) {
        this.name="Default";
        this.clockType=PHYSICAL;
        this.unit="s";
        this.domainText = domainText;
    }

    public Clock(String name,int type,String unit,String domainText){
        this.name=name;
        this.clockType=type;
        this.unit=unit;
        this.domainText = domainText;
    }
}
