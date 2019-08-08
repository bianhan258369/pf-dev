package com.ECNU.bean;

public class Data {
    public static String GD_TEXT = "?ProblemDomain";
    public static String M_TEXT = "?Machine";
    public static String I_TEXT = "?Interface";
    public static String R_TEXT = "?Requirement";
    public static String DD_TEXT = "?GivenDomain";
    public static String RR_TEXT = "?ReqRef";
    public static String RC_TEXT = "?ReqCon";
    public static float[] LENGTHOFDASH = { 5.0F };

    public static int WIDE = 10;
    public static int first = 1;
    public static int firstq = 1;

    public static boolean same(Shape a, Shape b)
    {
        if (a.getShape() != b.getShape()) {
            return false;
        }
        if (a.getShape() == 0) {
            Rect tmpa = (Rect)a;
            Rect tmpb = (Rect)b;
            String s1 = tmpa.getText();
            String s2 = tmpb.getText();
            if ((tmpa.getState() != tmpb.getState()) || (!tmpa.getText().equals(tmpb.getText()))) {
                return false;
            }
        }
        if (a.getShape() == 1) {
            Oval tmpa = (Oval)a;
            Oval tmpb = (Oval)b;
            if (!tmpa.getText().equals(tmpb.getText())) {
                return false;
            }
        }
        return true;
    }
}
