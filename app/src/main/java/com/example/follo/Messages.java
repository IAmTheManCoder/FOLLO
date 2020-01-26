package com.example.follo;

public class Messages {

    public String date, time, from, type, message;

    public Messages(){

    }

    public Messages(String date, String time, String from, String type, String message) {
        this.date = date;
        this.time = time;
        this.from = from;
        this.type = type;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
