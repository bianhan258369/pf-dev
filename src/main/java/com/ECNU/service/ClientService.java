package com.ECNU.service;

import com.ECNU.bean.*;
import com.ECNU.util.IPUtil;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.corba.se.spi.ior.ObjectKey;
import javafx.beans.binding.ObjectExpression;
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
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

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

    public Object getPhenomenonList(String path, int index) throws DocumentException {
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
        return subProblemDiagrams.get(index).getPhenomenon();
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

    public Object getSubProblenDiagramList(String path) throws DocumentException {
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
            return subProblemDiagrams;
        }
    }

    public Object getScenarioList(String path, int index) throws DocumentException {
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
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
        return scenarioDiagrams.get(index).getScenarios();
    }

    public Object getInteractionList(String path, int index) throws DocumentException {
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
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
        return scenarioDiagrams.get(index).getInteractions();
    }

    public Object getScenarioDiagramList(String path) throws DocumentException {
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
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
        return scenarioDiagrams;
    }

    public Object getDiagramList(String path) throws DocumentException{
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
        List<Object> result = new LinkedList<>();
        for(int i = 0;i < subProblemDiagrams.size();i++) result.add(subProblemDiagrams.get(i));
        for(int i = 0;i < scenarioDiagrams.size();i++) result.add(scenarioDiagrams.get(i));
        return result;
    }

    public Object getOWLConstraints(String xmlPath, String owlPath) throws FileNotFoundException, DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        List<ClockDiagram> clockDiagrams = new LinkedList<>();
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        ProblemDiagram myProblemDiagram;
        File file = new File(xmlPath);
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

        LinkedList<String> result = new LinkedList<>();
        Map<String, Rect> domains = new HashMap<>();
        Map<String, Phenomenon> phenomena = new HashMap<>();
        LinkedList<StateMachine> stateMachines = new LinkedList<>();
        file = new File(owlPath);
        for(int i = 0;i < scenarioDiagrams.size();i++){
            ProblemDiagram diagram = subProblemDiagrams.get(i);
            ScenarioDiagram intDiagram = scenarioDiagrams.get(i);
            for(int j = 0;j < diagram.getProblemDomains().size();j++){
                domains.put(((Rect) diagram.getProblemDomains().get(j)).getText(),(Rect) diagram.getProblemDomains().get(j));
            }
            for(int j = 0;j < diagram.getPhenomenon().size();j++){
                phenomena.put(((Phenomenon) diagram.getPhenomenon().get(j)).getName(),(Phenomenon) diagram.getPhenomenon().get(j));
            }
        }
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontModel.read(new FileInputStream(file.getPath()), "");
        for(Iterator i = ontModel.listClasses();i.hasNext();){
            OntClass c = (OntClass) i.next();
            if (!c.isAnon()) {
                boolean isTrans = false;
                for (Iterator it = c.listSuperClasses(); it.hasNext(); ){
                    OntClass sup = (OntClass) it.next();
                    if(sup.getLocalName()!=null && sup.getLocalName().equals("Transition")){
                        isTrans = true;
                        break;
                    }
                }
                if (isTrans) {
                    StateMachine stateMachine = new StateMachine();
                    for (Iterator it = c.listSuperClasses(); it.hasNext(); ) {
                        OntClass sup = (OntClass) it.next();
                        Iterator ipp = sup.listDeclaredProperties();
                        String from = null;
                        String to = null;
                        if (sup.isRestriction()) {
                            Restriction r = sup.asRestriction();
                            OntProperty p = r.getOnProperty();
                            if (r.isAllValuesFromRestriction()) {
                                AllValuesFromRestriction avf = r.asAllValuesFromRestriction();
                                if(p.getLocalName().equals("trigger")){
                                    stateMachine.setTrans(avf.getAllValuesFrom().getLocalName());
                                }
                                else if(p.getLocalName().equals("sink_to")){
                                    stateMachine.setTo(avf.getAllValuesFrom().getLocalName());
                                }
                                else if(p.getLocalName().equals("source_from")){
                                    stateMachine.setFrom(avf.getAllValuesFrom().getLocalName());
                                }
                            }
                        }
                    }
                    stateMachines.add(stateMachine);
                }
            }
        }
        for(int i = 0;i < stateMachines.size() - 1;i++){
            for(int j = i + 1;j < stateMachines.size();j++){
                StateMachine temp1 = stateMachines.get(i);
                StateMachine temp2 = stateMachines.get(j);
                if(temp1.isAlternate(temp2)){
                    if(phenomena.containsKey(temp1.getFrom()) && phenomena.containsKey(temp1.getTo())){
                        int from = phenomena.get(temp1.getFrom()).getBiaohao();
                        int to = phenomena.get(temp1.getTo()).getBiaohao();
                        for(int k = 0;k < scenarioDiagrams.size();k++){
                            if(subProblemDiagrams.get(k).getPhenomenon().contains(phenomena.get(temp1.getFrom())) && subProblemDiagrams.get(k).getPhenomenon().contains(phenomena.get(temp1.getTo()))){
                                result.add("int" + from + ".s Alternate int" + from + ".f");
                                result.add("int" + to + ".f Alternate int" + to + ".s");
                                result.add("int" + to + ".f Alternate int" + from + ".s");
                                result.add("int" + to + ".f Alternate int" + from + ".s");

                            }
                        }
                    }

                    if(phenomena.containsKey(temp1.getTrans()) && phenomena.containsKey(temp2.getTrans())){
                        int from = phenomena.get(temp1.getTrans()).getBiaohao();
                        int to = phenomena.get(temp2.getTrans()).getBiaohao();
                        for(int k = 0;k < scenarioDiagrams.size();k++){
                            if(subProblemDiagrams.get(k).getPhenomenon().contains(phenomena.get(temp1.getTrans())) && subProblemDiagrams.get(k).getPhenomenon().contains(phenomena.get(temp2.getTrans()))){
                                result.add("int" + from + " Alternate int" + to);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public Object getAllPhenomenonList(String path) throws DocumentException {
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
        List<Phenomenon> result = new LinkedList<>();
        for(int i = 0;i < subProblemDiagrams.size();i++){
            ProblemDiagram problemDiagram = subProblemDiagrams.get(i);
            for(int j = 0;j < problemDiagram.getPhenomenon().size();j++){
                Phenomenon phenomenon = (Phenomenon) problemDiagram.getPhenomenon().get(j);
                if(!result.contains(phenomenon)) result.add(phenomenon);
            }
        }
        return result;
    }

    public Object getAllReferenceList(String path) throws DocumentException {
        List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
        List<Phenomenon> phenomenonList = new LinkedList<>();
        File file = new File(path);
        ProblemDiagram problemDiagram = new ProblemDiagram("ProblemDiagram",file);
        return problemDiagram.getReference();
    }

    private boolean dfsCheckCircuit(boolean[] visited,boolean[][] graph, Interaction jiaohu, LinkedList<Interaction> jiaohus) {
        if (visited[jiaohu.getNumber()]) {
            return true;
        }
        visited[jiaohu.getNumber()] = true;
        for(int i = 0;i < jiaohus.size();i++){
            if(graph[jiaohu.getNumber()][jiaohus.get(i).getNumber()]){
                if(dfsCheckCircuit(visited, graph, jiaohus.get(i),jiaohus)){
                    return true;
                }
            }
        }
        visited[jiaohu.getNumber()] = false;
        return false;
    }

    private boolean canAddConstraint(String path, int index, String from, String to, String cons, int[] numbers) throws DocumentException {
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("filelist").next();
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
        if(cons.equals("StrictPre") || cons.equals("nStrictPre")){
            boolean[] visited = new boolean[100];
            boolean[][] graph = new boolean[100][100];
            ScenarioDiagram scenarioDiagram = scenarioDiagrams.get(index);
            for(int i = 0;i < 100;i++){
                for(int j = 0;j < 100;j++) graph[i][j] = false;
            }

            //initial graph and visited
            for(int i = 0;i < 100;i++) visited[i] = false;
            graph[Integer.parseInt(from.substring(3))][Integer.parseInt(to.substring(3))] = true;
            for(int i = 0;i < scenarioDiagram.getScenarios().size();i++){
                Scenario scenario = scenarioDiagram.getScenarios().get(i);
                int fromNum = scenario.getFrom().getNumber();
                int toNum = scenario.getTo().getNumber();
                if(scenario.getState() != 2) graph[fromNum][toNum] = true;
            }
            LinkedList<Interaction> jiaohus = scenarioDiagram.getInteractions();
            for(int i = 0;i < jiaohus.size();i++){
                if(dfsCheckCircuit(visited, graph, jiaohus.get(i),jiaohus)){
                    JOptionPane.showMessageDialog(null,"Circuit Exists!","Error",JOptionPane.ERROR_MESSAGE);
                    graph[Integer.parseInt(from.substring(3))][Integer.parseInt(to.substring(3))] = false;
                    return false;
                }
            }
            return true;
        }
        else if(cons.equals("BoundedDiff")){
            if(numbers[0] > 0){
                ScenarioDiagram scenarioDiagram = scenarioDiagrams.get(index);
                boolean[] visited = new boolean[100];
                for(int i = 0;i < 100;i++) visited[i] = false;
                boolean[][] graph = new boolean[100][100];
                graph[Integer.parseInt(from.substring(3))][Integer.parseInt(to.substring(3))] = true;

                for(int i = 0;i < scenarioDiagram.getScenarios().size();i++){
                    Scenario changjing = scenarioDiagram.getScenarios().get(i);
                    int fromNum = changjing.getFrom().getNumber();
                    int toNum = changjing.getTo().getNumber();
                    if(changjing.getState() != 2) graph[fromNum][toNum] = true;
                }
                LinkedList<Interaction> jiaohus = scenarioDiagram.getInteractions();
                for(int i = 0;i < jiaohus.size();i++){
                    if(dfsCheckCircuit(visited, graph, jiaohus.get(i),jiaohus)){
                        JOptionPane.showMessageDialog(null,"Circuit Exists!","Error",JOptionPane.ERROR_MESSAGE);
                        graph[Integer.parseInt(from.substring(3))][Integer.parseInt(to.substring(3))] = false;
                        return false;
                    }
                }
            }
            else if(numbers[1] < 0){
                ScenarioDiagram scenarioDiagram = scenarioDiagrams.get(index);
                boolean[] visited = new boolean[100];
                for(int i = 0;i < 100;i++) visited[i] = false;
                boolean[][] graph = new boolean[100][100];
                graph[Integer.parseInt(to.substring(3))][Integer.parseInt(from.substring(3))] = true;
                for(int i = 0;i < scenarioDiagram.getScenarios().size();i++){
                    Scenario changjing = scenarioDiagram.getScenarios().get(i);
                    int fromNum = changjing.getFrom().getNumber();
                    int toNum = changjing.getTo().getNumber();
                    if(changjing.getState() != 2) graph[fromNum][toNum] = true;
                }
                LinkedList<Interaction> jiaohus = scenarioDiagram.getInteractions();
                for(int i = 0;i < jiaohus.size();i++){
                    if(dfsCheckCircuit(visited, graph, jiaohus.get(i),jiaohus)){
                        JOptionPane.showMessageDialog(null,"Circuit Exists!","Error",JOptionPane.ERROR_MESSAGE);
                        graph[Integer.parseInt(from.substring(3))][Integer.parseInt(to.substring(3))] = false;
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public Object getScenarioDiagramByDomain(String path, int index, String domainText) throws DocumentException {
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

        ScenarioDiagram scenarioDiagram = scenarioDiagrams.get(index);
        ProblemDiagram problemDiagram = subProblemDiagrams.get(index);
        LinkedList<Phenomenon> phenomena= new LinkedList<>();
        LinkedList<Interaction> jiaohu = new LinkedList<>();

        Rect domain = null;
        for(int i = 0;i < problemDiagram.getProblemDomains().size();i++){
            Rect rect = (Rect) problemDiagram.getProblemDomains().get(i);
            System.out.println(rect.getText());
            if(rect.getText().equals(domainText)) domain = rect;
        }
        System.out.println(domainText);
        //this.problemDiagram = Main.win.subProblemDiagrams[index];
        //this.intDiagram = (IntDiagram) Main.win.myIntDiagram.get(index).clone();

        LinkedList tempJiaohu = scenarioDiagram.getInteractions();
        LinkedList tempPhenomenon = problemDiagram.getPhenomenon();

        for (int i = 0; i < tempJiaohu.size(); i++) {
            Interaction temp_j = (Interaction) tempJiaohu.get(i);
            int number = temp_j.getNumber();
            for (int j = 0; j < tempPhenomenon.size(); j++) {
                Phenomenon phenomenon = (Phenomenon) tempPhenomenon.get(j);
                if (phenomenon.getBiaohao() == number) {
                    if (phenomenon.getFrom().getShortName().equals(domain.getShortName()) || phenomenon.getTo().getShortName().equals(domain.getShortName())) {
                        if (!phenomena.contains(phenomenon)) phenomena.add(phenomenon);
                        jiaohu.add(temp_j);
                        break;
                    }
                }
            }
        }


        LinkedList<Interaction> absentBehaviourJiaohu = new LinkedList<>();
        LinkedList<Interaction> absentExpectJiaohu = new LinkedList<>();
        LinkedList<Interaction> allJiaohu = scenarioDiagram.getInteractions();
        LinkedList<Scenario> allChangjing = scenarioDiagram.getScenarios();
        LinkedList<Interaction> nowJiaohu = new LinkedList<>();
        LinkedList<Scenario> nowChangjing = new LinkedList<>();
        Set<Integer> tempBehaviourSet = new HashSet<>();
        Set<Integer> tempExpectSet = new HashSet<>();

        //分别得到当前ig所有的行为交互与期望交互的编号
        for (int i = 0; i < jiaohu.size(); i++) {
            if (jiaohu.get(i).getState() == 0) tempBehaviourSet.add(jiaohu.get(i).getNumber());
            if (jiaohu.get(i).getState() == 1) tempExpectSet.add(jiaohu.get(i).getNumber());
        }


        //分别得到当前ig所没有的行为交互与期望交互的编号
        for (int i = 0; i < allJiaohu.size(); i++) {
            int number = allJiaohu.get(i).getNumber();
            if ((tempBehaviourSet.contains(number) || tempExpectSet.contains(number)) && !nowJiaohu.contains(allJiaohu.get(i)))
                nowJiaohu.add(allJiaohu.get(i));
            if (allJiaohu.get(i).getState() == 0 && !tempBehaviourSet.contains(number))
                absentBehaviourJiaohu.add(allJiaohu.get(i));
            if (allJiaohu.get(i).getState() == 1 && !tempExpectSet.contains(number))
                absentExpectJiaohu.add(allJiaohu.get(i));
        }

        //得到与缺少的交互无关的场景
        for (int i = 0; i < scenarioDiagram.getScenarios().size(); i++) {
            Scenario changjing = scenarioDiagram.getScenarios().get(i);
            Interaction from = changjing.getFrom();
            Interaction to = changjing.getTo();
            if (nowJiaohu.contains(from) && nowJiaohu.contains(to)
                    && (changjing.getState() == 1 || changjing.getState() == 3)) nowChangjing.add(changjing);
        }


        //添加与缺少的行为交互有关的场景
        LinkedList<Scenario> tempBehaviourChangjing = new LinkedList<>();
        for (int i = 0; i < scenarioDiagram.getScenarios().size(); i++) {
            Scenario changjing = (Scenario) scenarioDiagram.getScenarios().get(i);
            if (changjing.getState() == 1) tempBehaviourChangjing.add(changjing);
        }

        for (int i = 0; i < absentBehaviourJiaohu.size(); i++) {
            Interaction absent = absentBehaviourJiaohu.get(i);
            LinkedList<Interaction> toAbsent = new LinkedList<>();//指向absent的交互
            LinkedList<Interaction> fromAbsent = new LinkedList<>();//来自absent的交互
            Iterator it = tempBehaviourChangjing.iterator();
            while (it.hasNext()) {
                Scenario changjing = (Scenario) it.next();
                Interaction from = changjing.getFrom();
                Interaction to = changjing.getTo();
                if (from.equals(absent) && !fromAbsent.contains(to)) {
                    fromAbsent.add(to);
                    it.remove();
                }
                if (to.equals(absent) && !toAbsent.contains(from)) {
                    toAbsent.add(from);
                    it.remove();
                }
            }
            for (int j = 0; j < fromAbsent.size(); j++) {
                Interaction tempFrom = fromAbsent.get(j);
                for (int k = 0; k < toAbsent.size(); k++) {
                    Interaction tempTo = toAbsent.get(k);
                    Scenario add = new Scenario(new LinkedList(), tempTo, tempFrom, 1);
                    nowChangjing.add(add);
                    tempBehaviourChangjing.add(add);
                }
            }
        }

        //添加与缺少的期望交互有关的场景
        LinkedList<Scenario> tempExpectedChangjing = new LinkedList<>();
        for (int i = 0; i < scenarioDiagram.getScenarios().size(); i++) {
            Scenario changjing = scenarioDiagram.getScenarios().get(i);
            if (changjing.getState() == 3) tempExpectedChangjing.add(changjing);
        }
        for (int i = 0; i < absentExpectJiaohu.size(); i++) {
            Interaction absent = absentExpectJiaohu.get(i);
            LinkedList<Interaction> toAbsent = new LinkedList<>();//指向absent的交互
            LinkedList<Interaction> fromAbsent = new LinkedList<>();//来自absent的交互
            Iterator it = tempExpectedChangjing.iterator();
            while (it.hasNext()) {
                Scenario changjing = (Scenario) it.next();
                Interaction from = changjing.getFrom();
                Interaction to = changjing.getTo();
                if (from.equals(absent) && !fromAbsent.contains(to)) {
                    fromAbsent.add(to);
                    it.remove();
                }
                if (to.equals(absent) && !toAbsent.contains(from)) {
                    toAbsent.add(from);
                    it.remove();
                }
            }
            for (int j = 0; j < fromAbsent.size(); j++) {
                Interaction tempFrom = fromAbsent.get(j);
                for (int k = 0; k < toAbsent.size(); k++) {
                    Interaction tempTo = toAbsent.get(k);
                    Scenario add = new Scenario(new LinkedList(), tempTo, tempFrom, 3);
                    nowChangjing.add(add);
                    tempExpectedChangjing.add(add);
                }
            }
        }

        Iterator it = nowChangjing.iterator();
        while (it.hasNext()) {
            Scenario changjing = (Scenario) it.next();
            Interaction from = changjing.getFrom();
            Interaction to = changjing.getTo();
            if (!nowJiaohu.contains(from) || !nowJiaohu.contains(to)) it.remove();
        }

        for (int i = 0; i < allChangjing.size(); i++) {
            Scenario changjing = allChangjing.get(i);
            Interaction from = changjing.getFrom();
            Interaction to = changjing.getTo();
            if ((changjing.getState() == 0 || changjing.getState() == 2 || changjing.getState() == 4) && nowJiaohu.contains(from) && nowJiaohu.contains(to)) {
                nowChangjing.add(changjing);
            }
        }
        List<Object> result = new LinkedList<>();
        for(int i = 0;i < nowJiaohu.size();i++) result.add(nowJiaohu.get(i));
        for(int i = 0;i < nowChangjing.size();i++) result.add(nowChangjing.get(i));
        return result;
    }
}
