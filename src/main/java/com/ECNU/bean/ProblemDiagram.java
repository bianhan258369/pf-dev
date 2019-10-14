package com.ECNU.bean;

import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

@Data
public class ProblemDiagram implements Serializable {
    public LinkedList<Object> components;
    private String title;
    private String interactionDescription = "";

    public ProblemDiagram(String title) {
        this.title = title;
        this.components = new LinkedList();
    }

    public ProblemDiagram(String title, File file){
        this.title = title;
        this.components = new LinkedList();
        try {

            SAXReader saxReader = new SAXReader();
            String path = getFilePath(file.getPath());
            Document project = saxReader.read(file);
            Element rootElement = project.getRootElement();
            Element filelist = rootElement.elementIterator("fileList").next();
            String cdPath = path + filelist.elementIterator("ContextDiagram").next().getText() + ".xml";
            String pdPath = path + filelist.elementIterator("ProblemDiagram").next().getText() + ".xml";

            Iterator it;

            Document contextDiagram = saxReader.read(cdPath);
            Document problemDiagram = saxReader.read(pdPath);

            rootElement = contextDiagram.getRootElement();
            Element cdRoot = rootElement;

            rootElement = problemDiagram.getRootElement();
            Element pdRoot = rootElement;

            Element temp;
            for(Iterator i = cdRoot.elementIterator("Machine");i.hasNext();){
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
                this.components.add(rect);
            }

            Element requirement = (Element) pdRoot.elementIterator("Requirement").next();
            for(Iterator i = requirement.elementIterator("Element");i.hasNext();){
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
                this.components.add(oval);
            }

            Element problemDomain = (Element) cdRoot.elementIterator("ProblemDomain").next();
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
                //System.out.println(rect.getText());
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                this.components.add(rect);
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
                //System.out.println(rect.getText());
                rect.setShortName(temp.attributeValue("problemdomain_shortname"));
                rect.setState(1);
                rect.setCxb(temp.attributeValue("problemdomain_type").charAt(0));
                this.components.add(rect);
            }

            Element Interface = (Element) cdRoot.elementIterator("Interface").next();
            for(Iterator i = Interface.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("interface_no");
                String to = temp.attributeValue("interface_to");
                String from = temp.attributeValue("interface_from");
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < this.components.size();j++){
                    Shape tempShape = (Shape)this.components.get(j);
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
                    for(int k = 0;k < this.components.size();k++){
                        Shape tempShape = (Shape)this.components.get(k);
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
                this.components.add(line);
            }

            Element reference = pdRoot.elementIterator("Reference").next();
            for(Iterator i = reference.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("reference_name");
                String to = temp.attributeValue("reference_to");
                String from = temp.attributeValue("reference_from");
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < this.components.size();j++){
                    Shape tempShape = (Shape)this.components.get(j);
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
                    for(int k = 0;k < this.components.size();k++){
                        Shape tempShape = (Shape)this.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = this.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                this.components.add(line);
            }

            Element constraint = (Element) pdRoot.elementIterator("Constraint").next();
            for(Iterator i = constraint.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String name = temp.attributeValue("constraint_name");
                String to = temp.attributeValue("constraint_to");
                String from = temp.attributeValue("constraint_from");
                Shape toShape = null;
                Shape fromShape = null;
                for(int j = 0;j < this.components.size();j++){
                    Shape tempShape = (Shape)this.components.get(j);
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
                    for(int k = 0;k < this.components.size();k++){
                        Shape tempShape = (Shape)this.components.get(k);
                        if(tempShape instanceof Rect){
                            Rect tempRect = (Rect)tempShape;
                            if(tempRect.getShortName().equals(phenomenonFrom)) phenomenonFromRect = tempRect;
                            if(tempRect.getShortName().equals(phenomenonTo)) phenomenonToRect = tempRect;
                        }
                    }
                    Oval oval = this.getRequirement(pehnomenonRequirementBiaohao);
                    Phenomenon phenomenon = new Phenomenon(phenomenonName, phenomenonState, phenomenonFromRect, phenomenonToRect);
                    phenomenon.setConstraining(phenomenonConstraining);
                    phenomenon.setBiaohao(phenomenonBiaohao);
                    phenomenon.setRequirement(oval);
                    line.getPhenomena().add(phenomenon);
                }
                this.components.add(line);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasSub(String path) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(path);
        Element rootElement = document.getRootElement();
        Iterator it = rootElement.elementIterator("filelist");
        Element root = (Element) it.next();
        return root.elementIterator("SubProblemDiagramList").next().elementIterator("SubProblemDiagram").hasNext();
    }

    public static String getFilePath(String path){
        int index = path.lastIndexOf('\\');
        //int index = path.lastIndexOf('/');
        String result = path.substring(0,index+1);
        return result;
    }

    public Oval getRequirement(int num) {
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 1) {
                Oval tmp_o = (Oval) tmp;
                if (tmp_o.getBiaohao() == num) {
                    return tmp_o;
                }
            }
        }
        return null;
    }

    public void add(Shape component) {
        for (int i = 0; i <= this.components.size() - 1; i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.equals(component)) {
                return;
            }
        }
        this.components.add(component);
    }

