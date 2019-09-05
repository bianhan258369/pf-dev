//package com.ECNU.util;
//
//import java.util.LinkedList;
//import java.util.List;
//
//public class Z3Util {
//    String[] sym=new String[]{"<","≤","⊆","#","+","*","∧","∨","$","∝","☇","|","=="};
//    int timeout;
//    String s;
//    int b;
//    String f;
//    List<String> clocks = new LinkedList<>();
//    int pb;
//    boolean dl;
//    boolean p;
//    String res="",tmp="",pec="",ncl="",ltl="",dlk="",filter="";
//    int lct = 0;
//
//    public Z3Util(int timeout, String s, int b, String f, int pb, boolean dl, boolean p) {
//        this.timeout = timeout;
//        this.s = s;
//        this.b = b;
//        this.f = f;
//        this.pb = pb;
//        this.dl = dl;
//        this.p = p;
//    }
//
//    private String pres(String str){
//        return str.replace(" ","");
//    }
//
//    private void addc(String c){
//        flag = true;
//        for(int i = 0;i < clocks.size();i++){
//            if(clocks.get(i).equals(c)){
//                flag = false;
//                break;
//            }
//        }
//        if(flag) clocks.add(c);
//    }
//
//    private void filterfunc(){
//
//    }
//
//    private void init(String str, boolean flag){
//        for(int i = 0;i < sym.length;i++){
//            if(str.indexOf(sym[i]) >= 0){
//                String[] c;
//                int ii;
//                if(i < 4){
//                    c = str.split(sym[i]);
//                    addc(c[0]);
//                    addc(c[1]);
//                    ii = i;
//                }
//                else if(i < 8){
//                    String[] opt = str.split(sym[i]);
//                    c = new String[3];
//                    c[0] = opt[0].split("=")[0];
//                    c[1] = opt[0].split("=")[1];
//                    c[2] = opt[1];
//                    for(int j = 0;j < 3;j++) addc(c[j]);
//                    ii = i;
//                }
//                else if(i == 8){
//                    String[] opt = str.split(sym[i]);
//                    c = new String[4];
//                    c[0] = opt[0].split("=")[0];
//                    c[1] = opt[0].split("=")[1];
//                    if(opt[1].indexOf(" on ")>=0){
//                        c[2]=opt[1].split(" on ")[0];
//                        c[3]=opt[1].split(" on ")[1];
//                        addc(c[0]);addc(c[1]);
//                        if(!c[3].equals("idealClock")) addc(c[3]);
//                        ii=i+1;
//                    }
//                    else{
//                        c[2]=opt[1];
//                        addc(c[0]);addc(c[1]);
//                        ii=i;
//                    }
//                }
//                else if(i == 9){
//                    String[] opt = str.split(sym[i]);
//                    c = new String[3];
//                    c[0]=opt[0].split("=")[0];
//                    c[1]=opt[0].split("=")[1];
//                    c[2]=opt[1];
//                    addc(c[0]);addc(c[1]);
//                    ii=i+1;
//                }
//                else if(i == 10){
//                    String[] opt = str.split(sym[i]);
//                    c = new String[3];
//                    c[0]=opt[0].split("=")[0];
//                    c[1]=opt[0].split("=")[1];
//                    c[2]=opt[1];
//                    addc(c[0]);addc(c[1]);addc(c[2]);
//                    ii=i+1;
//                }
//                else if(i == 1){
//                    String[] opt = str.split(sym[i]);
//                    c = new String[3];
//                    c[0]=opt[0].split("=")[0];
//                    c[1]=opt[0].split("=")[1];
//                    c[2]=c[0]+"F"+c[1];
//                    addc(c[0]);addc(c[1]);addc(c[2]);
//                    ii=5;
//                    filterfunc(c[2],opt[1]);
//                }
//                else {
//                    String[] opt = str.split(sym[i]);
//                    c = new String[2];
//                    c[0]=opt[0];
//                    c[1]=opt[1];
//                    addc(c[0]);addc(c[1]);
//                    ii=i;
//                }
//                if(flag) ncl+=addt(c,ii);
//                else tmp+="(assert"+addt(c,ii)+")\n";
//                if(dl) dlk+=addd(c,ii);
//                break;
//            }
//        }
//    }
//}
