package com.khairulm.simplechatapp;

public class User {
    private String userName, userAvatarURL, userId;

    public User(String userName, String userAvatarURL, String userId) {
        this.userName = userName;
        this.userAvatarURL = userAvatarURL;
        this.userId = userId;
    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarURL() {
        return userAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        this.userAvatarURL = userAvatarURL;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
