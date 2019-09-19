package com.ECNU.FormatTransfer;

import java.io.*;
import java.util.Iterator;

public class ToMyCCSLFormat extends ToCommonFormat{
    private int count;
    // MyCCSL clock steps
    private int stepBound = 10;
    // MyCCSL Time up Bound
    private int timeUpBound = 1000000;

    public ToMyCCSLFormat(String fileNameSrc, String fileNameDest, String path, int stepBound) {
        super(fileNameSrc,fileNameDest,path);
        this.stepBound = stepBound;
        writeAll();
    }

    @Override
    public void writeAll() {
        File fileDest = new File(path + fileSeparator + this.fileNameDest + ".myccsl");
        writeTopSection(fileDest);
    }

    @Override
    public void beginWrite(Writer fw) throws IOException {
        fw.write("");
    }

    @Override
    public void judgeAndWrite(Writer fw) throws Exception {
        String[] splitList = null;
        String kw = "";
        for(Iterator<String> iter = clockConstrans.iterator(); iter.hasNext();) {
            String temp = iter.next();
            System.out.println(temp);
            splitList = temp.split("\\s+");
            for(int i = 0;i < splitList.length;i++) System.out.println(splitList[i]);
            kw = judgeKw(splitList);
            if (kw.equals("Union")) {
                fw.write(splitList[0] + "=");
                fw.write(splitList[2] + "+" + splitList[4] + lineSeparator);
            }
            else if(kw.equals("Inf")){
                fw.write(splitList[0] + "=");
                fw.write(splitList[2] + "∧" + splitList[4] + lineSeparator);
            }
            else if(kw.equals("Sup")){
                fw.write(splitList[0] + "=");
                fw.write(splitList[2] + "∨" + splitList[4] + lineSeparator);
            }
            else if (kw.equals("BoundedDiff")) {
                String tmpStr = splitList[2].substring(1,splitList[2].length()-1);
                String downBound = "0";
                String upBound = "";
                if (tmpStr.indexOf(',') != -1) {
                    String[] tmpStrList = tmpStr.split(",");
                    downBound = tmpStrList[0];
                    upBound = tmpStrList[1];
                }else {
                    upBound = tmpStr;
                }
                fw.write(splitList[0] + "<" + splitList[3] + lineSeparator);
                fw.write("tmp" + count + "=" + splitList[0] + "$" + upBound);
                fw.write(" on idealClcok" + lineSeparator);
                fw.write(splitList[3] + "≤" + "tmp" + count + lineSeparator);
                count++;
            }else if (kw.equals("StrictPre")) {
                fw.write(splitList[0] + "<" + splitList[2] + lineSeparator);
            }else if (kw.equals("Coincidence")) {
                fw.write(splitList[0] + "==" + splitList[2] + lineSeparator);
            }else if(kw.equals("Alternate")) {
                fw.write(splitList[0] + "<" + splitList[2] + lineSeparator);
                fw.write("tmp" + count + "=" + splitList[0] + "$1");
                fw.write(lineSeparator + splitList[2] + "<tmp" + count);
                fw.write(lineSeparator);
                count++;
            }else if(kw.equals("Exclusion")) {
                fw.write(splitList[0] + "#" + splitList[2] + lineSeparator);
            }else if(kw.equals("Cause")) {
                fw.write(splitList[0] + "≤" + splitList[2] + lineSeparator);
            }
        }
    }

    public static void main(String[] args) {
        new ToMyCCSLFormat("test2.txt","test2"
                ,"NuSMV",15);
    }
}
