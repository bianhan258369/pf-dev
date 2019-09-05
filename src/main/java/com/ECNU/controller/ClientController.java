package com.ECNU.controller;

import com.ECNU.service.ClientService;
import com.ECNU.util.Cors;
import com.ECNU.util.GitUtil;
import com.ECNU.util.IPUtil;
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
    private final String ROOTADDRESS = "E:\\JavaProject\\pf-dev\\GitRepository\\";

    @CrossOrigin
    @RequestMapping("/upload")
    public String upload(@RequestParam("uploadedFiles") MultipartFile files) throws Exception {
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
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(ROOTADDRESS + files.getOriginalFilename()));
            outputStream.write(files.getBytes());
            outputStream.flush();
            outputStream.close();
            GitUtil.RecordUploadProjAt("upload",ROOTADDRESS, ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Upload Success";
    }

    @CrossOrigin
    @GetMapping("/downloadMyCCSLFile")
    public void download() throws Exception {
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
        String filePath = ROOTADDRESS + "constraints.myccsl";
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
        String filePath = "MyCCSL/MyCCSL.zip";
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
                System.out.print("生成XML文件成功");
            } catch (IOException e){
                System.out.print("生成XML文件失败");
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
    @GetMapping("/gitChange")
    public Object gitChange(String branch) throws Exception {
        JSONObject result = new JSONObject();
        GitUtil.gitCheckout(branch, ROOTADDRESS);
        result.put("state","Success");
        return result;
    }
}
