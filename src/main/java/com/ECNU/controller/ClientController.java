package com.ECNU.controller;

import com.ECNU.bean.*;
import com.ECNU.service.ClientService;
import com.ECNU.util.Cors;
import com.ECNU.util.IPUtil;
import org.dom4j.DocumentException;
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
    @GetMapping("/saveConstraintsTxtAndMyCCSL")
    public void saveConstraintsTxtAndMyCCSL(String constraints) throws DocumentException, IOException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        File assetFile = new File("asset");
        if(!assetFile.exists()) assetFile.mkdir();
        File tmpFile = new File("asset/" + ip);
        if(!tmpFile.exists()) tmpFile.mkdir();
        String filePath = "asset/" + ip + "/" + "constraints.txt";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
        for(int i = 0;i < constraints.split(",").length;i++){
            bufferedWriter.write(constraints.split(",")[i]);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
        String path = "asset/" + ip;
        String txtFileName =  "constraints.txt";
        String smvFileName = "constraints";
        clientService.toMyCCSLFormat(txtFileName, smvFileName, path,5);
    }

    @CrossOrigin
    @GetMapping("/getPhenomenonList")
    public Object getPhenomenonList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getPhenomenonList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getDiagramCount")
    public Object getDiagramCount() throws DocumentException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getDiagramCount(path);
    }


    @CrossOrigin
    @GetMapping("/getRectList")
    public Object getRectList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";

        return clientService.getRectList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getLineList")
    public Object getLineList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getLineList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getOvalList")
    public Object getOvalList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getOvalList(path, index);
    }

    /*
    @CrossOrigin
    @GetMapping("/getSubProblemDiagramList")
    public Object getSubProblenDiagramList() throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getSubProblenDiagramList(path);
    }
    */

    @CrossOrigin
    @GetMapping("/getScenarioList")
    public Object getScenarioList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getInteractionList")
    public Object getInteractionList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getInteractionList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getSubProblemDiagramList")
    public Object getSubProblemDiagramList() throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getSubProblenDiagramList(path);
    }

    @CrossOrigin
    @GetMapping("/getScenarioDiagramList")
    public Object getScenarioDiagramList() throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioDiagramList(path);
    }

    @CrossOrigin
    @GetMapping("/getDiagramList")
    public Object getDiagramList() throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getDiagramList(path);
    }

    @CrossOrigin
    @GetMapping("/getOWLConstraintList")
    public Object getOWLConstraintList() throws DocumentException, FileNotFoundException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String xmlPath = "asset/" + ip + "/" + "Project.xml";
        String owlPath = "asset/" + ip + "/" + "environment.owl";
        return clientService.getOWLConstraints(xmlPath, owlPath);
    }

    @CrossOrigin
    @GetMapping("/getAllPhenomenonList")
    public Object getAllPhenomenonList() throws DocumentException, FileNotFoundException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getAllPhenomenonList(path);
    }
    @CrossOrigin
    @GetMapping("/getAllReferenceList")
    public Object getAllReferenceList() throws DocumentException, FileNotFoundException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getAllReferenceList(path);
    }

    @CrossOrigin
    @GetMapping("/getScenarioDiagramByDomain")
    public Object getScenarioDiagramByDomain(int index, String domainText) throws DocumentException, FileNotFoundException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.getScenarioDiagramByDomain(path, index, domainText);
    }

    @CrossOrigin
    @GetMapping("/canAddConstraint")
    public Object canAddConstraint(int index, String from, String cons, String to, String boundedFrom, String boundedTo) throws DocumentException, FileNotFoundException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "Project.xml";
        return clientService.canAddConstraint(path, index, from, to, cons ,boundedFrom, boundedTo);
    }
}
