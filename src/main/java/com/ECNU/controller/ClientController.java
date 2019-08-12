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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        File tmpFile = new File("asset/" + ip);
        if(!tmpFile.exists()) tmpFile.mkdir();
        filePath = "asset/" + ip + "/" + filePath; // 这是文件的保存路径，如果不设置就会保存到项目的根目录

        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));

        outputStream.write(file.getBytes());
        outputStream.flush();
        outputStream.close();
        return "客户资料上传成功";
    }

    @CrossOrigin
    @GetMapping("/getPhenomenonList")
    public Object getPhenomenonList() throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "PackageRouterProject.xml";
        return clientService.getPhenomenonList(path);
    }

    @CrossOrigin
    @GetMapping("/getDiagramCount")
    public Object getDiagramCount() throws DocumentException {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "PackageRouterProject.xml";
        return clientService.getDiagramCount(path);
    }


    @CrossOrigin
    @GetMapping("/getRectList")
    public Object getRectList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "PackageRouterProject.xml";
        return clientService.getRectList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getLineList")
    public Object getLineList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "PackageRouterProject.xml";
        return clientService.getLineList(path, index);
    }

    @CrossOrigin
    @GetMapping("/getOvalList")
    public Object getOvalList(int index) throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');
        String path = "asset/" + ip + "/" + "PackageRouterProject.xml";
        return clientService.getOvalList(path, index);
    }
}
