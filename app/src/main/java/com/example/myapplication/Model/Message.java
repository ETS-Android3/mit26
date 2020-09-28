package com.example.myapplication.Model;

public class Message {
    public String sender;
    public String receiver;
    public String message;
    public String Time;
    public String avatar;

    public Message(String sender, String receiver, String message, String time, String avatar) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        Time = time;
        this.avatar = avatar;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
