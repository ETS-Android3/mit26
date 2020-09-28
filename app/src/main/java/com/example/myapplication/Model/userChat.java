package com.example.myapplication.Model;

public class userChat {
    String idboxchat;
    String userid;
    String avatar;
    String userName;
    String time;
    String lastMessage;
    String type;

    public userChat(String idboxchat, String userid, String avatar, String userName, String time, String lastMessage, String type) {
        this.idboxchat = idboxchat;
        this.userid = userid;
        this.avatar = avatar;
        this.userName = userName;
        this.time = time;
        this.lastMessage = lastMessage;
        this.type = type;
    }

    public String getIdboxchat() {
        return idboxchat;
    }

    public void setIdboxchat(String idboxchat) {
        this.idboxchat = idboxchat;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
