package com.ECNU.FormatTransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ExecCommand {
    private static final Log log = LogFactory.getLog(ExecCommand.class);

    /**
     * Description: Execute the command in the single thread。
     */
    public void exec(String command) throws IOException {
        exec(command, null, null);
    }

    /**
     * @Description: Execute the command in the specified path.
     */
    public void exec(String command, String workpath) throws IOException {
        exec(command, null, workpath);
    }

    /**
     * @Description: Execute the command in the specified path and environment.
     */
    public void exec(String command, String[] envp, String workpath) throws IOException {
        InputStream is = null;
        InputStream isErr = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        try {
            File dir = null;
            if (null != workpath)
                dir = new File(workpath);
            log.info("【COMMAND】>>>：" + command);
            Process p = Runtime.getRuntime().exec(command, envp, dir);

            is = p.getInputStream();
            in = new BufferedInputStream(is);
            br = new BufferedReader(new InputStreamReader(in));
            String ss = "";
            lineHandler("<Output>");
            while ((ss = br.readLine()) != null) {
                lineHandler(ss);
            }
            lineHandler("</Output>\n");

            isErr = p.getErrorStream();
            in = new BufferedInputStream(isErr);
            br = new BufferedReader(new InputStreamReader(in));
            lineHandler("<Error>");
            while ((ss = br.readLine()) != null) {
                lineHandler(ss);
            }
            lineHandler("</Error>");
        } finally {
            if (null != br)
                br.close();
            if (null != in)
                in.close();
            if (null != is)
                is.close();
        }
    }

    /**
     * @Description: Execute the command with parameters。
     * @param commands
     *            Include the command and parameters。Example：new
     *            String[]{"/home/user1/test.sh","arg1","arg2"};
     */
    public void exec(String[] commands) throws IOException {
        exec(commands, null, null);
    }

    public void exec(String[] commands, String workpath) throws IOException {
        exec(commands, null, workpath);
    }

    public void exec(String[] commands, String[] envp, String workpath) throws IOException {
        InputStream is = null;
        InputStream isErr = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        try {
            File dir = null;
            if (null != workpath)
                dir = new File(workpath);
            log.info("【COMMAND】>>>：" + getCommandString(commands));
            Process p = Runtime.getRuntime().exec(commands, envp, dir);

            is = p.getInputStream();
            in = new BufferedInputStream(is);
            br = new BufferedReader(new InputStreamReader(in));
            String ss = "";
            lineHandler("<Output>");
            while ((ss = br.readLine()) != null) {
                lineHandler(ss);
            }
            lineHandler("</Output>");

            isErr = p.getErrorStream();
            in = new BufferedInputStream(isErr);
            br = new BufferedReader(new InputStreamReader(in));
            lineHandler("<Error>");
            while ((ss = br.readLine()) != null) {
                lineHandler(ss);
            }
            lineHandler("</Error>");
        } finally {
            if (null != br)
                br.close();
            if (null != in)
                in.close();
            if (null != is)
                is.close();
        }
    }

    private String getCommandString(String[] commands) {
        StringBuilder sb = new StringBuilder();
        for (String command : commands) {
            sb.append(command);
            sb.append(" ");
        }
        return sb.toString();
    }

    protected abstract void lineHandler(String lineStr);
}

