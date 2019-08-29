package com.ECNU.FormatTransfer;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class ToCommonFormat {
    protected int count;
    protected String path;
    protected String fileNameSrc;
    protected String fileNameDest;
    // get System newLine
    protected static String lineSeparator = System.getProperty("line.separator");
    // get System fileSeparator
    protected static String fileSeparator = System.getProperty("file.separator");
    // process the ToNuSMVFormat/ToMyCCLFormat Exception
    protected static StringBuilder exception = new StringBuilder();
    protected static final String[] str = {"Union","Boundeddiff","StrictPre","Coincidence","Alternate",
            "Exclusion","Cause" };
    // All clock keyWordsString to ArrayList<String>
    protected static final List<String> clockKws = Arrays.asList(str);
    // Store All Clock Phenomena
    protected List<String> clocks;
    // Store All Clock Constraints
    protected List<String> clockConstrans;

    protected ToCommonFormat(String fileNameSrc, String fileNameDest, String path) {
        this.clocks = new ArrayList<>();
        this.clockConstrans = new ArrayList<>();
        this.path = path;
        this.count = 1;
        this.fileNameSrc = fileNameSrc;
        this.fileNameDest = fileNameDest;
        initClock();
    }

    protected void initClock() {
        // find the last semicolon of all phenomena
        boolean isLastSemicoPhe = false;
        try(BufferedReader br = new BufferedReader(new FileReader(path + fileSeparator +fileNameSrc));) {
            String read;
            while((read = br.readLine()) != null) {
                int index = read.lastIndexOf(';');
                // store all clock phenomena,except the clock with Semicolon
                if (index == -1 && !isLastSemicoPhe) {
                    clocks.add(read);
                } // store the clock with Semicolon
                else if (index != -1 && !isLastSemicoPhe) {
                    String readSub = read.substring(0,read.length()-1);
                    clocks.add(readSub);
                    isLastSemicoPhe = true;
                } // store all clock constraints,including the last Semicolon
                else if (index != -1 && isLastSemicoPhe) {
                    // remove the last Semicolon of a line
                    read = read.substring(0,read.length()-1);
                    clockConstrans.add(read);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("找不到文件...");
        } catch (IOException e) {
            System.out.println("读取文件失败...");
        }
    }

    protected abstract void writeAll();

    protected abstract void beginWrite(Writer fw) throws IOException;

    protected void writeTopSection(File fileDest) {
        Writer fileWriter = null;
        try {
            if (!fileDest.exists()) {
                fileDest.createNewFile();
            }
            fileWriter = new BufferedWriter(new FileWriter(fileDest));
            beginWrite(fileWriter);
            judgeAndWrite(fileWriter);
        } catch (Exception e) {
            //e.printStackTrace();
            exception.append("There are problems in " + fileNameSrc + " !!!");
            e.printStackTrace();
        } finally {
            try {
                if (null != fileWriter)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void judgeAndWrite(Writer fw) throws Exception;

    protected String judgeKw(String[] temp) {
        for(String s:clockKws) {
            for(String str:temp) {
                if (str.equals(s)) {
                    return s;
                }
            }
        }
        return "";
    }

    protected void writeBottomSection(File fileAppend, File fileDest) {
        // read by line and store in a List
        List<String> msgs = null;
        try {
            msgs = FileUtils.readLines(fileAppend,"GBK");
            for(Iterator<String> iter = msgs.iterator(); iter.hasNext();) {
                String msg = iter.next();
                // the last argument represent "append" mode is "true"
                FileUtils.writeStringToFile(fileDest,
                        msg + lineSeparator,"GBK",true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getException() {
        return exception.toString();
    }

    public static void clearException() {
        // clear StringBuilder object "exception"
        exception.delete(0,exception.length());
    }
}
