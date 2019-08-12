package com.ECNU.service;

import com.ECNU.bean.*;
import com.ECNU.util.IPUtil;
import com.sun.corba.se.spi.ior.ObjectKey;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Data
@Service
public class ClientService implements Serializable{

    private void loadProjectXML(String path) throws DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        List<ClockDiagram> clockDiagrams = new LinkedList<>();
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        ProblemDiagram myProblemDiagram;
        File file = new File(path);
        int count = 1;
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = (Element) root.elementIterator("data").next();
            Element temp;
            for(Iterator i = spdRoot.elementIterator("Machine");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("machine_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("machine_name"));
                rect.setShortName(temp.attributeValue("machine_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("machine_state")));
                diagram.components.add(rect);
            }

            Element requirement = (Element) spdRoot.elementIterator("Requirement").next();
            for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_text"));
                oval.setDes(Integer.parseInt(temp.attributeValue("requirement_des")));
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_biaohao")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("Problemdomain").next();
            for(Iterator i = problemDomain.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("problemdomain_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("problemdomain_name"));
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("problemdomain_state")));
                rect.setCxb(temp.attributeValue("problemdomain_cxb").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line1_name");
                String str = temp.attributeValue("line1_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                        if(tempRect.getState() == 2 && tempRect.getShortName().equals(from)){
                            fromShape = tempRect;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 0);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 1);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element constraint = (Element) spdRoot.elementIterator("Constraint").next();
            for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 2);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }
            subProblemDiagrams.add(diagram);
            ClockDiagram clockDiagram = new ClockDiagram(diagram);
            clockDiagram.setTitle("ClockDiagram" + count);
            clockDiagrams.add(clockDiagram);
            count++;
        }
        myProblemDiagram = new ProblemDiagram("ProblemDiagram",file);
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioFilelist").next();
        for(Iterator it = senList.elementIterator("SenarioDiagram");it.hasNext();){
            Element sd = (Element) it.next();
            String sdPath = ProblemDiagram.getFilePath(file.getPath()) + sd.getText()+".xml";
            File sdFile = new File(sdPath);
            ScenarioDiagram intDiagram = new ScenarioDiagram("SenarioDiagram" + senCount,senCount,sdFile);
            scenarioDiagrams.add(intDiagram);
            senCount++;
        }
    }

    public int getDiagramCount(String path) throws DocumentException {
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("filelist").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = (Element) root.elementIterator("data").next();
                Element temp;
                for(Iterator i = spdRoot.elementIterator("Machine");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("machine_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                    rect.setText(temp.attributeValue("machine_name"));
                    rect.setShortName(temp.attributeValue("machine_shortname"));
                    rect.setState(Integer.parseInt(temp.attributeValue("machine_state")));
                    diagram.components.add(rect);
                }

                Element requirement = (Element) spdRoot.elementIterator("Requirement").next();
                for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_text"));
                    oval.setDes(Integer.parseInt(temp.attributeValue("requirement_des")));
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_biaohao")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("Problemdomain").next();
                for(Iterator i = problemDomain.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("problemdomain_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                    rect.setText(temp.attributeValue("problemdomain_name"));
                    rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                    rect.setState(Integer.parseInt(temp.attributeValue("problemdomain_state")));
                    rect.setCxb(temp.attributeValue("problemdomain_cxb").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("line1_name");
                    String str = temp.attributeValue("line1_tofrom");
                    String[] locality = str.split(",");
                    String to = locality[0];
                    String from = locality[1];
                    Shape toShape = null;
                    Shape fromShape = null;
                    for(int j = 0;j < diagram.components.size();j++){
                        Shape tempShape = (Shape)diagram.components.get(j);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                                toShape = tempRect;
                            }
                            if(tempRect.getState() == 2 && tempRect.getShortName().equals(from)){
                                fromShape = tempRect;
                            }
                        }
                    }
                    Line line = new Line(fromShape, toShape, 0);
                    line.setName(name);
                    Element tempPhenomenon;
                    for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                        tempPhenomenon = (Element)j.next();
                        String phenomenonName = tempPhenomenon.attributeValue("name");
                        String phenomenonState = tempPhenomenon.attributeValue("state");
                        String phenomenonFrom = tempPhenomenon.attributeValue("from");
                        String phenomenonTo = tempPhenomenon.attributeValue("to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(phenomenonConstraining);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("line2_name");
                    String str = temp.attributeValue("line2_tofrom");
                    String[] locality = str.split(",");
                    String to = locality[0];
                    String from = locality[1];
                    Shape toShape = null;
                    Shape fromShape = null;
                    for(int j = 0;j < diagram.components.size();j++){
                        Shape tempShape = (Shape)diagram.components.get(j);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                                toShape = tempRect;
                            }
                        }
                        if(tempShape instanceof Oval){
                            Oval tempOval = (Oval)tempShape;
                            if(tempOval.getText().equals(from)){
                                fromShape = tempOval;
                            }
                        }
                    }
                    Line line = new Line(fromShape, toShape, 1);
                    line.setName(name);
                    Element tempPhenomenon;
                    for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                        tempPhenomenon = (Element)j.next();
                        String phenomenonName = tempPhenomenon.attributeValue("name");
                        String phenomenonState = tempPhenomenon.attributeValue("state");
                        String phenomenonFrom = tempPhenomenon.attributeValue("from");
                        String phenomenonTo = tempPhenomenon.attributeValue("to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(phenomenonConstraining);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        phenomenon.setRequirement(oval);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element constraint = (Element) spdRoot.elementIterator("Constraint").next();
                for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("line2_name");
                    String str = temp.attributeValue("line2_tofrom");
                    String[] locality = str.split(",");
                    String to = locality[0];
                    String from = locality[1];
                    Shape toShape = null;
                    Shape fromShape = null;
                    for(int j = 0;j < diagram.components.size();j++){
                        Shape tempShape = (Shape)diagram.components.get(j);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                                toShape = tempRect;
                            }
                        }
                        if(tempShape instanceof Oval){
                            Oval tempOval = (Oval)tempShape;
                            if(tempOval.getText().equals(from)){
                                fromShape = tempOval;
                            }
                        }
                    }
                    Line line = new Line(fromShape, toShape, 2);
                    line.setName(name);
                    Element tempPhenomenon;
                    for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                        tempPhenomenon = (Element)j.next();
                        String phenomenonName = tempPhenomenon.attributeValue("name");
                        String phenomenonState = tempPhenomenon.attributeValue("state");
                        String phenomenonFrom = tempPhenomenon.attributeValue("from");
                        String phenomenonTo = tempPhenomenon.attributeValue("to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(phenomenonConstraining);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        phenomenon.setRequirement(oval);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }
                subProblemDiagrams.add(diagram);
                count++;
            }
            return subProblemDiagrams.size();
        }
    }

    public Object getPhenomenonList(String path) throws DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        List<Phenomenon> phenomenonList = new LinkedList<>();
        File file = new File(path);
        int count = 1;
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = (Element) root.elementIterator("data").next();
            Element temp;
            for(Iterator i = spdRoot.elementIterator("Machine");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("machine_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("machine_name"));
                rect.setShortName(temp.attributeValue("machine_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("machine_state")));
                diagram.components.add(rect);
            }

            Element requirement = (Element) spdRoot.elementIterator("Requirement").next();
            for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_text"));
                oval.setDes(Integer.parseInt(temp.attributeValue("requirement_des")));
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_biaohao")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("Problemdomain").next();
            for(Iterator i = problemDomain.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("problemdomain_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("problemdomain_name"));
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("problemdomain_state")));
                rect.setCxb(temp.attributeValue("problemdomain_cxb").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line1_name");
                String str = temp.attributeValue("line1_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                        if(tempRect.getState() == 2 && tempRect.getShortName().equals(from)){
                            fromShape = tempRect;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 0);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 1);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element constraint = (Element) spdRoot.elementIterator("Constraint").next();
            for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 2);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }
            subProblemDiagrams.add(diagram);
            count++;
        }
        for(int i = 0;i < subProblemDiagrams.size();i++){
            ProblemDiagram problemDiagram = subProblemDiagrams.get(i);
            for(int j = 0;j < problemDiagram.getPhenomenon().size();j++){
                phenomenonList.add((Phenomenon) problemDiagram.getPhenomenon().get(j));
            }
        }
        return phenomenonList;
    }

    public Object getRectList(String path, int index) throws DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        File file = new File(path);
        int count = 1;
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = (Element) root.elementIterator("data").next();
            Element temp;
            for(Iterator i = spdRoot.elementIterator("Machine");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("machine_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("machine_name"));
                rect.setShortName(temp.attributeValue("machine_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("machine_state")));
                diagram.components.add(rect);
            }

            Element requirement = (Element) spdRoot.elementIterator("Requirement").next();
            for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_text"));
                oval.setDes(Integer.parseInt(temp.attributeValue("requirement_des")));
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_biaohao")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("Problemdomain").next();
            for(Iterator i = problemDomain.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("problemdomain_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("problemdomain_name"));
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("problemdomain_state")));
                rect.setCxb(temp.attributeValue("problemdomain_cxb").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line1_name");
                String str = temp.attributeValue("line1_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                        if(tempRect.getState() == 2 && tempRect.getShortName().equals(from)){
                            fromShape = tempRect;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 0);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 1);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element constraint = (Element) spdRoot.elementIterator("Constraint").next();
            for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 2);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }
            subProblemDiagrams.add(diagram);
            count++;
        }
        ProblemDiagram problemDiagram = subProblemDiagrams.get(index);
        return problemDiagram.getRect();
    }

    public Object getLineList(String path, int index) throws DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        File file = new File(path);
        int count = 1;
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = (Element) root.elementIterator("data").next();
            Element temp;
            for(Iterator i = spdRoot.elementIterator("Machine");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("machine_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("machine_name"));
                rect.setShortName(temp.attributeValue("machine_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("machine_state")));
                diagram.components.add(rect);
            }

            Element requirement = (Element) spdRoot.elementIterator("Requirement").next();
            for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_text"));
                oval.setDes(Integer.parseInt(temp.attributeValue("requirement_des")));
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_biaohao")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("Problemdomain").next();
            for(Iterator i = problemDomain.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("problemdomain_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("problemdomain_name"));
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("problemdomain_state")));
                rect.setCxb(temp.attributeValue("problemdomain_cxb").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line1_name");
                String str = temp.attributeValue("line1_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                        if(tempRect.getState() == 2 && tempRect.getShortName().equals(from)){
                            fromShape = tempRect;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 0);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 1);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element constraint = (Element) spdRoot.elementIterator("Constraint").next();
            for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 2);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }
            subProblemDiagrams.add(diagram);
            count++;
        }
        ProblemDiagram problemDiagram = subProblemDiagrams.get(index);
        return problemDiagram.getLines();
    }

    public Object getOvalList(String path, int index) throws DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        File file = new File(path);
        int count = 1;
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = (Element) root.elementIterator("data").next();
            Element temp;
            for(Iterator i = spdRoot.elementIterator("Machine");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("machine_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("machine_name"));
                rect.setShortName(temp.attributeValue("machine_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("machine_state")));
                diagram.components.add(rect);
            }

            Element requirement = (Element) spdRoot.elementIterator("Requirement").next();
            for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_text"));
                oval.setDes(Integer.parseInt(temp.attributeValue("requirement_des")));
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_biaohao")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("Problemdomain").next();
            for(Iterator i = problemDomain.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("problemdomain_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Rect rect = new Rect(x1 + x2 / 2, y1 + y2 / 2);
                rect.setText(temp.attributeValue("problemdomain_name"));
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(Integer.parseInt(temp.attributeValue("problemdomain_state")));
                rect.setCxb(temp.attributeValue("problemdomain_cxb").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line1_name");
                String str = temp.attributeValue("line1_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                        if(tempRect.getState() == 2 && tempRect.getShortName().equals(from)){
                            fromShape = tempRect;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 0);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 1);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element constraint = (Element) spdRoot.elementIterator("Constraint").next();
            for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("line2_name");
                String str = temp.attributeValue("line2_tofrom");
                String[] locality = str.split(",");
                String to = locality[0];
                String from = locality[1];
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < diagram.components.size();j++){
                    Shape tempShape = (Shape)diagram.components.get(j);
                    if(tempShape instanceof Rect){
                        Rect tempRect = (Rect)tempShape;
                        if(tempRect.getState() != 2 && tempRect.getShortName().equals(to)){
                            toShape = tempRect;
                        }
                    }
                    if(tempShape instanceof Oval){
                        Oval tempOval = (Oval)tempShape;
                        if(tempOval.getText().equals(from)){
                            fromShape = tempOval;
                        }
                    }
                }
                Line line = new Line(fromShape, toShape, 2);
                line.setName(name);
                Element tempPhenomenon;
                for(Iterator j = temp.elementIterator("Phenomenon");j.hasNext();){
                    tempPhenomenon = (Element)j.next();
                    String phenomenonName = tempPhenomenon.attributeValue("name");
                    String phenomenonState = tempPhenomenon.attributeValue("state");
                    String phenomenonFrom = tempPhenomenon.attributeValue("from");
                    String phenomenonTo = tempPhenomenon.attributeValue("to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("constraining")).equals("True") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("biaohao"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getText().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getText().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = diagram.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }
            subProblemDiagrams.add(diagram);
            count++;
        }
        ProblemDiagram problemDiagram = subProblemDiagrams.get(index);
        return problemDiagram.getRequirements();
    }
}
