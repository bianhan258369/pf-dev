package com.ECNU.bean;

import java.util.Hashtable;
import java.util.LinkedList;

public class ClockDiagram extends ProblemDiagram {
    private String title;
    private Hashtable<Rect, String> members = new Hashtable<Rect, String>();
    private LinkedList<Clock> clocks=new LinkedList<>();

    public ClockDiagram(ProblemDiagram t) {
        super("ClockDiagram");

        this.components = new LinkedList();

        for (int i = 0; i <= t.components.size() - 1; i++) {
            Shape tmp_s = (Shape) t.components.get(i);
            this.components.add(tmp_s);
            if (t.components.get(i) instanceof Rect && ((Rect) t.components.get(i)).getState() != 2) {
                this.clocks.add(new Clock(((Rect) t.components.get(i)).getText()));
            }
        }
    }

    public void addClock(Clock clock){
        if(clock.getName().equals("Default"))
            return;

        int size=this.clocks.size();
        int temp=size;

        for(int i=0;i<size;i++){
            if(this.clocks.get(i).getName().equals(clock.getName())){
                temp=i;
            }
        }

        if(temp!=size){
            this.clocks.remove(temp);
        }

        this.clocks.add(clock);
    }

    public Clock getClock(Rect rect) {
        String clockName=this.members.get(rect);

        if(clockName!=null&&clockName.equals("Default")){
            return new Clock(rect.getText());
        }

        for(int i=0;i<this.clocks.size();i++){
            Clock tempClock=clocks.get(i);
            if(tempClock.getName().equals(clockName)){
                return tempClock;
            }
        }
        return null;
    }
}
