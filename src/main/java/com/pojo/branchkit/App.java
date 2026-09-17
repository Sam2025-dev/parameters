package com.pojo.branchkit;

public final class App {

    public String greeting() {
        return "Hello, Maven!";
    }

    public static void main(String[] args) {
        System.out.println(new App().greeting());
    }
}
