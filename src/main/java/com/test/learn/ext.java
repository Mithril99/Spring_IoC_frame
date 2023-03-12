package com.test.learn;
class Father {
    static int a = 10;
    private int x = 10;

    final void add(){
        System.out.println("add");
    }
    public Father() {
        System.out.println("Father " + this.x);
        this.print();
        this.x = 20;
        add();
    }
    public void print() {
        System.out.println("Father.x = " + this.x);
    }
}
class Son extends Father {
    static int a = 20;
    int x = 30;

    public Son() {
        System.out.println("Son " + x);
        this.print();
        x = 40;
    }
    public void print() {
        System.out.println("Son.x = " + x);
    }
}

public class ext {
    public static void main(String[] args) {
        Father f = new Son();
        Son s = new Son();
//        System.out.println(f.x);
        f.print();
        System.out.println(Son.a);
    }
}


