package com.shpp.sv.wifichat;

/**
 * Created by SV on 08.06.2016.
 */
public class Message {

    private User author;
    private String text;
    private long time;
    private boolean own = false;

    public Message(User author, String text, long time, boolean own){
        this.author = author;
        this.text = text;
        this.time = time;
        this.own = own;
    }

    public User getAuthor(){
        return author;
    }

    public String getText(){
        return text;
    }

    public long getTime(){
        return time;
    }

    public int getColor(){
        return author.getColor();
    }

    public boolean isOwn(){
        return own;
    }


}
