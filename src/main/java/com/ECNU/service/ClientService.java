package com.ECNU.service;

import com.ECNU.FormatTransfer.RunNuSMV;
import com.ECNU.FormatTransfer.ToMyCCSLFormat;
import com.ECNU.FormatTransfer.ToNuSMVFormat;
import com.ECNU.bean.*;
import com.ECNU.util.IPUtil;
import com.ECNU.util.TestCircle;
import com.sun.corba.se.spi.ior.ObjectKey;
import javafx.beans.binding.ObjectExpression;
import lombok.Data;
import net.sf.json.JSONObject;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.io.*;
import java.util.*;

@Data
@Service
@Scope("prototype")
public class ClientService implements Serializable{
    private boolean[] visited;
    private String circle;

    public int getDiagramCount(String path) throws DocumentException {
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = root;
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
                    rect.setState(2);
                    diagram.components.add(rect);
                }

                for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_context"));
                    oval.setDes(1);
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
                Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
                for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element designDomain = problemDomain.elementIterator("DesignDomain").next();
                for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("interface_name");
                    String to = temp.attributeValue("interface_to");
                    String from = temp.attributeValue("interface_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(false);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("reference_name");
                    String to = temp.attributeValue("reference_to");
                    String from = temp.attributeValue("reference_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                    String name = temp.attributeValue("constraint_name");
                    String to = temp.attributeValue("constraint_to");
                    String from = temp.attributeValue("constraint_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = root;
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
                    rect.setState(2);
                    diagram.components.add(rect);
                }

                for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_context"));
                    oval.setDes(1);
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
                Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
                for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element designDomain = problemDomain.elementIterator("DesignDomain").next();
                for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("interface_name");
                    String to = temp.attributeValue("interface_to");
                    String from = temp.attributeValue("interface_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(false);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("reference_name");
                    String to = temp.attributeValue("reference_to");
                    String from = temp.attributeValue("reference_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                    String name = temp.attributeValue("constraint_name");
                    String to = temp.attributeValue("constraint_to");
                    String from = temp.attributeValue("constraint_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
    }

    public Object getRectList(String path, int index) throws DocumentException {
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                System.out.println(spdPath);
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = root;
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
                    rect.setState(2);
                    diagram.components.add(rect);
                }

                for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_context"));
                    oval.setDes(1);
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
                Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
                for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element designDomain = problemDomain.elementIterator("DesignDomain").next();
                for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("interface_name");
                    String to = temp.attributeValue("interface_to");
                    String from = temp.attributeValue("interface_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(false);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("reference_name");
                    String to = temp.attributeValue("reference_to");
                    String from = temp.attributeValue("reference_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                    String name = temp.attributeValue("constraint_name");
                    String to = temp.attributeValue("constraint_to");
                    String from = temp.attributeValue("constraint_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
            return subProblemDiagrams.get(index).getRect();
        }
    }

    public Object getLineList(String path, int index) throws DocumentException {
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = root;
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
                    rect.setState(2);
                    diagram.components.add(rect);
                }

                for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_context"));
                    oval.setDes(1);
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
                Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
                for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element designDomain = problemDomain.elementIterator("DesignDomain").next();
                for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("interface_name");
                    String to = temp.attributeValue("interface_to");
                    String from = temp.attributeValue("interface_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(false);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("reference_name");
                    String to = temp.attributeValue("reference_to");
                    String from = temp.attributeValue("reference_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                    String name = temp.attributeValue("constraint_name");
                    String to = temp.attributeValue("constraint_to");
                    String from = temp.attributeValue("constraint_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
            return subProblemDiagrams.get(index).getLines();
        }
    }

    public Object getOvalList(String path, int index) throws DocumentException {
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = root;
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
                    rect.setState(2);
                    diagram.components.add(rect);
                }

                for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_context"));
                    oval.setDes(1);
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
                Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
                for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element designDomain = problemDomain.elementIterator("DesignDomain").next();
                for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("interface_name");
                    String to = temp.attributeValue("interface_to");
                    String from = temp.attributeValue("interface_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(false);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("reference_name");
                    String to = temp.attributeValue("reference_to");
                    String from = temp.attributeValue("reference_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                    String name = temp.attributeValue("constraint_name");
                    String to = temp.attributeValue("constraint_to");
                    String from = temp.attributeValue("constraint_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
            return subProblemDiagrams.get(index).getRequirements();
        }
    }

    public Object getSubProblenDiagramList(String path) throws DocumentException {
        {
            List<ProblemDiagram> subProblemDiagrams = new LinkedList<>();
            File file = new File(path);
            int count = 1;
            SAXReader saxReader = new SAXReader();
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            Element subList = filelist.elementIterator("SubProblemDiagramList").next();
            for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
                ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
                diagram.components = new LinkedList();
                Element spd = (Element) it.next();
                String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
                Document subProDiagram = saxReader.read(spdPath);
                Element root = subProDiagram.getRootElement();
                Element spdRoot = root;
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
                    rect.setState(2);
                    diagram.components.add(rect);
                }

                for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                    temp = (Element)i.next();
                    String str = temp.attributeValue("requirement_locality");
                    String[] locality = str.split(",");
                    int x1 = Integer.parseInt(locality[0]);
                    int y1 = Integer.parseInt(locality[1]);
                    int x2 = Integer.parseInt(locality[2]);
                    int y2 = Integer.parseInt(locality[3]);
                    Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                    oval.setText(temp.attributeValue("requirement_context"));
                    oval.setDes(1);
                    oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                    diagram.components.add(oval);
                }

                Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
                Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
                for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element designDomain = problemDomain.elementIterator("DesignDomain").next();
                for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                    rect.setState(1);
                    rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                    diagram.components.add(rect);
                }

                Element Interface = (Element) spdRoot.elementIterator("Interface").next();
                for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("interface_name");
                    String to = temp.attributeValue("interface_to");
                    String from = temp.attributeValue("interface_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                            }
                        }
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                        phenomenon.setConstraining(false);
                        phenomenon.setBiaohao(phenomenonBiaohao);
                        line.getPhenomena().add(phenomenon);
                    }
                    diagram.components.add(line);
                }

                Element reference = spdRoot.elementIterator("Reference").next();
                for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                    temp = (Element)i.next();
                    String name = temp.attributeValue("reference_name");
                    String to = temp.attributeValue("reference_to");
                    String from = temp.attributeValue("reference_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                    String name = temp.attributeValue("constraint_name");
                    String to = temp.attributeValue("constraint_to");
                    String from = temp.attributeValue("constraint_from");
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
                        String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                        String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                        String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                        String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                        int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                        boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                        int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                        Rect phenomenonFromRect = null;
                        Rect phenomenonToRect = null;
                        for(int k = 0;k < diagram.components.size();k++){
                            Shape tempShape = (Shape)diagram.components.get(k);
                            if(tempShape instanceof Rect){
                                Rect tempRect = (Rect)tempShape;
                                if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                                if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
        Element filelist = rootElement.elementIterator("fileList").next();
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
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
        Element filelist = rootElement.elementIterator("fileList").next();
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
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
        Element filelist = rootElement.elementIterator("fileList").next();
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
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
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        int count = 1;
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("fileList").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            System.out.println(file.getPath());
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = root;
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
                rect.setState(2);
                diagram.components.add(rect);
            }

            for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_context"));
                oval.setDes(1);
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
            Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
            for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element designDomain = problemDomain.elementIterator("DesignDomain").next();
            for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("interface_name");
                String to = temp.attributeValue("interface_to");
                String from = temp.attributeValue("interface_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(false);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("reference_name");
                String to = temp.attributeValue("reference_to");
                String from = temp.attributeValue("reference_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                String name = temp.attributeValue("constraint_name");
                String to = temp.attributeValue("constraint_to");
                String from = temp.attributeValue("constraint_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
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
        Element filelist = rootElement.elementIterator("fileList").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = root;
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
                rect.setState(2);
                diagram.components.add(rect);
            }

            for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_context"));
                oval.setDes(1);
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
            Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
            for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element designDomain = problemDomain.elementIterator("DesignDomain").next();
            for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("interface_name");
                String to = temp.attributeValue("interface_to");
                String from = temp.attributeValue("interface_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(false);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("reference_name");
                String to = temp.attributeValue("reference_to");
                String from = temp.attributeValue("reference_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                String name = temp.attributeValue("constraint_name");
                String to = temp.attributeValue("constraint_to");
                String from = temp.attributeValue("constraint_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
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
        if(!file.exists()) return null;
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
        Element filelist = rootElement.elementIterator("fileList").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = root;
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
                rect.setState(2);
                diagram.components.add(rect);
            }

            for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_context"));
                oval.setDes(1);
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
            Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
            for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element designDomain = problemDomain.elementIterator("DesignDomain").next();
            for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("interface_name");
                String to = temp.attributeValue("interface_to");
                String from = temp.attributeValue("interface_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(false);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("reference_name");
                String to = temp.attributeValue("reference_to");
                String from = temp.attributeValue("reference_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                String name = temp.attributeValue("constraint_name");
                String to = temp.attributeValue("constraint_to");
                String from = temp.attributeValue("constraint_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
        File file = new File(path);
        ProblemDiagram problemDiagram = new ProblemDiagram("ProblemDiagram",file);
        return problemDiagram.getReference();
    }

    //from/to : number,state
    public boolean canAddConstraint(String path, int index, String from, String to, String cons, String boundedFrom, String boundedTo) throws DocumentException {
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("fileList").next();
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
            Element sd = (Element) it.next();
            String sdPath = ProblemDiagram.getFilePath(file.getPath()) + sd.getText()+".xml";
            File sdFile = new File(sdPath);
            ScenarioDiagram intDiagram = new ScenarioDiagram("SenarioDiagram" + senCount,senCount,sdFile);
            scenarioDiagrams.add(intDiagram);
            senCount++;
        }
        if(cons.equals("StrictPre") || cons.equals("nStrictPre")){
            ScenarioDiagram scenarioDiagram = scenarioDiagrams.get(index);
            int fromNum = Integer.parseInt(from.split(",")[0]);
            int fromstate = Integer.parseInt(from.split(",")[1]);
            int toNum = Integer.parseInt(to.split(",")[0]);
            int toState = Integer.parseInt(to.split(",")[1]);
            Interaction fromInt = scenarioDiagram.getInteraction(fromNum, fromstate);
            Interaction toInt = scenarioDiagram.getInteraction(toNum, toState);
            Scenario addedScenario = null;
            if(fromstate == 0 && toState == 0) addedScenario = new Scenario(new LinkedList<String>(), fromInt, toInt, 1);
            else if(fromstate == 1 && toState == 1) addedScenario = new Scenario(new LinkedList<String>(), fromInt, toInt, 3);
            else if(fromstate == 0 && toState == 1) addedScenario = new Scenario(new LinkedList<String>(), fromInt, toInt, 0);
            else addedScenario = new Scenario(new LinkedList<String>(), fromInt, toInt, 4);
            scenarioDiagram.addChangjing(addedScenario);
            //step1
            BiMap<Integer, Integer> behMap = HashBiMap.create();
            BiMap<Integer, Integer> expMap = HashBiMap.create();
            for (int j = 0; j < scenarioDiagram.getInteractions().size(); j++) {
                Interaction interaction = scenarioDiagram.getInteractions().get(j);
                if (interaction.getState() == 0) behMap.put(interaction.getNumber(), behMap.size());
                if (interaction.getState() == 1) expMap.put(interaction.getNumber(), expMap.size());
            }
            int behSize = behMap.size();
            int expSize = expMap.size();
            int[][] behGraph = new int[behSize][behSize];
            int[][] expGraph = new int[expSize][expSize];


            for (int m = 0; m < behSize; m++) {
                for (int n = 0; n < behSize; n++) {
                    behGraph[m][n] = 0;
                }
            }
            for (int j = 0; j < scenarioDiagram.getScenarios().size(); j++) {
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if (scenario.getFrom().getState() == 0 && scenario.getTo().getState() == 0) {
                    behGraph[behMap.get(scenario.getFrom().getNumber())][behMap.get(scenario.getTo().getNumber())] = 1;
                }
            }
            for (int m = 0; m < expSize; m++) {
                for (int n = 0; n < expSize; n++) {
                    expGraph[m][n] = 0;
                }
            }
            for (int j = 0; j < scenarioDiagram.getScenarios().size(); j++) {
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if (scenario.getFrom().getState() == 1 && scenario.getTo().getState() == 1) {
                    expGraph[expMap.get(scenario.getFrom().getNumber())][expMap.get(scenario.getTo().getNumber())] = 1;
                }
            }

            //step2
            for(int j = 0;j < scenarioDiagram.getScenarios().size();j++){
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if(scenario.getState() == 0 || scenario.getState() == 2 || scenario.getState() == 4){
                    int behStart = -1;
                    int expStart = -1;
                    if(scenario.getFrom().getState() == 0 && scenario.getTo().getState() == 1){
                        behStart = behMap.get(scenario.getFrom().getNumber());
                        expStart = expMap.get(scenario.getTo().getNumber());
                    }
                    else{
                        behStart = behMap.get(scenario.getTo().getNumber());
                        expStart = expMap.get(scenario.getFrom().getNumber());
                    }
                    TestCircle behTc = new TestCircle(behMap.size(),behGraph);
                    TestCircle expTc = new TestCircle(expMap.size(),expGraph);
                    behTc.findCycle(behStart);
                    expTc.findCycle(expStart);
                    if(behTc.getHasCycle() && !expTc.getHasCycle()){
                        return false;
                    }
                    else if(!behTc.getHasCycle() && expTc.getHasCycle()){
                        return false;
                    }
                    if(behTc.getHasCycle() && expTc.getHasCycle()){
                        for(int m = 0;m < behTc.getCircles().size();m++){
                            ArrayList<Integer> behCircle = behTc.getCircles().get(m);
                            int behLast = behMap.inverse().get(behCircle.get(behCircle.size() - 1));
                            System.out.println("behlast:" + behLast);
                            if(behMap.inverse().get(behCircle.get(0)) == behMap.inverse().get(behStart)){
                                for(int n = 0;n < expTc.getCircles().size();n++){
                                    boolean flag = false;
                                    ArrayList<Integer> expCircle = expTc.getCircles().get(n);
                                    if(expMap.inverse().get(expCircle.get(0)) == expMap.inverse().get(expStart)){
                                        int expLast = expMap.inverse().get(expCircle.get(expCircle.size() - 1));
                                        System.out.println("expLast:" + expLast);
                                        for(int k = 0;k < scenarioDiagram.getScenarios().size();k++){
                                            Scenario tmpScenario = scenarioDiagram.getScenarios().get(k);
                                            if(tmpScenario.getState() == 0 || tmpScenario.getState() == 2 ||tmpScenario.getState() == 4){
                                                if(tmpScenario.getFrom().getNumber() == behLast && tmpScenario.getTo().getNumber() == expLast
                                                        || tmpScenario.getTo().getNumber() == behLast && tmpScenario.getFrom().getNumber() == expLast){
                                                    flag = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if(!flag){
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //step3
            BiMap<Integer, Integer> map = HashBiMap.create();
            for(int j = 0;j < scenarioDiagram.getInteractions().size();j++){
                Interaction interaction = scenarioDiagram.getInteractions().get(j);
                map.put(interaction.toNum(),map.size());
            }
            int mapSize = map.size();
            int[][] graph = new int[mapSize][mapSize];
            for(int m = 0;m < mapSize;m++){
                for(int n = 0;n < mapSize;n++){
                    graph[m][n] = 0;
                }
            }

            List<Scenario> synchronize = new LinkedList<>();
            for(int j = 0;j < scenarioDiagram.getScenarios().size();j++){
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if(scenario.getState() != 2){
                    graph[map.get(scenario.getFrom().toNum())]
                            [map.get(scenario.getTo().toNum())] = 1;
                }
                else {
                    synchronize.add(scenario);
                }
            }
            for(int j = 0;j < Math.pow(2,synchronize.size());j++){
                String bin = Integer.toBinaryString(j);
                while(bin.length() < synchronize.size()) bin = "0" + bin;
                for(int k = 0;k < synchronize.size();k++){
                    if(bin.charAt(k) == '0')
                        graph[map.get(synchronize.get(k).getFrom().toNum())]
                                [map.get(synchronize.get(k).getTo().toNum())] = 1;
                    else
                        graph[map.get(synchronize.get(k).getTo().toNum())]
                                [map.get(synchronize.get(k).getFrom().toNum())] = 1;
                }
                for(int m = 0;m < scenarioDiagram.getScenarios().size();m++){
                    Scenario scenario = scenarioDiagram.getScenarios().get(m);
                    if(scenario.getState() == 0){
                        Interaction interaction = scenario.getFrom();
                        TestCircle tc = new TestCircle(mapSize, graph);
                        tc.findCycle(map.get(interaction.toNum()));
                        if(tc.getHasCycle()){
                            for(int n = 0;n < tc.getCircles().size();n++){
                                ArrayList<Integer> circles = tc.getCircles().get(n);
                                if(map.inverse().get(circles.get(0)) == interaction.toNum()){
                                    List<Integer> states = new LinkedList<>();
                                    for(int t = 0;t < circles.size();t++){
                                        if(map.inverse().get(circles.get(t)) > 0){
                                            states.add(1);
                                        }
                                        else{
                                            states.add(0);
                                        }
                                    }
                                    if(states.contains(0) && states.contains(1)){
                                        return false;
                                    }
                                }

                            }
                        }
                    }
                }
                for(int k = 0;k < synchronize.size();k++){
                    if(bin.charAt(k) == '0')
                        graph[map.get(synchronize.get(k).getFrom().toNum())]
                                [map.get(synchronize.get(k).getTo().toNum())] = 0;
                    else
                        graph[map.get(synchronize.get(k).getTo().toNum())]
                                [map.get(synchronize.get(k).getFrom().toNum())] = 0;
                }
            }
            return true;
        }
        else if(cons.equals("BoundedDiff")){
            if(Double.parseDouble(boundedFrom) > 0){
                return canAddConstraint(path, index, from, to, "StrictPre",null, null);
            }
            else if(Double.parseDouble(boundedTo) < 0){
                return canAddConstraint(path, index, to, from, "StrictPre",null, null);
            }
        }
        return true;
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
        Element filelist = rootElement.elementIterator("fileList").next();
        Element subList = filelist.elementIterator("SubProblemDiagramList").next();
        for(Iterator it = subList.elementIterator("SubProblemDiagram"); it.hasNext();){
            ProblemDiagram diagram = new ProblemDiagram("SubProblemDiagram" + count);
            diagram.components = new LinkedList();
            Element spd = (Element) it.next();
            String spdPath = ProblemDiagram.getFilePath(file.getPath()) + spd.getText()+".xml";
            Document subProDiagram = saxReader.read(spdPath);
            Element root = subProDiagram.getRootElement();
            Element spdRoot = root;
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
                rect.setState(2);
                diagram.components.add(rect);
            }

            for(Iterator i = spdRoot.elementIterator("Requirement");i.hasNext();){
                temp = (Element)i.next();
                String str = temp.attributeValue("requirement_locality");
                String[] locality = str.split(",");
                int x1 = Integer.parseInt(locality[0]);
                int y1 = Integer.parseInt(locality[1]);
                int x2 = Integer.parseInt(locality[2]);
                int y2 = Integer.parseInt(locality[3]);
                Oval oval = new Oval(x1 + x2 / 2, y1 + y2 / 2);
                oval.setText(temp.attributeValue("requirement_context"));
                oval.setDes(1);
                oval.setBiaohao(Integer.parseInt(temp.attributeValue("requirement_no")));
                diagram.components.add(oval);
            }

            Element problemDomain = (Element) spdRoot.elementIterator("ProblemDomain").next();
            Element givenDomain = problemDomain.elementIterator("GivenDomain").next();
            for(Iterator i = givenDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element designDomain = problemDomain.elementIterator("DesignDomain").next();
            for(Iterator i = designDomain.elementIterator("Element");i.hasNext();){
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
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                diagram.components.add(rect);
            }

            Element Interface = (Element) spdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("interface_name");
                String to = temp.attributeValue("interface_to");
                String from = temp.attributeValue("interface_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(false);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    line.getPhenomena().add(phenomenon);
                }
                diagram.components.add(line);
            }

            Element reference = spdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("reference_name");
                String to = temp.attributeValue("reference_to");
                String from = temp.attributeValue("reference_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
                String name = temp.attributeValue("constraint_name");
                String to = temp.attributeValue("constraint_to");
                String from = temp.attributeValue("constraint_from");
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
                    String phenomenonName = tempPhenomenon.attributeValue("phenomenon_name");
                    String phenomenonState = tempPhenomenon.attributeValue("phenomenon_type");
                    String phenomenonFrom = tempPhenomenon.attributeValue("phenomenon_from");
                    String phenomenonTo = tempPhenomenon.attributeValue("phenomenon_to");
                    int pehnomenonRequirementBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_requirement"));
                    boolean phenomenonConstraining = (tempPhenomenon.attributeValue("phenomenon_constraint")).equals("true") ? true : false;
                    int phenomenonBiaohao = Integer.parseInt(tempPhenomenon.attributeValue("phenomenon_no"));
                    Rect phenomenonFromRect = null;
                    Rect phenomenonToRect = null;
                    for(int k = 0;k < diagram.components.size();k++){
                        Shape tempShape = (Shape)diagram.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
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
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
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
            //System.out.println(rect.getText());
            if(rect.getText().equals(domainText)) domain = rect;
        }
        //System.out.println(domainText);
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

    public String ruleBasedCheck(String path) throws DocumentException {
        List<ScenarioDiagram> scenarioDiagrams = new LinkedList<>();
        File file = new File(path);
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        Element filelist = rootElement.elementIterator("fileList").next();
        int senCount = 1;
        Element senList = filelist.elementIterator("SenarioGraphList").next();
        for(Iterator it = senList.elementIterator("SenarioGraph");it.hasNext();){
            Element sd = (Element) it.next();
            String sdPath = ProblemDiagram.getFilePath(file.getPath()) + sd.getText()+".xml";
            File sdFile = new File(sdPath);
            ScenarioDiagram intDiagram = new ScenarioDiagram("SenarioDiagram" + senCount,senCount,sdFile);
            scenarioDiagrams.add(intDiagram);
            senCount++;
        }

        circle = "{\"circle\":\"";
        //step1 : 使用两个BiMap 分别将行为交互与期望交互的编号映射到0至交互数量 然后根据这个映射创建行为交互与期望交互对应的graph
        //step2 : 遍历state为2的场景（coincidence）这个场景的from与to一定分别为行为交互和期望交互 分别寻找以它们为起点的循环 如果这两个循环的首尾编号都相同 则没问题
        //step3 : 考虑行为交互和期望交互之间的循环，只需考虑从每一个state为0的场景的from交互即可
        Outer:
        for(int i = 0;i < scenarioDiagrams.size();i++) {
            ScenarioDiagram scenarioDiagram = scenarioDiagrams.get(i);

            //step1
            BiMap<Integer, Integer> behMap = HashBiMap.create();
            BiMap<Integer, Integer> expMap = HashBiMap.create();
            for (int j = 0; j < scenarioDiagram.getInteractions().size(); j++) {
                Interaction interaction = scenarioDiagram.getInteractions().get(j);
                if (interaction.getState() == 0) behMap.put(interaction.getNumber(), behMap.size());
                if (interaction.getState() == 1) expMap.put(interaction.getNumber(), expMap.size());
            }
            int behSize = behMap.size();
            int expSize = expMap.size();
            int[][] behGraph = new int[behSize][behSize];
            int[][] expGraph = new int[expSize][expSize];


            for (int m = 0; m < behSize; m++) {
                for (int n = 0; n < behSize; n++) {
                    behGraph[m][n] = 0;
                }
            }
            for (int j = 0; j < scenarioDiagram.getScenarios().size(); j++) {
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if (scenario.getFrom().getState() == 0 && scenario.getTo().getState() == 0) {
                    behGraph[behMap.get(scenario.getFrom().getNumber())][behMap.get(scenario.getTo().getNumber())] = 1;
                }
            }
            for (int m = 0; m < expSize; m++) {
                for (int n = 0; n < expSize; n++) {
                    expGraph[m][n] = 0;
                }
            }
            for (int j = 0; j < scenarioDiagram.getScenarios().size(); j++) {
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if (scenario.getFrom().getState() == 1 && scenario.getTo().getState() == 1) {
                    expGraph[expMap.get(scenario.getFrom().getNumber())][expMap.get(scenario.getTo().getNumber())] = 1;
                }
            }


            //step2
            for(int j = 0;j < scenarioDiagram.getScenarios().size();j++){
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if(scenario.getState() == 0 || scenario.getState() == 2 || scenario.getState() == 4){
                    int behStart = -1;
                    int expStart = -1;
                    if(scenario.getFrom().getState() == 0 && scenario.getTo().getState() == 1){
                        behStart = behMap.get(scenario.getFrom().getNumber());
                        expStart = expMap.get(scenario.getTo().getNumber());
                    }
                    else{
                        behStart = behMap.get(scenario.getTo().getNumber());
                        expStart = expMap.get(scenario.getFrom().getNumber());
                    }
                    TestCircle behTc = new TestCircle(behMap.size(),behGraph);
                    TestCircle expTc = new TestCircle(expMap.size(),expGraph);
                    behTc.findCycle(behStart);
                    expTc.findCycle(expStart);
                    if(behTc.getHasCycle() && !expTc.getHasCycle()){
                        circle = circle + "Behaviour Interactions Has Circle";
                        circle = circle + "\"}";
                        System.out.println(circle);
                        return circle;
                    }
                    else if(!behTc.getHasCycle() && expTc.getHasCycle()){
                        circle = circle + "Expect Interactions Has Circle";
                        circle = circle + "\"}";
                        System.out.println(circle);
                        return circle;
                    }
                    if(behTc.getHasCycle() && expTc.getHasCycle()){
                        for(int m = 0;m < behTc.getCircles().size();m++){
                            ArrayList<Integer> behCircle = behTc.getCircles().get(m);
                            int behLast = behMap.inverse().get(behCircle.get(behCircle.size() - 1));
                            System.out.println("behlast:" + behLast);
                            if(behMap.inverse().get(behCircle.get(0)) == behMap.inverse().get(behStart)){
                                for(int n = 0;n < expTc.getCircles().size();n++){
                                    boolean flag = false;
                                    ArrayList<Integer> expCircle = expTc.getCircles().get(n);
                                    if(expMap.inverse().get(expCircle.get(0)) == expMap.inverse().get(expStart)){
                                        int expLast = expMap.inverse().get(expCircle.get(expCircle.size() - 1));
                                        System.out.println("expLast:" + expLast);
                                        for(int k = 0;k < scenarioDiagram.getScenarios().size();k++){
                                            Scenario tmpScenario = scenarioDiagram.getScenarios().get(k);
                                            if(tmpScenario.getState() == 0 || tmpScenario.getState() == 2 ||tmpScenario.getState() == 4){
                                                if(tmpScenario.getFrom().getNumber() == behLast && tmpScenario.getTo().getNumber() == expLast
                                                        || tmpScenario.getTo().getNumber() == behLast && tmpScenario.getFrom().getNumber() == expLast){
                                                    flag = true;
                                                    System.out.println("11111111");
                                                    break;
                                                }
                                            }
                                        }
                                        if(!flag){
                                            circle = circle + "Both Expect Interactions And Behaviour Interactions Have Circle";
                                            circle = circle + "\"}";
                                            System.out.println(circle);
                                            return circle;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //step3
            BiMap<Integer, Integer> map = HashBiMap.create();
            for(int j = 0;j < scenarioDiagram.getInteractions().size();j++){
                Interaction interaction = scenarioDiagram.getInteractions().get(j);
                map.put(interaction.toNum(),map.size());
            }
            int mapSize = map.size();
            int[][] graph = new int[mapSize][mapSize];
            for(int m = 0;m < mapSize;m++){
                for(int n = 0;n < mapSize;n++){
                    graph[m][n] = 0;
                }
            }

            List<Scenario> synchronize = new LinkedList<>();
            for(int j = 0;j < scenarioDiagram.getScenarios().size();j++){
                Scenario scenario = scenarioDiagram.getScenarios().get(j);
                if(scenario.getState() != 2){
                    graph[map.get(scenario.getFrom().toNum())]
                            [map.get(scenario.getTo().toNum())] = 1;
                }
                else {
                    synchronize.add(scenario);
                }
            }
            for(int j = 0;j < Math.pow(2,synchronize.size());j++){
                String bin = Integer.toBinaryString(j);
                while(bin.length() < synchronize.size()) bin = "0" + bin;
                for(int k = 0;k < synchronize.size();k++){
                    if(bin.charAt(k) == '0')
                        graph[map.get(synchronize.get(k).getFrom().toNum())]
                                [map.get(synchronize.get(k).getTo().toNum())] = 1;
                    else
                        graph[map.get(synchronize.get(k).getTo().toNum())]
                                [map.get(synchronize.get(k).getFrom().toNum())] = 1;
                }
                for(int m = 0;m < scenarioDiagram.getScenarios().size();m++){
                    Scenario scenario = scenarioDiagram.getScenarios().get(m);
                    if(scenario.getState() == 0){
                        Interaction interaction = scenario.getFrom();
                        TestCircle tc = new TestCircle(mapSize, graph);
                        tc.findCycle(map.get(interaction.toNum()));
                        if(tc.getHasCycle()){
                            for(int n = 0;n < tc.getCircles().size();n++){
                                ArrayList<Integer> circles = tc.getCircles().get(n);
                                if(map.inverse().get(circles.get(0)) == interaction.toNum()){
                                    List<Integer> states = new LinkedList<>();
                                    for(int t = 0;t < circles.size();t++){
                                        if(map.inverse().get(circles.get(t)) > 0){
                                            states.add(1);
                                        }
                                        else{
                                            states.add(0);
                                        }
                                    }
                                    if(states.contains(0) && states.contains(1)){
                                        for(int t = 0;t < circles.size();t++){
                                            if(map.inverse().get(circles.get(t)) > 0){
                                                circle = circle + "1" + "," + map.inverse().get(circles.get(t));
                                                circle = circle + ";";
                                            }
                                            else{
                                                circle = circle + "0" + "," + (-1 * map.inverse().get(circles.get(t)));
                                                circle = circle + ";";
                                            }
                                        }
                                        circle = circle + i;
                                        circle = circle + "\"}";
                                        return circle;
                                    }
                                }

                            }
                        }
                    }
                }
                for(int k = 0;k < synchronize.size();k++){
                    if(bin.charAt(k) == '0')
                        graph[map.get(synchronize.get(k).getFrom().toNum())]
                                [map.get(synchronize.get(k).getTo().toNum())] = 0;
                    else
                        graph[map.get(synchronize.get(k).getTo().toNum())]
                                [map.get(synchronize.get(k).getFrom().toNum())] = 0;
                }
            }
        }
        circle = circle + "\"}";
        return circle;
    }

    //3:20,0 StrictPre 21,0/4:
    public String loadConstraintsXML(String path) throws DocumentException {
        JSONObject constraints = new JSONObject();
        JSONObject newClockConstraints = new JSONObject();
        String result = "{\"constraints\":\"";
        File file = new File(path);
        if(!file.exists()){
            result = result + "NotExist" + "\"}";
            return result;
        }
        SAXReader saxReader = new SAXReader();
        Document project = saxReader.read(file);
        Element rootElement = project.getRootElement();
        for(Iterator it = rootElement.elementIterator("constraint");it.hasNext();){
            Element element = (Element) it.next();
            //TD0:int1state0 Union int5state1 newclock
            String constraint = element.getText();
            if(constraint.contains("TD")){
                String from = constraint.substring(constraint.indexOf(":") + 1).split(" ")[0];
                String cons = constraint.substring(constraint.indexOf(":")).split(" ")[1];
                String to = constraint.substring(constraint.indexOf(":")).split(" ")[2];
                String extra = "";

                result = result + constraint.substring(2,constraint.indexOf(":")) + ':';
                if(from.contains("int") && from.contains("state")){
                    result = result + from.substring(3,from.indexOf("state")) + ",";
                    result = result + from.substring(from.indexOf("state") + 5) + " ";
                }
                else result = result + from + " ";
                result = result + cons + " ";
                if(to.contains("int") && to.contains("state")){
                    result = result + to.substring(3,to.indexOf("state")) + ",";
                    result = result + to.substring(to.indexOf("state") + 5);
                }
                else result = result + to;
                if(cons.equals("Union") || cons.equals("Inf")
                        || cons.equals("Sup") || cons.equals("BoundedDiff")){
                    extra = constraint.split(" ")[3];
                    result = result + " " + extra + "/";
                }
                else result = result + "/";
            }
        }
        result = result + "\"}";
        System.out.println(result);
        return result;
    }

    // .txt --> .myccsl
    public void toMyCCSLFormat(String srcFileName, String destFileName, String path, int bound) {
        new ToMyCCSLFormat(srcFileName,destFileName,path,bound);
    }

}