    public LinkedList<Rect> getRect(){
        LinkedList ll = new LinkedList();
        for(int i = 0;i < components.size();i++){
            Shape shape = (Shape) components.get(i);
            if(shape.getShape() == 0) ll.add((Rect) shape);
        }
        return ll;
    }

    public LinkedList getPhenomenon() {
        LinkedList ll = new LinkedList();
        for (int i = 0; i <= this.components.size() - 1; i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 2) {
                Line tmp_l = (Line) tmp_s;
                for (int j = 0; j <= tmp_l.getPhenomena().size() - 1; j++) {
                    Phenomenon tmp_p = (Phenomenon) tmp_l.getPhenomena().get(j);
                    boolean jia = true;
                    for (int k = 0; k <= ll.size() - 1; k++) {
                        Phenomenon tmp1 = (Phenomenon) ll.get(k);
                        if ((!tmp1.getName().equals(tmp_p.getName()))
                                || (!tmp1.getState().equals(tmp_p.getState())))
                            continue;
                        jia = false;
                    }

                    if (jia) {
                        ll.add(tmp_p);
                    }
                }
            }
        }
        return ll;
    }

    public LinkedList getReference() {
        LinkedList ll = new LinkedList();
        for (int i = 0; i <= this.components.size() - 1; i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 2) {
                Line tmp_l = (Line) tmp_s;
                if (tmp_l.getState() == 0) {
                    continue;
                }
                for (int j = 0; j <= tmp_l.getPhenomena().size() - 1; j++) {
                    ll.add((Phenomenon) tmp_l.getPhenomena().get(j));
                }
            }
        }

        return ll;
    }

    public void addInterface(int i) {
        Line line = new Line((Shape) this.components.get(0),
                (Shape) this.components.get(1), i);

        setLineName();
        this.components.addFirst(line);
    }

    public Rect getMachine() {
        for (int i = 0; i < this.components.size(); i++) {
            if (((Shape) this.components.get(i)).getShape() == 0) {
                Rect rr = (Rect) this.components.get(i);
                if (rr.getShape() == 2) {
                    return rr;
                }
            }
        }
        return null;
    }


    public int hasMachine() {
        int count = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 0) {
                Rect tmp_r = (Rect) tmp_s;
                if (tmp_r.getShape() == 2) {
                    count++;
                }
            }
        }
        return count;
    }

    public int hasInterface() {
        int count = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 2) {
                Line tmp_r = (Line) tmp_s;
                if (tmp_r.getState() == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public void setLineName() {
        int j = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 2) {
                String s = "" + (char) (97 + j);
                j++;
                ((Line) tmp).setName(s);
            }
        }
    }

    public LinkedList getRequirements() {
        LinkedList ll = new LinkedList();
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 1) {
                ll.add(tmp);
            }
        }
        return ll;
    }

    public LinkedList getProblemDomains() {
        LinkedList ll = new LinkedList();
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 0) {
                if(((Rect)tmp).getState() != 2) ll.add(tmp);
            }
        }
        return ll;
    }

    public LinkedList getLines(){
        LinkedList ll = new LinkedList();
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 2 ) {
                ll.add(tmp);
            }
        }
        return ll;
    }

    public int hasProDom() {
        int count = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 0) {
                Rect tmp_r = (Rect) tmp_s;
                if ((tmp_r.getState() == 1) || (tmp_r.getState() == 0)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int hasReqCon() {
        int count = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 2) {
                Line tmp_r = (Line) tmp_s;
                if (tmp_r.getState() == 2) {
                    count++;
                }
            }
        }
        return count;
    }

    public int hasReqRef() {
        int count = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 2) {
                Line tmp_r = (Line) tmp_s;
                if (tmp_r.getState() == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    public int hasReq() {
        int count = 0;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp_s = (Shape) this.components.get(i);
            if (tmp_s.getShape() == 1) {
                count++;
            }
        }
        return count;
    }

    public String getInteractionDescription() {
        this.interactionDescription = "";
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 2) {
                Line tmp_l = (Line) tmp;
                this.interactionDescription = (this.interactionDescription
                        + tmp_l.getDescription() + "\n");
            }
        }

        this.interactionDescription += "\n";
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp = (Shape) this.components.get(i);
            if (tmp.getShape() == 1) {
                Oval tmp_l = (Oval) tmp;
                this.interactionDescription = (this.interactionDescription
                        + "req" + tmp_l.getBiaohao() + ":" + tmp_l.getText() + "\n");
            }

        }
        return this.interactionDescription;
    }

    public boolean find(Shape a, Shape b) {
        for (int i = 0; i < this.components.size(); i++) {
            if (((Shape) this.components.get(i)).getShape() == 2) {
                Line line = (Line) this.components.get(i);
                if ((line.getFrom().equals(a)) && (line.getTo().equals(b))) {
                    return true;
                }
                if ((line.getFrom().equals(b)) && (line.getTo().equals(a))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void rule() {
        Rect machine = getMachine();
        if (machine == null) {
            return;
        }
        for (int i = 0; i < this.components.size(); i++)
            if (((Shape) this.components.get(i)).getShape() == 0) {
                Rect rr = (Rect) this.components.get(i);
                if ((rr.getState() == 2) || (find(machine, rr)))
                    continue;
                add(new Line(machine, rr, 0));
                setLineName();
            }
    }
    public void delete(Shape shape) {
        if (shape.getShape() == 2) {
            for (int i = 0; i < this.components.size(); i++) {
                Shape tmp = (Shape) this.components.get(i);
                if (shape.equals(tmp))
                    this.components.remove(i);
            }
        } else {
            boolean k = false;
            for (int i = 0; i < this.components.size(); i++) {
                Shape tmp = (Shape) this.components.get(i);
                if (tmp.getShape() == 2) {
                    Line tmpLine = (Line) tmp;
                    if ((tmpLine.getFrom().equals(shape))
                            || (tmpLine.getTo().equals(shape))) {
                        this.components.remove(i);
                        i--;
                        if (i == -1) {
                            k = true;
                            i++;
                        }
                    }
                }
                if (tmp.equals(shape)) {
                    this.components.remove(i);
                    i--;
                    if (i == -1) {
                        k = true;
                        i++;
                    }
                }
            }
            if (k) {
                if (this.components.size() == 0) {
                    return;
                }
                Shape tmp = (Shape) this.components.get(0);
                if (tmp.getShape() == 2) {
                    Line tmpLine = (Line) tmp;
                    if ((tmpLine.getFrom().equals(shape))
                            || (tmpLine.getTo().equals(shape))) {
                        this.components.remove(0);
                    }
                }
                if (tmp.equals(shape))
                    this.components.remove(0);
            }
        }
    }

    public Line findALine(Shape r1, Shape r2) {
        Line re = null;
        for (int i = 0; i < this.components.size(); i++) {
            Shape tmp1 = (Shape) this.components.get(i);
            if (tmp1.getShape() == 2) {
                Line tmp_line = (Line) tmp1;
                if ((com.ECNU.bean.Data.same(tmp_line.getFrom(), r1))
                        && (com.ECNU.bean.Data.same(tmp_line.getTo(), r2))) {
                    re = tmp_line;
                    break;
                }
            }
        }
        return re;
    }
}
