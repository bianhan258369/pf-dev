package com.ECNU.controller;

import com.ECNU.bean.VersionInfo;
import com.ECNU.service.ClientService;
import com.ECNU.util.Cors;
import com.ECNU.util.GitUtil;
import com.ECNU.util.IPUtil;
import com.ECNU.util.Z3Util;
import com.github.jsonldjava.utils.Obj;
import net.sf.json.JSONObject;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/client")
public class ClientController extends Cors{
    @Autowired
    ClientService clientService;
    //private final String ROOTADDRESS = "E:/JavaProject/pf-dev/GitRepository/";
    private final String ROOTADDRESS = "/root/PF/Project/";

    private List<VersionInfo> searchVersionInfo(String branch){
        List<VersionInfo> versions = new ArrayList<VersionInfo>();
        try {
            GitUtil.gitCheckout(branch, ROOTADDRESS);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String command = "git reflog " + branch;
        File check = new File(ROOTADDRESS);

        List<String> vs = new ArrayList<String>();
        String commitVersion = null;
        try {
            Process p1 = Runtime.getRuntime().exec(command,null,check);
            BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            String s;

            while ((s = br.readLine()) != null) {
                if(s.indexOf("commit") != -1) {
                    commitVersion = s.split(" ")[0];
                    vs.add(commitVersion);
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for(String v : vs) {
            String versionCommand = "git show " + v;
            try {
                Process p2 = Runtime.getRuntime().exec(versionCommand,null,check);
                BufferedReader br = new BufferedReader(new InputStreamReader(p2.getInputStream()));
                String str = null;
                String time = null;
                String versionId = null;

                while ((str = br.readLine()) != null) {
                    if(str.startsWith("commit")) {
                        versionId = str.split(" ")[1].substring(0, 7);
                    }
                    if(str.startsWith("Date:")) {
                        str = str.substring(8);
                        time = str.substring(0, str.length() - 6);
                        str = br.readLine();
                        String value = br.readLine().split("0")[0];
                        if(value.indexOf("upload") != -1) {
                            if(value.indexOf("uploadproject") != -1) {
                                continue;
                            }else if(value.indexOf("uploadfile") != -1) {
                                if(versions.size() > 0) {
                                    if(versions.get(versions.size() - 1).getCommand().indexOf("uploadfile") != -1) {
                                        continue;
                                    }
                                }
                            }else {
                                continue;
                            }
                        }
                        if(value.indexOf("download") != -1) {
                            continue;
                        }
                        VersionInfo version = new VersionInfo();
                        version.setVersionId(versionId);
                        version.setTime(time);
                        version.setCommand(value);
                        versions.add(version);
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return versions;
    }


    private List<String> searchVersion(String branch) {
        // TODO Auto-generated method stub
        List<String> projectVersions = new ArrayList<String>();
        List<VersionInfo> versions = searchVersionInfo(branch);
        if(versions != null) {
            for(VersionInfo version: versions) {
                projectVersions.add(version.getTime());
            }
        }
        return projectVersions;
    }

    @CrossOrigin
    @RequestMapping("/upload")
    public String upload(@RequestParam("uploadedFiles") MultipartFile files,String folderName) throws Exception {
        String branch = folderName;
        File file = new File(ROOTADDRESS);
        if(!file.exists()){
            try {
                Repository repository = GitUtil.createRepository(ROOTADDRESS);
                GitUtil.RecordUploadProjAt("upload",ROOTADDRESS,ROOTADDRESS);
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
        if(!GitUtil.branchNameExist(branch,ROOTADDRESS)){
            GitUtil.createBranch(branch, ROOTADDRESS);
        }
        try {
            GitUtil.gitCheckout(branch,ROOTADDRESS);
            GitUtil.currentBranch(ROOTADDRESS);
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(ROOTADDRESS + files.getOriginalFilename()));
            outputStream.write(files.getBytes());
            outputStream.flush();
            outputStream.close();
            GitUtil.RecordUploadProjAt("uploadfile",ROOTADDRESS, ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Upload Success";
    }

    @CrossOrigin
    @GetMapping("/downloadMyCCSLFile")
    public void downloadMyCCSLFile() throws Exception {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        File repositoryFile = new File(ROOTADDRESS);
        if(!repositoryFile.exists()){
            try {
                Repository repository = GitUtil.createRepository(ROOTADDRESS);
                GitUtil.RecordUploadProjAt("upload",ROOTADDRESS,ROOTADDRESS);
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
        String filePath = ROOTADDRESS + "constraints.myccsl";
        System.out.println(filePath);
        File file = new File(filePath);
        if(file.exists()){
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.setHeader("Content-Disposition", "attachment;fileName="+ new String(filePath.getBytes("GB2312"),"ISO-8859-1"));
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @CrossOrigin
    @GetMapping("/downloadMyCCSLTool")
    public void downloadMyCCSLTool() throws IOException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        //String filePath = "MyCCSL/MyCCSL.zip";
        String filePath = "/root/PF/tool/MyCCSL.zip";
        File file = new File(filePath);
        if(file.exists()){
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.setHeader("Content-Disposition", "attachment;fileName="+ new String(filePath.getBytes("GB2312"),"ISO-8859-1"));
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @CrossOrigin
    @GetMapping("/saveConstraintsTxtAndXMLAndMyCCSL")
    public void saveConstraintsTxtAndXMLAndMyCCSL(String branch,String constraints,String addedConstraints) throws Exception {
        File file = new File(ROOTADDRESS);
        if(!file.exists()){
            try {
                Repository repository = GitUtil.createRepository(ROOTADDRESS);
                GitUtil.RecordUploadProjAt("upload",ROOTADDRESS,ROOTADDRESS);
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
        try {
            GitUtil.gitCheckout(branch, ROOTADDRESS);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ROOTADDRESS + "constraints.txt"));
            Document document = DocumentHelper.createDocument();
            Element root=document.addElement("AddedConstraints");
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            System.out.println(constraints);
            for(int i = 0;i < constraints.split("/").length;i++){
                bufferedWriter.write(constraints.split("/")[i]);
                bufferedWriter.newLine();
            }
            System.out.println(addedConstraints);
            for(int i = 0;i < addedConstraints.split("//").length;i++){
                String addedConstraint = addedConstraints.split("//")[i];
                if(addedConstraint.contains("TD")){
                    String oldIndex = addedConstraint.substring(2, addedConstraint.indexOf(":"));
                    String newIndex = ((Integer)(Integer.parseInt(addedConstraint.substring(2, addedConstraint.indexOf(":"))) - 1)).toString();
                    addedConstraint = addedConstraint.replaceFirst(oldIndex, newIndex);
                }
                Element node = root.addElement("constraint").addText(addedConstraint);
            }
            Writer out;
            try {
                out = new FileWriter(ROOTADDRESS + "addedConstraints.xml");
                XMLWriter writer = new XMLWriter(out, format);
                writer.write(document);
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            bufferedWriter.close();

            //修改txt中的addedConstraint
            String temp = "";
            File txtFile = new File(ROOTADDRESS + "constraints.txt");
            FileInputStream fis = new FileInputStream(txtFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            for (int j = 1; (temp = br.readLine()) != null; j++) {
                if(!temp.contains(":")){
                    buf = buf.append(temp);
                    buf = buf.append(System.getProperty("line.separator"));
                }
                else {
                    System.out.println(temp);
                    String extra = "";
                    String[] constraint = temp.substring(temp.indexOf(":") + 1).split(" ");
                    String from = "";
                    String to = "";
                    if(constraint[0].contains("int") && constraint[0].contains("state"))from = "int" + temp.substring(3 + temp.indexOf("int"),temp.indexOf("state"));
                    else from = constraint[0];
                    if(constraint[2].contains("int") && constraint[2].contains("state"))to = "int" + temp.substring(3 + temp.lastIndexOf("int"),temp.lastIndexOf("state"));
                    else to = constraint[2];
//                    String fromNum = temp.substring(3 + temp.indexOf("int"),temp.indexOf("state"));
//                    String toNum = temp.substring(3 + temp.lastIndexOf("int"),temp.lastIndexOf("state"));
                    String cons = constraint[1];
                    if(cons.equals("BoundedDiff") || cons.equals("Union")
                            || cons.equals("Inf") || cons.equals("Sup")
                            || cons.equals("Delay")){
                        extra = temp.split(" ")[3];
                        extra = extra.substring(0, extra.length() - 1);
                    }
                    if(cons.equals("BoundedDiff")){
                        buf = buf.append(from + ' ' + cons + " [" + extra + "] " + to + ";");
                    }
                    else if(cons.equals("Union") || cons.equals("Inf") || cons.equals("Sup")){
                        buf = buf.append(extra + " = " + from + ' ' + cons + " " + to + ";");
                    }
                    else if(cons.equals("Delay")){
                        buf = buf.append(from + " = " + to + ' ' + cons + ' ' + extra + ";");
                    }
                    else buf = buf.append(from + ' ' + cons + ' ' + to + ';');
                    buf = buf.append(System.getProperty("line.separator"));
                }
            }
            br.close();
            FileOutputStream fos = new FileOutputStream(txtFile);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();

            String txtFileName =  "constraints.txt";
            String smvFileName = "constraints";
            clientService.toMyCCSLFormat(txtFileName, smvFileName, ROOTADDRESS,5);
            GitUtil.RecordUploadProjAt("save",ROOTADDRESS, ".");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @CrossOrigin
    @GetMapping("/getPhenomenonList")
    public Object getPhenomenonList(int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getPhenomenonList(ROOTADDRESS + "Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getDiagramCount")
    public Object getDiagramCount() throws DocumentException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getDiagramCount(ROOTADDRESS + "Project.xml");
    }


    @CrossOrigin
    @GetMapping("/getRectList")
    public Object getRectList(int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getRectList(ROOTADDRESS + "Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getLineList")
    public Object getLineList(int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getLineList(ROOTADDRESS + "Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getOvalList")
    public Object getOvalList(int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getOvalList(ROOTADDRESS + "Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getScenarioList")
    public Object getScenarioList(int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioList(ROOTADDRESS + "Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getInteractionList")
    public Object getInteractionList(int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getInteractionList(ROOTADDRESS + "Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getSubProblemDiagramList")
    public Object getSubProblemDiagramList() throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getSubProblenDiagramList(ROOTADDRESS + "Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getScenarioDiagramList")
    public Object getScenarioDiagramList() throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioDiagramList(ROOTADDRESS + "Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getDiagramList")
    public Object getDiagramList() throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getDiagramList(ROOTADDRESS + "Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getOWLConstraintList")
    public Object getOWLConstraintList() throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String xmlPath = "asset/" + ip + "/" + "Project.xml";
//        String owlPath = "asset/" + ip + "/" + "environment.owl";
        return clientService.getOWLConstraints(ROOTADDRESS + "Project.xml", ROOTADDRESS + "environment.owl");
    }

    @CrossOrigin
    @GetMapping("/getAllPhenomenonList")
    public Object getAllPhenomenonList() throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getAllPhenomenonList(ROOTADDRESS + "Project.xml");
    }
    @CrossOrigin
    @GetMapping("/getAllReferenceList")
    public Object getAllReferenceList() throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getAllReferenceList(ROOTADDRESS + "Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getScenarioDiagramByDomain")
    public Object getScenarioDiagramByDomain(int index, String domainText) throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioDiagramByDomain(ROOTADDRESS + "Project.xml", index, domainText);
    }

    @CrossOrigin
    @GetMapping("/canAddConstraint")
    public Object canAddConstraint(int index, String from, String cons, String to, String boundedFrom, String boundedTo) throws DocumentException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.canAddConstraint(ROOTADDRESS + "Project.xml", index, from, to, cons ,boundedFrom, boundedTo);
    }

    @CrossOrigin
    @GetMapping("/ruleBasedCheck")
    public String ruleBasedCheck() throws DocumentException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.ruleBasedCheck(ROOTADDRESS + "Project.xml");
    }

    @CrossOrigin
    @GetMapping("/loadConstraintsXML")
    public String loadConstraintsXML() throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "addedConstraints.xml";
        return clientService.loadConstraintsXML(ROOTADDRESS + "addedConstraints.xml");
    }

    @CrossOrigin
    @GetMapping("/z3Check")
    public Object z3Check(int timeout, int b, int pb, boolean dl, boolean p) throws Exception{
        JSONObject resultJson = new JSONObject();
        Z3Util z3Util = new Z3Util(timeout, ROOTADDRESS + "constraints.myccsl",b, pb, dl, p);
        z3Util.exportSMT(ROOTADDRESS);
        //String command = "z3 constraints.smt2";
        String command = "z3 " + ROOTADDRESS + "constraints.smt2";
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedInputStream bis = new BufferedInputStream(
                    process.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
            String line;
            while ((line = br.readLine()) != null) {
                result = result + line + " ";
            }
            process.waitFor();
            if (process.exitValue() != 0) {
                System.out.println("error!");
            }

            bis.close();
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(result);
        if(result.contains("unsat"))result = "unsat";
        else result = "sat";
        resultJson.put("result",result);
        return resultJson;
    }

    @CrossOrigin
    @GetMapping("/getRootAddress")
    public Object getRootAddress() throws Exception{
        JSONObject result = new JSONObject();
        result.put("rootaddress",ROOTADDRESS);
        return result;
    }

    @CrossOrigin
    @GetMapping("/showBranchList")
    public Object showBranchList() throws IOException, GitAPIException {
        JSONObject result = new JSONObject();
        Map map = GitUtil.gitAllBranch(ROOTADDRESS);
        Iterator it = map.keySet().iterator();
        while(it.hasNext()){
            String branch = it.next().toString();
            branch = branch.substring(branch.lastIndexOf("/") + 1);
            if(!branch.equals("master")) result.accumulate("branchlist",branch);
        }
        return result;
    }

//    @CrossOrigin
//    @GetMapping("/showFolderList")
//    public Object showFolderList(String branch) throws IOException, GitAPIException {
//        JSONObject result = new JSONObject();
//        try{
//            GitUtil.gitCheckout(branch,ROOTADDRESS);
//            File root = new File(ROOTADDRESS);
//            File[] folders = root.listFiles();
//            for(int i = 0;i < folders.length;i++){
//                if(folders[i].isDirectory()){
//                    if(!folders[i].getName().equals(".git")) result.accumulate("folderlist",folders[i].getName());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    @CrossOrigin
    @GetMapping("/showVersionList")
    public Object showVersionList(String branch) throws IOException, GitAPIException {
        JSONObject result = new JSONObject();
        try{
            GitUtil.gitCheckout(branch,ROOTADDRESS);
            List<String> versions = searchVersion(branch);
            System.out.println(versions.size());
            for(int i = 0;i < versions.size();i++){
                result.accumulate("versionlist",versions.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @CrossOrigin
    @GetMapping("/gitCheckout")
    public Object gitCheckout(String branch) throws Exception {
        JSONObject result = new JSONObject();
        GitUtil.gitCheckout(branch, ROOTADDRESS);
        result.put("state","Success");
        return result;
    }

    @CrossOrigin
    @GetMapping("/getCurrentBranch")
    public Object getCurrentBranch() throws Exception{
        JSONObject result = new JSONObject();
        String res = GitUtil.currentBranch(ROOTADDRESS);
        result.put("branch",res);
        return result;
    }


    @CrossOrigin
    @GetMapping("/gitRollBack")
    public Object gitRollBack(String branch, String version) throws Exception{
        JSONObject result = new JSONObject();
        List<VersionInfo> versions = searchVersionInfo(branch);
        try {
            GitUtil.gitCheckout(branch, ROOTADDRESS);
            GitUtil.rollback(branch, ROOTADDRESS, version, versions);
            result.put("state","Success");
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @CrossOrigin
    @GetMapping("/downloadCaseTool")
    public void downloadProgression() throws IOException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        //String filePath = "MyCCSL/MyCCSL.zip";
        String filePath = "/root/PF/tool/Case_Tool.zip";
        File file = new File(filePath);
        if(file.exists()){
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.setHeader("Content-Disposition", "attachment;fileName="+ new String(filePath.getBytes("GB2312"),"ISO-8859-1"));
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @CrossOrigin
    @GetMapping("/downloadProjects")
    public void downloadProjects() throws IOException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        //String filePath = "MyCCSL/MyCCSL.zip";
        String filePath = "/root/PF/Download/Projects4Download/Projects.zip";
        File file = new File(filePath);
        if(file.exists()){
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.setHeader("Content-Disposition", "attachment;fileName="+ new String(filePath.getBytes("GB2312"),"ISO-8859-1"));
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}