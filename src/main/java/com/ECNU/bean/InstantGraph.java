package com.ECNU.bean;

import lombok.Data;

import java.util.LinkedList;

@Data
public class InstantGraph {
    private String name = "";

    private LinkedList<Interaction> nowJiaohu = new LinkedList<>();
    private LinkedList<Scenario> nowChangjing = new LinkedList<>();

    private LinkedList<Scenario> changjing = new LinkedList<>();
    private ScenarioDiagram intDiagram = null;
    private LinkedList<Phenomenon> phenomenons = new LinkedList<>();
    private LinkedList<String> constraint = new LinkedList<>();
    private LinkedList<Interaction> jiaohu = new LinkedList<>();
    private Rect domain;// Ҫ����ʱ��ͼ��������
    private Clock clock;
    private ProblemDiagram problemDiagram;// ����ͼ

    public InstantGraph(Rect domain, Clock clock, int index, ProblemDiagram problemDiagram, ScenarioDiagram scenarioDiagram) throws CloneNotSupportedException {
        this.domain = domain;
        this.clock = clock;
        this.problemDiagram = problemDiagram;
        this.intDiagram = scenarioDiagram;
        if (problemDiagram == null) return;

        LinkedList tempJiaohu = scenarioDiagram.getInteractions();
        LinkedList tempPhenomenon = problemDiagram.getPhenomenon();
        this.setName(domain.getShortName());

        for (int i = 0; i < tempJiaohu.size(); i++) {
            Interaction temp_j = (Interaction) tempJiaohu.get(i);
            int number = temp_j.getNumber();
            for (int j = 0; j < tempPhenomenon.size(); j++) {
                Phenomenon phenomenon = (Phenomenon) tempPhenomenon.get(j);
                if (phenomenon.getBiaohao() == number) {
                    if (phenomenon.getFrom().getShortName().equals(domain.getShortName()) || phenomenon.getTo().getShortName().equals(domain.getShortName())) {
                        if (!phenomenons.contains(phenomenon)) phenomenons.add(phenomenon);
                        jiaohu.add(temp_j);
                        break;
                    }
                }
            }
        }
    }
}
