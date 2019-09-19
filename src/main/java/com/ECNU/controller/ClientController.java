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
    private final String ROOTADDRESS = "E:/JavaProject/pf-dev/GitRepository/";
    //private final String ROOTADDRESS = "/root/PF/Project/";

    private List<VersionInfo> searchVersionInfo(String project, String branch){
        List<VersionInfo> versions = new ArrayList<VersionInfo>();
        List<String> vs = new ArrayList<String>();
        System.out.println(project);
        String command = "git reflog " + project + "/";
        String dir = ROOTADDRESS;
        File check = new File(dir);
//  String newVersion = null;
        String commitVersion = null;
        try {
            Process p1 = Runtime.getRuntime().exec(command,null,check);
            BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            String s;

            while ((s = br.readLine()) != null) {
                System.out.println(s + "--------------------");
                if(s.indexOf("commit") != -1) {
//           newVersion = s.substring(0, 7);
                    commitVersion = s.split(" ")[0];
                    vs.add(commitVersion);
//           break;
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
                        System.out.println(value);
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

    private List<String> searchVersion(String project, String branch) {
        // TODO Auto-generated method stub
        List<String> projectVersions = new ArrayList<String>();
        List<VersionInfo> versions = searchVersionInfo(project, branch);
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
        String branch = "test";
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
            File folder = new File(ROOTADDRESS + folderName);
            if(!folder.exists()){
                folder.mkdir();
                GitUtil.RecordUploadProjAt("uploadproject",ROOTADDRESS, ".");
            }
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(ROOTADDRESS + folderName + "/" + files.getOriginalFilename()));
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
    public void download(String path) throws Exception {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        String branch = "test";
        File repositoryFile = new File(ROOTADDRESS);
        if(!repositoryFile.exists()){
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
        } catch (Exception e){
            e.printStackTrace();
        }
        String filePath = path + "/constraints.myccsl";
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
    public void downloadMyCCSL() throws IOException {
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
    public void saveConstraintsTxtAndXMLAndMyCCSL(String path, String constraints,String addedConstraints) throws Exception {
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
            GitUtil.currentBranch(ROOTADDRESS);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path + "/constraints.txt"));
            Document document = DocumentHelper.createDocument();
            Element root=document.addElement("AddedConstraints");
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            for(int i = 0;i < constraints.split(",").length;i++){
                bufferedWriter.write(constraints.split(",")[i]);
                bufferedWriter.newLine();
            }
            for(int i = 0;i < addedConstraints.split(",").length;i++){
                String addedConstraint = addedConstraints.split(",")[i];
                if(addedConstraint.contains("TD")){
                    String oldIndex = addedConstraint.substring(2, addedConstraint.indexOf(":"));
                    String newIndex = ((Integer)(Integer.parseInt(addedConstraint.substring(2, addedConstraint.indexOf(":"))) - 1)).toString();
                    addedConstraint = addedConstraint.replaceFirst(oldIndex, newIndex);
                }
                Element node = root.addElement("constraint").addText(addedConstraint);
            }
            Writer out;
            try {
                out = new FileWriter(path + "/addedConstraints.xml");
                XMLWriter writer = new XMLWriter(out, format);
                writer.write(document);
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            bufferedWriter.close();

            //修改txt中的addedConstraint
            String temp = "";
            File txtFile = new File(path + "/constraints.txt");
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
                    String fromNum = temp.substring(3 + temp.indexOf("int"),temp.indexOf("state"));
                    String toNum = temp.substring(3 + temp.lastIndexOf("int"),temp.lastIndexOf("state"));
                    String cons = temp.substring(temp.indexOf(" ") + 1, temp.lastIndexOf(" "));
                    buf = buf.append("int" + fromNum + ' ' + cons + ' ' + "int" + toNum + ';');
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
            clientService.toMyCCSLFormat(txtFileName, smvFileName, path,5);
            GitUtil.RecordUploadProjAt("upload",ROOTADDRESS, ".");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @CrossOrigin
    @GetMapping("/getPhenomenonList")
    public Object getPhenomenonList(String path, int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getPhenomenonList(path + "/Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getDiagramCount")
    public Object getDiagramCount(String path) throws DocumentException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getDiagramCount(path + "/Project.xml");
    }


    @CrossOrigin
    @GetMapping("/getRectList")
    public Object getRectList(String path, int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getRectList(path + "/Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getLineList")
    public Object getLineList(String path, int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getLineList(path + "/Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getOvalList")
    public Object getOvalList(String path, int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getOvalList(path + "/Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getScenarioList")
    public Object getScenarioList(String path, int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioList(path + "/Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getInteractionList")
    public Object getInteractionList(String path, int index) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getInteractionList(path + "/Project.xml", index);
    }

    @CrossOrigin
    @GetMapping("/getSubProblemDiagramList")
    public Object getSubProblemDiagramList(String path) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getSubProblenDiagramList(path + "/Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getScenarioDiagramList")
    public Object getScenarioDiagramList(String path) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioDiagramList(path + "/Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getDiagramList")
    public Object getDiagramList(String path) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getDiagramList(path + "/Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getOWLConstraintList")
    public Object getOWLConstraintList(String path) throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String xmlPath = "asset/" + ip + "/" + "Project.xml";
//        String owlPath = "asset/" + ip + "/" + "environment.owl";
        return clientService.getOWLConstraints(path + "/Project.xml", path + "/environment.owl");
    }

    @CrossOrigin
    @GetMapping("/getAllPhenomenonList")
    public Object getAllPhenomenonList(String path) throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getAllPhenomenonList(path + "/Project.xml");
    }
    @CrossOrigin
    @GetMapping("/getAllReferenceList")
    public Object getAllReferenceList(String path) throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getAllReferenceList(path + "/Project.xml");
    }

    @CrossOrigin
    @GetMapping("/getScenarioDiagramByDomain")
    public Object getScenarioDiagramByDomain(String path, int index, String domainText) throws DocumentException, FileNotFoundException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioDiagramByDomain(path + "/Project.xml", index, domainText);
    }

    @CrossOrigin
    @GetMapping("/canAddConstraint")
    public Object canAddConstraint(String path, int index, String from, String cons, String to, String boundedFrom, String boundedTo) throws DocumentException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.canAddConstraint(path + "/Project.xml", index, from, to, cons ,boundedFrom, boundedTo);
    }

    @CrossOrigin
    @GetMapping("/ruleBasedCheck")
    public String ruleBasedCheck(String path) throws DocumentException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.ruleBasedCheck(path + "/Project.xml");
    }

    @CrossOrigin
    @GetMapping("/loadConstraintsXML")
    public String loadConstraintsXML(String path) throws DocumentException{
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        String path = "asset/" + ip + "/" + "addedConstraints.xml";
        return clientService.loadConstraintsXML(path + "/addedConstraints.xml");
    }

    @CrossOrigin
    @GetMapping("/z3Check")
    public Object z3Check(String path, int timeout, int b, int pb, boolean dl, boolean p) throws Exception{
        JSONObject resultJson = new JSONObject();
        Z3Util z3Util = new Z3Util(timeout, path + "/constraints.myccsl",b, pb, dl, p);
        z3Util.exportSMT(path);
        String command = "z3 constraints.smt2";
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
        result = result.substring(0,result.indexOf(" "));
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
            result.accumulate("branchlist",branch);
        }
        return result;
    }

    @CrossOrigin
    @GetMapping("/showFolderList")
    public Object showFolderList(String branch) throws IOException, GitAPIException {
        JSONObject result = new JSONObject();
        try{
            GitUtil.gitCheckout(branch,ROOTADDRESS);
            File root = new File(ROOTADDRESS);
            File[] folders = root.listFiles();
            for(int i = 0;i < folders.length;i++){
                if(folders[i].isDirectory()){
                    if(!folders[i].getName().equals(".git")) result.accumulate("folderlist",folders[i].getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @CrossOrigin
    @GetMapping("/showVersionList")
    public Object showVersionList(String branch, String folderName) throws IOException, GitAPIException {
        JSONObject result = new JSONObject();
        try{
            GitUtil.gitCheckout(branch,ROOTADDRESS);
            List<String> versions = searchVersion(folderName, branch);
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
    @GetMapping("/gitChange")
    public Object gitChange(String branch) throws Exception {
        JSONObject result = new JSONObject();
        GitUtil.gitCheckout(branch, ROOTADDRESS);
        result.put("state","Success");
        return result;
    }


    @CrossOrigin
    @GetMapping("/gitRollBack")
    public Object gitRollBack(String branch, String folderName, String version) throws Exception{
        JSONObject result = new JSONObject();
        List<VersionInfo> versions = searchVersionInfo(folderName, branch);
        try {
            GitUtil.gitCheckout(branch, ROOTADDRESS);
            GitUtil.rollback(branch, ROOTADDRESS, folderName, version, versions);
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
        String filePath = "/root/PF/tool/CaseTool.zip";
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