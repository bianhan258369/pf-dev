package com.ECNU.bean;

import com.ECNU.util.TestCircle;
import lombok.Data;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

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
            Element root = document.getRootElement();
            Element nodeRoot = root.elementIterator("NodeList").next();
            Element temp;

            Element intNode = nodeRoot.elementIterator("IntNode").next();
            Element controlNode = nodeRoot.elementIterator("ControlNode").next();
            Element lineNode = root.elementIterator("LineList").next();

            Element actIntNode = intNode.elementIterator("BehIntNode").next();
            for(Iterator i = actIntNode.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[] = temp.attributeValue("node_locality").split(",");
                int x = Integer.parseInt(str[0]);
                int y = Integer.parseInt(str[1]);
                int number = Integer.parseInt(temp.attributeValue("node_no"));
                int state = 0;
                String name = "int";
                Interaction tempJiaohu = new Interaction(x,y,number,state);
                tempJiaohu.setName(name);
                interactions.add(tempJiaohu);
            }

            Element expectIntNode = intNode.elementIterator("ExpIntNode").next();
            for(Iterator i = expectIntNode.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[] = temp.attributeValue("node_locality").split(",");
                int x = Integer.parseInt(str[0]);
                int y = Integer.parseInt(str[1]);
                int number = Integer.parseInt(temp.attributeValue("node_no"));
                int state = 1;
                String name = "int";
                Interaction tempJiaohu = new Interaction(x,y,number,state);
                tempJiaohu.setName(name);
                interactions.add(tempJiaohu);
            }

            Element actCause = lineNode.elementIterator("BehEnable").next();
            for(Iterator i = actCause.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = 0;
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element actOrder = lineNode.elementIterator("BehOrder").next();
            for(Iterator i = actOrder.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String fType = temp.attributeValue("from_type");
                String tType = temp.attributeValue("to_type");
                if(!fType.equals("BehInt") || !tType.equals("BehInt")) continue;
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = 1;
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
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = 2;
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element expectOrder = lineNode.elementIterator("ExpOrder").next();
            for(Iterator i = expectOrder.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String fType = temp.attributeValue("from_type");
                String tType = temp.attributeValue("to_type");
                if(!fType.equals("ExpInt") || !tType.equals("ExpInt")) continue;
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = 3;
                Scenario tempChangjing = new Scenario(list, from, to,state);
                scenarios.add(tempChangjing);
            }

            Element expectCause = lineNode.elementIterator("ExpEnable").next();
            for(Iterator i = expectCause.elementIterator("Element");i.hasNext();){
                temp = (Element)i.next();
                String str[];
                if(temp.attributeValue("turnings").contains(",")) str = temp.attributeValue("turnings").split(",");
                else str = new String[0];
                LinkedList list = new LinkedList();
                for(int j = 0;j < str.length;j++) list.add(str[j]);
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                int state = 4;
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
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                if(fState != 0) continue;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                if(tState == 4){
                    Element decisionNode = controlNode.elementIterator("DecisionNode").next();
                    for(Iterator j = decisionNode.elementIterator("Element");j.hasNext();){
                        Element tempDecisionNode = (Element)j.next();
                        if(Integer.parseInt(tempDecisionNode.attributeValue("node_no"))==tNumber){
                            for(Iterator k = tempDecisionNode.elementIterator("to");k.hasNext();){
                                Element tempFrom = (Element)k.next();
                                int dx = Integer.parseInt(tempFrom.attributeValue("node_locality").split(",")[0]);
                                int dy = Integer.parseInt(tempFrom.attributeValue("node_locality").split(",")[1]);
                                int dNumber = Integer.parseInt(tempFrom.attributeValue("node_no"));
                                int dState = -1;
                                if(tempFrom.attributeValue("node_type").equals("Start")) dState = 2;
                                else if(tempFrom.attributeValue("node_type").equals("BehInt")) dState = 0;
                                else if(tempFrom.attributeValue("node_type").equals("ExpInt")) dState = 1;
                                else if(tempFrom.attributeValue("node_type").equals("End")) dState = 3;
                                else if(tempFrom.attributeValue("node_type").equals("Decision")) dState = 4;
                                else if(tempFrom.attributeValue("node_type").equals("Merge")) dState = 5;
                                else if(tempFrom.attributeValue("node_type").equals("Branch")) dState = 6;
                                Interaction dJiaohu = new Interaction(dx, dy, dNumber, dState);
                                if(dState == 0) scenarios.add(new Scenario(new LinkedList(), from, dJiaohu, 1));
                            }
                        }
                    }
                }
                if(tState == 5){
                    Element mergeNode = controlNode.elementIterator("MergeNode").next();
                    for(Iterator j = mergeNode.elementIterator("Element");j.hasNext();){
                        Element tempMergeNode = (Element)j.next();
                        Element tempTo = (Element)tempMergeNode.elementIterator("to").next();
                        int mx = Integer.parseInt(tempTo.attributeValue("node_locality").split(",")[0]);
                        int my = Integer.parseInt(tempTo.attributeValue("node_locality").split(",")[1]);
                        int mNumber = Integer.parseInt(tempTo.attributeValue("node_no"));
                        int mState = -1;
                        if(tempTo.attributeValue("node_type").equals("Start")) mState = 2;
                        else if(tempTo.attributeValue("node_type").equals("BehInt")) mState = 0;
                        else if(tempTo.attributeValue("node_type").equals("ExpInt")) mState = 1;
                        else if(tempTo.attributeValue("node_type").equals("End")) mState = 3;
                        else if(tempTo.attributeValue("node_type").equals("Decision")) mState = 4;
                        else if(tempTo.attributeValue("node_type").equals("Merge")) mState = 5;
                        else if(tempTo.attributeValue("node_type").equals("Branch")) mState = 6;
                        Interaction merge = new Interaction(mx, my, mNumber, mState);
                        if(mState == 0) scenarios.add(new Scenario(new LinkedList(), from, merge, 1));
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
                int fx = Integer.parseInt(temp.attributeValue("from_locality").split(",")[0]);
                int fy = Integer.parseInt(temp.attributeValue("from_locality").split(",")[1]);
                int fNumber = Integer.parseInt(temp.attributeValue("from_no"));
                int fState = -1;
                if(temp.attributeValue("from_type").equals("Start")) fState = 2;
                else if(temp.attributeValue("from_type").equals("BehInt")) fState = 0;
                else if(temp.attributeValue("from_type").equals("ExpInt")) fState = 1;
                else if(temp.attributeValue("from_type").equals("End")) fState = 3;
                else if(temp.attributeValue("from_type").equals("Decision")) fState = 4;
                else if(temp.attributeValue("from_type").equals("Merge")) fState = 5;
                else if(temp.attributeValue("from_type").equals("Branch")) fState = 6;
                if(fState != 1) continue;
                String fName = "int";
                Interaction from = new Interaction(fx, fy, fNumber, fState);
                from.setName(fName);
                int tx = Integer.parseInt(temp.attributeValue("to_locality").split(",")[0]);
                int ty = Integer.parseInt(temp.attributeValue("to_locality").split(",")[1]);
                int tNumber = Integer.parseInt(temp.attributeValue("to_no"));
                int tState = -1;
                if(temp.attributeValue("to_type").equals("Start")) tState = 2;
                else if(temp.attributeValue("to_type").equals("BehInt")) tState = 0;
                else if(temp.attributeValue("to_type").equals("ExpInt")) tState = 1;
                else if(temp.attributeValue("to_type").equals("End")) tState = 3;
                else if(temp.attributeValue("to_type").equals("Decision")) tState = 4;
                else if(temp.attributeValue("to_type").equals("Merge")) tState = 5;
                else if(temp.attributeValue("to_type").equals("Branch")) tState = 6;
                String tName = "int";
                Interaction to = new Interaction(tx, ty, tNumber, tState);
                to.setName(tName);
                if(tState == 4){
                    Element decisionNode = controlNode.elementIterator("DecisionNode").next();
                    for(Iterator j = decisionNode.elementIterator("Element");j.hasNext();){
                        Element tempDecisionNode = (Element)j.next();
                        if(Integer.parseInt(tempDecisionNode.attributeValue("node_no"))==tNumber){
                            for(Iterator k = tempDecisionNode.elementIterator("to");k.hasNext();){
                                Element tempFrom = (Element)k.next();
                                int dx = Integer.parseInt(tempFrom.attributeValue("node_locality").split(",")[0]);
                                int dy = Integer.parseInt(tempFrom.attributeValue("node_locality").split(",")[1]);
                                int dNumber = Integer.parseInt(tempFrom.attributeValue("node_no"));
                                int dState = -1;
                                if(tempFrom.attributeValue("node_type").equals("Start")) dState = 2;
                                else if(tempFrom.attributeValue("node_type").equals("BehInt")) dState = 0;
                                else if(tempFrom.attributeValue("node_type").equals("ExpInt")) dState = 1;
                                else if(tempFrom.attributeValue("node_type").equals("End")) dState = 3;
                                else if(tempFrom.attributeValue("node_type").equals("Decision")) dState = 4;
                                else if(tempFrom.attributeValue("node_type").equals("Merge")) dState = 5;
                                else if(tempFrom.attributeValue("node_type").equals("Branch")) dState = 6;
                                Interaction dJiaohu = new Interaction(dx, dy, dNumber, dState);
                                if(dState == 1) scenarios.add(new Scenario(new LinkedList(), from, dJiaohu, 3));
                            }
                        }
                    }
                }
                if(tState == 5){
                    Element mergeNode = controlNode.elementIterator("MergeNode").next();
                    for(Iterator j = mergeNode.elementIterator("Element");j.hasNext();){
                        Element tempMergeNode = (Element)j.next();
                        Element tempTo = (Element)tempMergeNode.elementIterator("to").next();
                        int mx = Integer.parseInt(tempTo.attributeValue("node_locality").split(",")[0]);
                        int my = Integer.parseInt(tempTo.attributeValue("node_locality").split(",")[1]);
                        int mNumber = Integer.parseInt(tempTo.attributeValue("node_no"));
                        int mState = -1;
                        if(tempTo.attributeValue("node_type").equals("Start")) mState = 2;
                        else if(tempTo.attributeValue("node_type").equals("BehInt")) mState = 0;
                        else if(tempTo.attributeValue("node_type").equals("ExpInt")) mState = 1;
                        else if(tempTo.attributeValue("node_type").equals("End")) mState = 3;
                        else if(tempTo.attributeValue("node_type").equals("Decision")) mState = 4;
                        else if(tempTo.attributeValue("node_type").equals("Merge")) mState = 5;
                        else if(tempTo.attributeValue("node_type").equals("Branch")) mState = 6;
                        Interaction merge = new Interaction(mx, my, mNumber, mState);
                        if(mState == 1) scenarios.add(new Scenario(new LinkedList(), from, merge, 3));
                    }
                }
            }

            Element branchNode = controlNode.elementIterator("BranchNode").next();
            for(Iterator i = branchNode.elementIterator("Element");i.hasNext();){
                Element tempBranchNode = (Element)i.next();
                for(Iterator it = tempBranchNode.elementIterator("from");it.hasNext();){
                    Element tempFrom = (Element)it.next();
                    int fx = Integer.parseInt(tempFrom.attributeValue("node_locality").split(",")[0]);
                    int fy = Integer.parseInt(tempFrom.attributeValue("node_locality").split(",")[1]);
                    int fNumber = Integer.parseInt(tempFrom.attributeValue("node_no"));
                    int fState = -1;
                    if(tempFrom.attributeValue("node_type").equals("Start")) fState = 2;
                    else if(tempFrom.attributeValue("node_type").equals("BehInt")) fState = 0;
                    else if(tempFrom.attributeValue("node_type").equals("ExpInt")) fState = 1;
                    else if(tempFrom.attributeValue("node_type").equals("End")) fState = 3;
                    else if(tempFrom.attributeValue("node_type").equals("Decision")) fState = 4;
                    else if(tempFrom.attributeValue("node_type").equals("Merge")) fState = 5;
                    else if(tempFrom.attributeValue("node_type").equals("Branch")) fState = 6;
                    if(fState != 0 && fState != 1) continue;
                    String fName = "int";
                    Interaction from = new Interaction(fx, fy, fNumber, fState);
                    from.setName(fName);
                    for(Iterator j = tempBranchNode.elementIterator("to");j.hasNext();){
                        Element temoTo = (Element)j.next();
                        int tx = Integer.parseInt(temoTo.attributeValue("node_locality").split(",")[0]);
                        int ty = Integer.parseInt(temoTo.attributeValue("node_locality").split(",")[1]);
                        int tNumber = Integer.parseInt(temoTo.attributeValue("node_no"));
                        int tState = -1;
                        if(temoTo.attributeValue("node_type").equals("Start")) tState = 2;
                        else if(temoTo.attributeValue("node_type").equals("BehInt")) tState = 0;
                        else if(temoTo.attributeValue("node_type").equals("ExpInt")) tState = 1;
                        else if(temoTo.attributeValue("node_type").equals("End")) tState = 3;
                        else if(temoTo.attributeValue("node_type").equals("Decision")) tState = 4;
                        else if(temoTo.attributeValue("node_type").equals("Merge")) tState = 5;
                        else if(temoTo.attributeValue("node_type").equals("Branch")) tState = 6;
                        if(tState != 0 && tState != 1) continue;
                        String tName = "int";
                        Interaction to = new Interaction(tx, ty, tNumber, tState);
                        to.setName(tName);
                        if (fState == 0) scenarios.add(new Scenario(new LinkedList(), from, to, 1));
                        if (fState == 1) scenarios.add(new Scenario(new LinkedList(), from, to, 3));
                    }
                }
            }
            Collections.sort(scenarios, new Comparator<Scenario>() {
                @Override
                public int compare(Scenario o1, Scenario o2) {
                    return o1.getFrom().getY1() - o2.getFrom().getY1();
                }
            });

            //step1
            BiMap<Integer, Integer> behMap = HashBiMap.create();
            BiMap<Integer, Integer> expMap = HashBiMap.create();
            for (int j = 0; j < interactions.size(); j++) {
                Interaction interaction = interactions.get(j);
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
            for (int j = 0; j < scenarios.size(); j++) {
                Scenario scenario = scenarios.get(j);
                if (scenario.getFrom().getState() == 0 && scenario.getTo().getState() == 0) {
                    behGraph[behMap.get(scenario.getFrom().getNumber())][behMap.get(scenario.getTo().getNumber())] = 1;
                }
            }
            for (int m = 0; m < expSize; m++) {
                for (int n = 0; n < expSize; n++) {
                    expGraph[m][n] = 0;
                }
            }
            for (int j = 0; j < scenarios.size(); j++) {
                Scenario scenario = scenarios.get(j);
                if (scenario.getFrom().getState() == 1 && scenario.getTo().getState() == 1) {
                    expGraph[expMap.get(scenario.getFrom().getNumber())][expMap.get(scenario.getTo().getNumber())] = 1;
                }
            }

            //step2
            for(int j = 0;j < scenarios.size();j++){
                Scenario scenario =scenarios.get(j);
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
                    if(behTc.getHasCycle() && expTc.getHasCycle()){
                        Outer:
                        for(int m = 0;m < behTc.getCircles().size();m++){
                            ArrayList<Integer> behCircle = behTc.getCircles().get(m);
                            int behLast = behMap.inverse().get(behCircle.get(behCircle.size() - 1));
                            if(behMap.inverse().get(behCircle.get(0)) == behMap.inverse().get(behStart)){
                                for(int n = 0;n < expTc.getCircles().size();n++){
                                    boolean flag = false;
                                    ArrayList<Integer> expCircle = expTc.getCircles().get(n);
                                    if(expMap.inverse().get(expCircle.get(0)) == expMap.inverse().get(expStart)){
                                        int expLast = expMap.inverse().get(expCircle.get(expCircle.size() - 1));
                                        for(int k = 0;k < scenarios.size();k++){
                                            Scenario tmpScenario = scenarios.get(k);
                                            if(tmpScenario.getState() == 0 || tmpScenario.getState() == 2 ||tmpScenario.getState() == 4){
                                                if((tmpScenario.getFrom().getNumber() == behLast && tmpScenario.getTo().getNumber() == expLast)
                                                        || (tmpScenario.getTo().getNumber() == behLast && tmpScenario.getFrom().getNumber() == expLast)){
//                                                    System.out.println(this.title);
//                                                    System.out.println("behStart:" + behMap.inverse().get(behStart));
//                                                    System.out.println("behLast:" + behLast);
//                                                    System.out.println("expStart:" + expMap.inverse().get(expStart));
//                                                    System.out.println("expLast:" + expLast);
                                                    behStart = behMap.inverse().get(behStart);
                                                    expStart = expMap.inverse().get(expStart);
                                                    Scenario behDelete = new Scenario(new LinkedList<>(),new Interaction(behLast,0),new Interaction(behStart, 0),1);
                                                    Scenario expDelete = new Scenario(new LinkedList<>(),new Interaction(expLast,1),new Interaction(expStart, 1),3);
                                                    System.out.println(scenarios.contains(behDelete));
                                                    System.out.println(scenarios.contains(expDelete));
                                                    scenarios.remove(behDelete);
                                                    scenarios.remove(expDelete);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
