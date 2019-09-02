package com.ECNU.controller;

import com.ECNU.bean.*;
import com.ECNU.service.ClientService;
import com.ECNU.util.Cors;
import com.ECNU.util.IPUtil;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.jsonldjava.utils.Obj;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/client")
public class ClientController extends Cors{
    @Autowired
    ClientService clientService;

    @CrossOrigin
    @RequestMapping("/upload")
    public String upload(@RequestParam("uploadedFiles") MultipartFile file) throws IOException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String filePath = file.getOriginalFilename(); // 获取文件的名称
        File assetFile = new File("asset");
        if(!assetFile.exists()) assetFile.mkdir();
        File tmpFile = new File("asset/" + ip);
        if(!tmpFile.exists()) tmpFile.mkdir();
        filePath = "asset/" + ip + "/" + filePath; // 这是文件的保存路径，如果不设置就会保存到项目的根目录
        System.out.println(filePath);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));

        outputStream.write(file.getBytes());
        outputStream.flush();
        outputStream.close();
        return "Upload Success";
    }

    @CrossOrigin
    @GetMapping("/downloadMyCCSLFile")
    public void download() throws IOException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        File assetFile = new File("asset");
        if(!assetFile.exists()) assetFile.mkdir();
        File tmpFile = new File("asset/" + ip);
        if(!tmpFile.exists()) tmpFile.mkdir();
        String filePath = "asset/" + ip + "/" + "constraints.myccsl";
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
        File assetFile = new File("asset");
        if(!assetFile.exists()) assetFile.mkdir();
        String filePath = "asset/MyCCSL.zip";
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
    public void saveConstraintsTxtAndXMLAndMyCCSL(String path, String constraints,String addedConstraints) throws DocumentException, IOException {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String ip = IPUtil.getIpAddress(request);
//        ip = ip.replace(':','-');
//        File assetFile = new File("asset");
//        if(!assetFile.exists()) assetFile.mkdir();
//        File tmpFile = new File("asset/" + ip);
//        if(!tmpFile.exists()) tmpFile.mkdir();
//        String filePath = "asset/" + ip + "/" + "constraints.txt";
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
        String txtFileName =  "constraints.txt";
        String smvFileName = "constraints";
        clientService.toMyCCSLFormat(txtFileName, smvFileName, path,5);
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
        System.out.println(path + "/addedConstraints.xml");
        return clientService.loadConstraintsXML(path + "/addedConstraints.xml");
    }

    @CrossOrigin
    @GetMapping("/showServerFiles")
    public Object showServerFiles(String folderPath){
        JSONObject result = new JSONObject();
        System.out.println(folderPath);
        if(folderPath == null || folderPath.trim().equals("")){
            result.put("filelist","asset");
        }
        else{
            File file = new File(folderPath);
            if(file.isDirectory()){
                String[] filelist = file.list();
                result.put("filelist",filelist);
            }
            else{
                result.put("filelist","noDir");
            }
        }
        return result;
    }
}
