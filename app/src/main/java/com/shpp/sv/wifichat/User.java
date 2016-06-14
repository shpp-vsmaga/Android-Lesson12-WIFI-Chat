package com.shpp.sv.wifichat;

/**
 * Created by SV on 08.06.2016.
 */
public class User {

    private String name;
    private int color;

    public User(String name, int color){
        this.name = name;
        this.color = color;
    }

    public String getName(){
        return name;
    }


    public int getColor(){
        return color;
    }
}
