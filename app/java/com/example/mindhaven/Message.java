package com.example.mindhaven;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
    private String user;
    private String text;
    private long timestamp;


    public Message() {

    }

    public Message(String user, String text, long timestamp) {
        this.user = user;
        this.text = text;
        this.timestamp = timestamp;
    }


    public String getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "user='" + user + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
