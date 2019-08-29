package com.ECNU.FormatTransfer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunNuSMV extends ExecCommand {
    private File fileOutput;
    private Writer fileWriter;
    private String fileNameSrc,fileNameDest,path;
    // get System newLine
    private static String lineSeparator = System.getProperty("line.separator");
    // get System fileSeparator
    private static String fileSeparator = System.getProperty("file.separator");
    private List<String> info;
    // get Simple NuSMV OutPut.
    private Map<String,String> SimpleOutPutInfo;

    public RunNuSMV(String fileNameSrc, String fileNameDest,String path) {
        this.info = new ArrayList<>();
        this.fileNameSrc = fileNameSrc;
        this.fileNameDest = fileNameDest;
        this.path = path;
        try {
            this.fileOutput = new File(this.path + fileSeparator + this.fileNameDest + ".info");
            if (!fileOutput.exists())
                fileOutput.createNewFile();
            this.fileWriter = new BufferedWriter(new FileWriter(this.fileOutput));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runSMV() {
        try {
            this.exec(new String[] {"asset" + fileSeparator + "DONOTMOVE\\NuSMV.exe",
                    path + fileSeparator + fileNameSrc},path);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void lineHandler(String lineStr) {
       if (lineStr.equals(""))
           return;
        try {
            info.add(lineStr);
            fileWriter.write(lineStr + lineSeparator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String,String> getSimpleOutPutInfo() {
        return this.SimpleOutPutInfo;
    }

    public String getOutputInfo() {
        StringBuilder sb = new StringBuilder("");
        int firstOutPutIndex = info.indexOf("<Output>");
        int lastOutPutIndex = info.indexOf("</Output>");
        for (int i=firstOutPutIndex+1;i<lastOutPutIndex;i++) {
            String tempInfo = info.get(i);
            if(tempInfo.startsWith("***"))
                continue;
            if(tempInfo.startsWith("-- specification")) {
                String[] tempList = tempInfo.split(" ");
                String lastElement = tempList[tempList.length-1];
                StringBuilder stringBuilder = new StringBuilder();
                this.SimpleOutPutInfo = new HashMap<String,String>();
                if (lastElement.equals("true")) {
                    stringBuilder.append("Consistency Verified by NuSMV 2.6.0 Timing Requirements in "
                            + fileNameSrc.substring(0,fileNameSrc.lastIndexOf('.')) + " are consistent.");
                    this.SimpleOutPutInfo.put("true",stringBuilder.toString());
                } else {
                    stringBuilder.append("Consistency Verified by NuSMV 2.6.0 Timing Requirements in "
                            + fileNameSrc.substring(0,fileNameSrc.lastIndexOf('.')) + " are inconsistent.");
                    this.SimpleOutPutInfo.put("false",stringBuilder.toString());
                }
            }
            sb.append(tempInfo + lineSeparator);
        }
        return sb.toString();
    }

    public String getErrorOutPutInfo() {
        StringBuilder sb = new StringBuilder("");
        int firstOutPutIndex = info.indexOf("<Error>");
        int lastOutPutIndex = info.indexOf("</Error>");
        if (lastOutPutIndex - firstOutPutIndex == 1) {
            return "";
        } else {
            for(int i=firstOutPutIndex+1;i<lastOutPutIndex;i++) {
                sb.append(info.get(i) + lineSeparator);
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
       RunNuSMV run = new RunNuSMV("ISCCSL.smv","ISCCSLSMVResult","NuSMV"); //"test.smv","testSMVResult","NuSMV"
       run.runSMV();
       // getOutputInfo() must be invoked before getSimpleOutPutInfo()
       System.out.println(run.getOutputInfo() + " --> trueOrFalse");
       System.out.println(run.getSimpleOutPutInfo() + " --> map");
       System.out.println(run.getErrorOutPutInfo() + " --> error");
    }
}
