package com.ECNU.bean;

import lombok.Data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
public class Line extends Shape{
    private int state;
    private Shape from;
    private Shape to;
    private String name;
    private List<Phenomenon> phenomena = new LinkedList<>();
    private String core = "";
    private String core1 = "";

    public Line(Shape from, Shape to, int state) {
        this.setShape(2);
        setState(state);
        this.name = "";
        if (from.getShape() == 0) {
            Rect tmp_r = (Rect)from;
            if (tmp_r.getState() == 2) {
                this.from = from;
                this.to = to;
            }
            else {
                this.to = from;
                this.from = to;
            }
        }
        else {
            this.from = from;
            this.to = to;
        }
    }

    public String getDescription() {
        String s = "";
        s = s + this.name + ":";
        String s1 = "";
        String s2 = "";
        Rect m = null;
        Rect d = null;
        if (this.phenomena.size() == 0) {
            if (this.state == 0) {
                return s + com.ECNU.bean.Data.I_TEXT;
            }
            if (this.state == 1) {
                return s + com.ECNU.bean.Data.RR_TEXT;
            }
            if (this.state == 2) {
                return s + com.ECNU.bean.Data.RC_TEXT;
            }
        }
        for (int i = 0; i <= this.phenomena.size() - 1; i++) {
            Phenomenon tmp_p = this.phenomena.get(i);
            Rect f = tmp_p.getFrom();
            Rect t = tmp_p.getTo();
            if (f.getState() == 2) {
                m = f;
                d = t;
                s1 = s1 + tmp_p.getName() + ",";
            }
            else {
                d = f;
                m = t;
                s2 = s2 + tmp_p.getName() + ",";
            }
        }

        if (!s1.equals("")) {
            s1 = s1.substring(0, s1.length() - 1);
            s1 = "{" + s1 + "}";
            if (m.getShortName() != null) {
                s1 = m.getShortName() + "!" + s1;
            }
        }
        if (!s2.equals("")) {
            s2 = s2.substring(0, s2.length() - 1);
            s2 = "{" + s2 + "}";
            if (d.getShortName() != null) {
                s2 = d.getShortName() + "!" + s2;
            }
        }
        if ((!s1.equals("")) && (!this.core.equals(""))) {
            s1 = s1 + "[" + this.core + "]";
        }
        if ((!s2.equals("")) && (!this.core1.equals(""))) {
            s2 = s2 + "[" + this.core1 + "]";
        }
        if ((!s1.equals("")) && (!s2.equals("")))
        {
            s = s + s1 + "," + s2;
        }
        else if (!s1.equals("")) {
            s = s + s1;
        }
        else if (!s2.equals("")) {
            s = s + s2;
        }
        return s;
    }
}
