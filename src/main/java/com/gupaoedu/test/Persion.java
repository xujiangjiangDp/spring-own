package com.gupaoedu.test;

public class Persion {
    public String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void work(){
        System.out.println("work");
    }

    public void work(int i){
        System.out.println("work"+i);
    }
}
