package com.myni.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ask {
    @SerializedName("Sender")
    @Expose
    private String Sender;
    @SerializedName("message")
    @Expose
    private String message;

    public ask() {
    }

    public ask(String sender, String message) {
        Sender = sender;
        this.message = message;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        this.Sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}