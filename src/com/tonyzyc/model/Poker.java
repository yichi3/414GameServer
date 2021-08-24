package com.tonyzyc.model;

public class Poker {
    private int id;
    private String name;
    private String color;
    private int num;
    private boolean isOut;
    private boolean isHun;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isHun() {
        return isHun;
    }

    public void setHun(boolean hun) {
        isHun = hun;
    }

    public Poker() {
    }

    public Poker(int id, String name, String color, int num, boolean isHun) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.num = num;
        this.isHun = isHun;
    }

    public Poker(int id, String name, String color, int num, boolean isOut, boolean isHun) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.num = num;
        this.isOut = isOut;
        this.isHun = isHun;
    }
}
