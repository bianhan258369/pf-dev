package com.ECNU.util;

import com.ECNU.bean.Interaction;
import com.ECNU.bean.Scenario;
import com.ECNU.bean.ScenarioDiagram;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;

import java.util.*;

public class TestCircle {
    private int n;
    private int[] visited;//节点状态,值为0的是未访问的
    private int[][] e;//有向图的邻接矩阵
    private ArrayList<Integer> trace=new ArrayList<Integer>();//从出发节点到当前节点的轨迹
    private ArrayList<ArrayList<Integer>> circles = new ArrayList<>();
    private boolean hasCycle=false;

    public TestCircle(int n,int[][] e){
        this.n=n;
        visited=new int[n];
        Arrays.fill(visited,0);
        this.e=e;
    }

    public void findCycle(int v)   //递归DFS
    {
        if(visited[v]==1)
        {
            int j;
            if((j=trace.indexOf(v))!=-1)
            {
                hasCycle=true;
                System.out.print("Cycle:");
                ArrayList<Integer> circle = new ArrayList<>();
                while(j<trace.size())
                {
                    circle.add(trace.get(j));
                    System.out.print(trace.get(j)+" ");
                    j++;
                }
                circles.add(circle);
                System.out.print("\n");
                return;
            }
            return;
        }
        visited[v]=1;
        trace.add(v);

        for(int i=0;i<n;i++)
        {
            if(e[v][i]==1)
                findCycle(i);
        }
        trace.remove(trace.size()-1);
    }

    public boolean getHasCycle(){
        return hasCycle;
    }

    public ArrayList<ArrayList<Integer>> getCircles() {
        return circles;
    }

    public static void main(String[] args) {
//        int n=7;
//        int[][] e={
//                {0,1,1,0,0,0,0},
//                {0,0,0,1,0,0,0},
//                {0,0,0,0,0,1,0},
//                {0,0,0,0,1,0,0},
//                {0,0,1,0,0,0,0},
//                {0,0,0,0,1,0,1},
//                {1,0,1,0,0,0,0}};//有向图的邻接矩阵,值大家任意改.
//        TestCircle tc=new TestCircle(n,e);
//        for(int i =0 ;i < e.length;i++){
//            tc.findCycle(i);
//        }
//        //tc.findCycle(1);
//        ArrayList<ArrayList<Integer>> circles = tc.getCircles();
//
//        for(int i = 0;i < circles.size();i++){
//            ArrayList<Integer> circle = circles.get(i);
//            for(int j = 0;j < circle.size();j++){
//                System.out.print(circle.get(j) + " ");
//            }
//            System.out.println("\n");
//        }
        ScenarioDiagram scenarioDiagram = new ScenarioDiagram("sd1",1);
        List<Interaction> interactions = new LinkedList<>();
        List<Scenario> scenarios = new LinkedList<>();
        interactions.add(new Interaction(100,100,13,0));
        interactions.add(new Interaction(300,200,15,1));
        interactions.add(new Interaction(300,300,14,1));
        interactions.add(new Interaction(300,400,16,1));
        interactions.add(new Interaction(100,200,1,0));
        interactions.add(new Interaction(100,300,14,0));
        interactions.add(new Interaction(100,400,2,0));
        interactions.add(new Interaction(300,100,13,1));

        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,200,1,0),
                new Interaction(100,300,14,0),1));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,300,14,0),
                new Interaction(100,400,2,0),1));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,400,2,0),
                new Interaction(100,100,13,0),1));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,100,13,0),
                new Interaction(100,200,1,0),1));


        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(300,200,15,1),
                new Interaction(300,300,14,1),3));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(300,300,14,1),
                new Interaction(300,400,16,1),3));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(300,100,13,1),
                new Interaction(300,200,15,1),3));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(300,400,16,1),
                new Interaction(300,100,13,1),3));

        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,300,14,0),
                new Interaction(300,300,14,1),2));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,400,2,0),
                new Interaction(300,400,16,1),0));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,100,13,0),
                new Interaction(300,100,13,1),2));
        scenarios.add(new Scenario(new LinkedList<String>(),new Interaction(100,200,1,0),
                new Interaction(300,200,15,1),0));

        for(int i = 0;i < interactions.size();i++) scenarioDiagram.addJiaohu(interactions.get(i));
        for(int i = 0;i < scenarios.size();i++) scenarioDiagram.addChangjing(scenarios.get(i));

        String circle = "{\"circle\":\"";
        //step1
        BiMap<Integer, Integer> behMap = HashBiMap.create();
        BiMap<Integer, Integer> expMap = HashBiMap.create();
        for(int j = 0;j < scenarioDiagram.getInteractions().size();j++){
            Interaction interaction = scenarioDiagram.getInteractions().get(j);
            if(interaction.getState() == 0) behMap.put(interaction.getNumber(), behMap.size());
            if(interaction.getState() == 1) expMap.put(interaction.getNumber(), expMap.size());
        }
        int behSize = behMap.size();
        int expSize = expMap.size();
        int[][] behGraph = new int[behSize][behSize];
        int[][] expGraph = new int[expSize][expSize];


        for(int m = 0;m < behSize;m++){
            for(int n = 0;n < behSize;n++){
                behGraph[m][n] = 0;
            }
        }
        for(int j = 0;j < scenarioDiagram.getScenarios().size();j++){
            Scenario scenario = scenarioDiagram.getScenarios().get(j);
            if(scenario.getFrom().getState() == 0 && scenario.getTo().getState() == 0){
                behGraph[behMap.get(scenario.getFrom().getNumber())][behMap.get(scenario.getTo().getNumber())] = 1;
            }
        }
        for(int m = 0;m < expSize;m++){
            for(int n = 0;n < expSize;n++){
                expGraph[m][n] = 0;
            }
        }
        for(int j = 0;j < scenarioDiagram.getScenarios().size();j++){
            Scenario scenario = scenarioDiagram.getScenarios().get(j);
            if(scenario.getFrom().getState() == 1 && scenario.getTo().getState() == 1){
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
                                        return;
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
                                    circle = circle + 0;
                                    circle = circle + "\"}";
                                    System.out.println(circle);
                                    return;
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

        circle = circle + "\"}";
        System.out.println(circle);
    }
}
