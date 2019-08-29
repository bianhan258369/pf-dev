package com.ECNU.FormatTransfer;

import java.io.*;
import java.util.*;

public class ToNuSMVFormat extends ToCommonFormat {
    // give StrictPre and Cause Bound
    private int bound = 5;
    // consistent formular
    private String consistentFormular;

    public ToNuSMVFormat(String fileNameSrc, String fileNameDest, String path, int bound) {
        super(fileNameSrc,fileNameDest,path);
        this.bound = bound;
        writeAll();
    }

    @Override
    public void writeAll() {
        File fileDest = new File(path + fileSeparator + this.fileNameDest + ".smv");
        writeTopSection(fileDest);
        // append File "next.txt" content
        File fileAppend = new File("NuSMV/DONOTMOVE/next.txt");
        // the file needed to be appended
        // File fileDest = new File(path + fileSeparator + this.fileNameDest + ".smv");
        writeBottomSection(fileAppend,fileDest);
    }

    @Override
    public void beginWrite(Writer fw) throws IOException {
        fw.write("MODULE main" + lineSeparator);
        fw.write(lineSeparator);
        fw.write("VAR" + lineSeparator);
        for(String clock:clocks) {
            fw.write(clock + ":boolean;" + lineSeparator);
        }
        fw.write(lineSeparator);
    }

    @Override
    public void judgeAndWrite(Writer fw) throws Exception {
        String[] splitList = null;
        String kw = "";
        for(Iterator<String> iter = clockConstrans.iterator(); iter.hasNext();) {
            String temp = iter.next();
            // To escape(转义)
            // "\s" you need two backslashes(反斜线) since the backslash is a special character in Java strings,
            // and needs to be escaped itself.
            // So, it becomes: "\\s"
            splitList = temp.split("\\s+");
            kw = judgeKw(splitList);
            fw.write("ctr" + count);
            if (kw.equals("Union")) {
                fw.write(":unionn(");
                fw.write(splitList[0] + "," + splitList[2] + "," +
                        splitList[3].substring(1,splitList[3].length()-1));
                fw.write(");" + lineSeparator);
                count++;
            }else if (kw.equals("Boundeddiff")) {
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
                fw.write(":boundeddiff(");
                fw.write(splitList[0] + "," + splitList[3] + "," + downBound + "," + upBound);
                fw.write(");" + lineSeparator);
                count++;
            }else if (kw.equals("StrictPre")) {
                fw.write(":strictpre(");
                fw.write(splitList[0] + "," + splitList[2] + ",");
                fw.write(bound + ");" + lineSeparator);
                count++;
            }else if (kw.equals("Coincidence")) {
                fw.write(":coincidence(");
                fw.write(splitList[0] + "," + splitList[2] + ");" + lineSeparator);
                count++;
            }else if(kw.equals("Alternate")) {
                fw.write(":Alter(");
                fw.write(splitList[0] + "," + splitList[2] + ");" + lineSeparator);
                count++;
            }else if(kw.equals("Exclusion")) {
                fw.write(":exclusion(");
                fw.write(splitList[0] + "," + splitList[2] + ");" + lineSeparator);
                count++;
            }else if(kw.equals("Cause")) {
                fw.write(":cause(");
                fw.write(splitList[0] + "," + splitList[2] + ",");
                fw.write(bound + ");" + lineSeparator);
                count++;
            }
        }

        // init All Clock Phenomena "false"
        fw.write(lineSeparator + "ASSIGN" + lineSeparator);
        for(Iterator<String> iter=clocks.iterator();iter.hasNext();) {
            String temp = iter.next();
            fw.write("init(" + temp + ") := FALSE;" + lineSeparator);
        }

        // write consistent formula
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("CTLSPEC ( ! EF AG !( ");
        for(String clock:clocks) {
            stringBuilder.append(clock + "|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append(" ) )");
        consistentFormular = stringBuilder.toString();
        fw.write(consistentFormular + lineSeparator + lineSeparator);
    }

    public String getConsistentFormular() {
        return consistentFormular;
    }

    public static void main(String[] args) {
        new ToNuSMVFormat("ISCCSL.txt","ISCCSL"
                ,"NuSMV",5);
    }
}
