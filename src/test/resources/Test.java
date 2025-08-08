package com.jstarts.test;

public class Test {
    private int count;
    public static final String NAME = "Java";

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public static void log() {
        System.out.println("Log called");
    }
}
