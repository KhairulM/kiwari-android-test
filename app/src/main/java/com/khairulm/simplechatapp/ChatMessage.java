package com.khairulm.simplechatapp;

import java.util.Date;

public class ChatMessage {
    private String messageSender, messageText, senderId;
    private long messageTime;

    public ChatMessage(String messageSender, String messageText, String senderId) {
        this.messageSender = messageSender;
        this.messageText = messageText;
        this.senderId = senderId;
        this.messageTime = new Date().getTime();
    }

    public ChatMessage(String messageSender, String messageText, String senderId, long messageTime) {
        this.messageSender = messageSender;
        this.messageText = messageText;
        this.senderId = senderId;
        this.messageTime = messageTime;
    }

    public ChatMessage(){

    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
