package com.kundevahal.lostnfound;

public class HandoverItem {
    private String id;
    private String itemName;
    private String userName;
    private String userPhone;
    private String photoUrl;
    private String location;
    private String email;
    private String phoneNumber;
    private String description;

    // No-argument constructor
    public HandoverItem() {
        // Required empty constructor
    }

    // Parameterized constructor
    public HandoverItem(String id, String itemName, String userName, String userPhone, String photoUrl, String location, String email, String phoneNumber, String description) {
        this.id = id;
        this.itemName = itemName;
        this.userName = userName;
        this.userPhone = userPhone;
        this.photoUrl = photoUrl;
        this.location = location;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
