package com.ECNU.controller;

import com.ECNU.bean.*;
import com.ECNU.service.ClientService;
import com.ECNU.util.Cors;
import com.ECNU.util.IPUtil;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/client")
@Data
public class ClientController extends Cors {
    @Autowired
    ClientService clientService;

    private List<Phenomenon> phenomenonList = new LinkedList<>();
    private List<Shape> shapeList = new LinkedList<>();
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
    @GetMapping("/loadProjectXML")
    public Object loadProjectXML() throws DocumentException{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = IPUtil.getIpAddress(request);
        ip = ip.replace(':','-');

        if(phenomenonList.size() == 0){
            clientService.loadProjectXML("asset/" + ip + "/" + "PackageRouterProject.xml");
        }
        for(int i = 0;i < clientService.getSubProblemDiagrams().size();i++){
            ProblemDiagram problemDiagram = clientService.getSubProblemDiagrams().get(i);
            for(int j = 0;j < problemDiagram.getPhenomenon().size();j++){
                phenomenonList.add((Phenomenon) problemDiagram.getPhenomenon().get(j));
            }
        }
        return phenomenonList;
    }
}
