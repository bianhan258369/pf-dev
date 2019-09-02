package com.ECNU.bean;

import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

@Data
public class ScenarioDiagram {
    private LinkedList<Scenario> scenarios = new LinkedList<>();
    private LinkedList<Interaction> interactions = new LinkedList<>();
    private String title;
    private int biaohao;

    public ScenarioDiagram(String title, int biaohao) {
        this.title = title;
        this.biaohao = biaohao;
    }

    public ScenarioDiagram(String title, int biaohao, File file){
        this.title = title;
        this.biaohao = biaohao;
        try{
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(file);
            Element root = document.getRootElement().elementIterator("data").next();
            Element temp;

            Element intNode = root.elementIterator("IntNode").next();
            Element lineNode = root.elementIterator("LineNode").next();

            Element actIntNode = intNode.elementIterator("ActIntNode").next();
            for(Iterator i = actIntNode.elementIterator("Element"); i.hasNext();){
                temp = (Element)i.next();
                String str[] = temp.attributeValue("middleXY").split(",");
                int x = Integer.parseInt(str[0]);
                int y = Integer.parseInt(str[1]);
                int number = Integer.parseInt(temp.attributeValue("number"));
                int state = Integer.parseInt(temp.attributeValue("state"));
                String name = temp.attributeValue("name");
                Interaction tempJiaohu = new Interaction(x,y,number,state);
                tempJiaohu.setName(name);
                interactions.add(tempJiaohu);
            }

            Element expectIntNode = intNode.elementIterator("ExpectIntNode").next();
            for(Iterator i = expectIntNode.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[] = temp.attributeValue("middleXY").split(",");
                int x = Integer.parseInt(str[0]);
                int y = Integer.parseInt(str[1]);
                int number = Integer.parseInt(temp.attributeValue("number"));
                int state = Integer.parseInt(temp.attributeValue("state"));
                String name = temp.attributeValue("name");
                Interaction tempJiaohu = new Interaction(x,y,number,state);
                tempJiaohu.setName(name);
                interactions.add(tempJiaohu);
            }

            Element actCause = lineNode.elementIterator("ActCause").next();
            for(Iterator i = actCause.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = Integer.parseInt(temp.attributeValue("state"));
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element actOrder = lineNode.elementIterator("ActOrder").next();
            for(Iterator i = actOrder.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                if((fState != 0 && fState != 1) || (tState != 0 && tState != 1)) continue;
                to.setName(tName);
                int state = Integer.parseInt(temp.attributeValue("state"));
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element synchrony = lineNode.elementIterator("Synchrony").next();
            for(Iterator i = synchrony.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = Integer.parseInt(temp.attributeValue("state"));
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element expectOrder = lineNode.elementIterator("ExpectOrder").next();
            for(Iterator i = expectOrder.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                if((fState != 0 && fState != 1) || (tState != 0 && tState != 1)) continue;
                int state = Integer.parseInt(temp.attributeValue("state"));
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element expectCause = lineNode.elementIterator("ExpectCause").next();
            for(Iterator i = expectCause.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = Integer.parseInt(temp.attributeValue("state"));
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            for(Iterator i = actOrder.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                if(tState == 4){
                    Element decisionNode = intNode.elementIterator("DecisionNode").next();
                    for(Iterator j = decisionNode.elementIterator("Element");j.hasNext();){
                        Element tempDecisionNode = (Element)j.next();
                        if(Integer.parseInt(tempDecisionNode.attributeValue("number"))==tNumber){
                            int dx1 = Integer.parseInt(tempDecisionNode.attributeValue("to1XY").split(",")[0]);
                            int dy1 = Integer.parseInt(tempDecisionNode.attributeValue("to1XY").split(",")[1]);
                            int dNumber1 = Integer.parseInt(tempDecisionNode.attributeValue("to1Num"));
                            int dState1 = Integer.parseInt(tempDecisionNode.attributeValue("to1State"));
                            int dx2 = Integer.parseInt(tempDecisionNode.attributeValue("to2XY").split(",")[0]);
                            int dy2 = Integer.parseInt(tempDecisionNode.attributeValue("to2XY").split(",")[1]);
                            int dNumber2 = Integer.parseInt(tempDecisionNode.attributeValue("to2Num"));
                            int dState2 = Integer.parseInt(tempDecisionNode.attributeValue("to2State"));
                            Interaction left = new Interaction(dx1, dy1, dNumber1, dState1);
                            Interaction right = new Interaction(dx2, dy2, dNumber2, dState2);
                            scenarios.add(new Scenario(new LinkedList(), from, left, 1));
                            scenarios.add(new Scenario(new LinkedList(), from, right, 1));
                        }
                    }
                }
                if(tState == 5){
                    Element mergeNode = intNode.elementIterator("MergeNode").next();
                    for(Iterator j = mergeNode.elementIterator("Element");j.hasNext();){
                        Element tempMergeNode = (Element)j.next();
                        int mx = Integer.parseInt(tempMergeNode.attributeValue("toXY").split(",")[0]);
                        int my = Integer.parseInt(tempMergeNode.attributeValue("toXY").split(",")[1]);
                        int mNumber = Integer.parseInt(tempMergeNode.attributeValue("toNum"));
                        int mState = Integer.parseInt(tempMergeNode.attributeValue("toState"));
                        Interaction merge = new Interaction(mx, my, mNumber, mState);
                        scenarios.add(new Scenario(new LinkedList(), from, merge, 1));
                    }
                }
            }

            for(Iterator i = expectOrder.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("fromXY").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("fromXY").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("fromNum"));
                int fState = Integer.parseInt(temp.attributeValue("fromState"));
                String fName = temp.attributeValue("fromName");
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("toXY").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("toXY").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("toNum"));
                int tState = Integer.parseInt(temp.attributeValue("toState"));
                String tName = temp.attributeValue("toName");
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                if(tState == 4){
                    Element decisionNode = intNode.elementIterator("DecisionNode").next();
                    for(Iterator j = decisionNode.elementIterator("Element");j.hasNext();){
                        Element tempDecisionNode = (Element)j.next();
                        if(Integer.parseInt(tempDecisionNode.attributeValue("number"))==tNumber){
                            int dx1 = Integer.parseInt(tempDecisionNode.attributeValue("to1XY").split(",")[0]);
                            int dy1 = Integer.parseInt(tempDecisionNode.attributeValue("to1XY").split(",")[1]);
                            int dNumber1 = Integer.parseInt(tempDecisionNode.attributeValue("to1Num"));
                            int dState1 = Integer.parseInt(tempDecisionNode.attributeValue("to1State"));
                            int dx2 = Integer.parseInt(tempDecisionNode.attributeValue("to2XY").split(",")[0]);
                            int dy2 = Integer.parseInt(tempDecisionNode.attributeValue("to2XY").split(",")[1]);
                            int dNumber2 = Integer.parseInt(tempDecisionNode.attributeValue("to2Num"));
                            int dState2 = Integer.parseInt(tempDecisionNode.attributeValue("to2State"));
                            Interaction left = new Interaction(dx1, dy1, dNumber1, dState1);
                            Interaction right = new Interaction(dx2, dy2, dNumber2, dState2);
                            scenarios.add(new Scenario(new LinkedList(), from, left, 3));
                            scenarios.add(new Scenario(new LinkedList(), from, right, 3));
                        }
                    }
                }
                if(tState == 5){
                    Element mergeNode = intNode.elementIterator("MergeNode").next();
                    for(Iterator j = mergeNode.elementIterator("Element");j.hasNext();){
                        Element tempMergeNode = (Element)j.next();
                        int mx = Integer.parseInt(tempMergeNode.attributeValue("toXY").split(",")[0]);
                        int my = Integer.parseInt(tempMergeNode.attributeValue("toXY").split(",")[1]);
                        int mNumber = Integer.parseInt(tempMergeNode.attributeValue("toNum"));
                        int mState = Integer.parseInt(tempMergeNode.attributeValue("toState"));
                        Interaction merge = new Interaction(mx, my, mNumber, mState);
                        scenarios.add(new Scenario(new LinkedList(), from, merge, 3));
                    }
                }
            }

            Element branchNode = intNode.elementIterator("BranchNode").next();
            for(Iterator i = branchNode.elementIterator("Element");i.hasNext();){
                Element tempBranchNode = (Element)i.next();
                for(Iterator it = tempBranchNode.elementIterator("from");it.hasNext();){
                    Element tempFrom = (Element)it.next();
                    int fx = Integer.parseInt(tempFrom.attributeValue("middleXY").split(",")[0]);
                    int fy = Integer.parseInt(tempFrom.attributeValue("middleXY").split(",")[1]);
                    int fNumber = Integer.parseInt(tempFrom.attributeValue("number"));
                    int fState = Integer.parseInt(tempFrom.attributeValue("state"));
                    if(fState != 0 && fState != 1) continue;
                    String fName = tempFrom.attributeValue("name");
                    Interaction from = new Interaction(fx, fy, fNumber, fState);
                    from.setName(fName);
                    for(Iterator j = tempBranchNode.elementIterator("to");j.hasNext();){
                        Element temoTo = (Element)j.next();
                        int tx = Integer.parseInt(temoTo.attributeValue("middleXY").split(",")[0]);
                        int ty = Integer.parseInt(temoTo.attributeValue("middleXY").split(",")[1]);
                        int tNumber = Integer.parseInt(temoTo.attributeValue("number"));
                        int tState = Integer.parseInt(temoTo.attributeValue("state"));
                        if(tState != 0 && tState != 1) continue;
                        String tName = temoTo.attributeValue("name");
                        Interaction to = new Interaction(tx, ty, tNumber, tState);
                        to.setName(tName);
                        if (fState == 0) scenarios.add(new Scenario(new LinkedList(), from, to, 1));
                        if (fState == 1) scenarios.add(new Scenario(new LinkedList(), from, to, 3));
                    }
                }
            }

            /*
            LinkedList<Integer> replaces = new LinkedList<>();
            for(int i = 0;i < scenarios.size();i++){
                Scenario tempChangjing = scenarios.get(i);
                if(tempChangjing.getState() == 0){
                    int index = 0;
                    Interaction tempFrom = tempChangjing.getFrom();
                    Interaction tempTo = tempChangjing.getTo();
                    LinkedList<Interaction> precedent = new LinkedList<>();
                    LinkedList<Interaction> successor = new LinkedList<>();
                    getBehaviourPrecedent(tempFrom, precedent);
                    getExpectedSuccessor(tempTo, successor);
                    if(precedent.size() > 0) {
                        while (index < precedent.size()) {
                            getBehaviourPrecedent(precedent.get(index), precedent);
                            index++;
                        }
                    }
                    if(successor.size() > 0){
                        index = 0;
                        while(index < successor.size()){
                            getExpectedSuccessor(successor.get(index), successor);
                            index++;
                        }
                    }
                    for(int j = 0;j < precedent.size();j++){
                        Interaction tempPrecedent = precedent.get(j);
                        for(int k = 0;k < successor.size();k++){
                            Interaction tempSuccessor = successor.get(k);
                            if(tempPrecedent.getNumber() == tempSuccessor.getNumber()){
                                if(!replaces.contains(i)) replaces.add(i);
                            }
                        }
                    }
                }
            }

            for(int i = 0;i < replaces.size();i++){
                Scenario replace = scenarios.get(replaces.get(i));
                scenarios.remove(replace);
                LinkedList<String> dians = replace.getTurning();
                LinkedList newDians = new LinkedList();
                newDians.add(dians.get(2));
                newDians.add(dians.get(3));
                newDians.add(dians.get(0));
                newDians.add(dians.get(1));
                scenarios.add(new Scenario(newDians, replace.getTo(), replace.getFrom(), replace.getState()));
            }
            */
        }catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    //找到一个行为交互的所有前驱
    private void getBehaviourPrecedent(Interaction jiaohu, LinkedList<Interaction> Precedent){
        for(int i = 0;i < scenarios.size();i++){
            Scenario tempChangjing = scenarios.get(i);
            if(tempChangjing.getState() == 1 && tempChangjing.getTo().getNumber() == jiaohu.getNumber()){
                Precedent.add(tempChangjing.getFrom());
            }
        }
    }

    //找到一个期望交互的所有后继
    private void getExpectedSuccessor(Interaction jiaohu, LinkedList<Interaction> successor){
        for(int i = 0;i < scenarios.size();i++){
            Scenario tempChangjing = scenarios.get(i);
            if(tempChangjing.getState() == 3 && tempChangjing.getFrom().getNumber() == jiaohu.getNumber()){
                successor.add(tempChangjing.getTo());
            }
        }
    }

    public void addChangjing(Scenario cj) {
        this.scenarios.add(cj);
    }

    public void addJiaohu(Interaction jh) {
        this.interactions.add(jh);
    }

    public Interaction getInteraction(int number, int state){
        for(int i = 0;i < interactions.size();i++){
            Interaction interaction = interactions.get(i);
            if(interaction.getState() == state && interaction.getNumber() == number){
                return interaction;
            }
        }
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ScenarioDiagram intDiagram = (ScenarioDiagram) super.clone();
        LinkedList<Interaction> tempJiaohu = new LinkedList<>();
        LinkedList<Scenario> tempChangjing = new LinkedList<>();
        for(int i = 0;i < this.interactions.size();i++){
            tempJiaohu.add((Interaction) (this.interactions.get(i)).clone());
        }
        for(int i = 0;i < this.scenarios.size();i++){
            tempChangjing.add((Scenario) (this.scenarios.get(i)).clone());
        }
        intDiagram.scenarios = tempChangjing;
        intDiagram.interactions = tempJiaohu;
        return intDiagram;
    }
}
