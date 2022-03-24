package com.example.recipe.model;

import java.util.Map;
import java.util.HashMap;


public class User implements Comparable<User>  {

    String email = "";
    String fullName = "";
    String userName = "";
    String proImageUrl = "";

    public static final String COLLECTION_NAME = "Users";
    public static final String COLLECTION_EMAIL_NAME = "Emails";


    public User() {
    }

    public User(String fullName, String userName, String email) {
        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("fullName", fullName);
        json.put("userName", userName);
        json.put("email", email);
        json.put("imageUrl", proImageUrl);
        return json;
    }

    public static User create(Map<String, Object> json) {
        String fullName = (String) json.get("fullName");
        String userName = (String) json.get("userName");
        String email = (String) json.get("email");
        String imageUrl = (String) json.get("imageUrl");
        User user = new User(fullName, userName, email);
        user.setProImageUrl(imageUrl);
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserName() {
        return userName;
    }

    public String getProImageUrl() {
        return proImageUrl;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProImageUrl(String proImageUrl) {
        this.proImageUrl = proImageUrl;
    }


    @Override
    public int compareTo(User o) {
        return this.getUserName().compareTo(o.getUserName());
    }
}
