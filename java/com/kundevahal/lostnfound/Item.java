package com.kundevahal.lostnfound;

import java.io.Serializable;

public class Item implements Serializable {
    private String itemId;
    private String name;
    private String location;
    private String dateTime;
    private String itemImageUrl;
    private String adharImageUrl;
    private String description;
    private String adharNumber;
    private String userName;
    private String phoneNumber;

    private String userEmail;  // New field for user email
    private String finalImageType;

    public Item() {
        // Default constructor required for calls to DataSnapshot.getValue(Item.class)
    }

    public Item(String itemId, String name, String location, String dateTime,
                String itemImageUrl, String adharImageUrl, String itemDescription,
                String adharNumber, String userName, String phoneNumber, String userEmail, String finalImageType) { // Updated constructor
        this.itemId = itemId;
        this.name = name;
        this.location = location;
        this.dateTime = dateTime;
        this.itemImageUrl = itemImageUrl;
        this.adharImageUrl = adharImageUrl;
        this.description = itemDescription;
        this.adharNumber = adharNumber;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userEmail = userEmail;
        this.finalImageType = finalImageType;
    }

    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Other getters and setters...

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public String getAdharImageUrl() {
        return adharImageUrl;
    }

    public void setAdharImageUrl(String adharImageUrl) {
        this.adharImageUrl = adharImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdharNumber() {
        return adharNumber;
    }

    public void setAdharNumber(String adharNumber) {
        this.adharNumber = adharNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFinalImageType() {
        return finalImageType;
    }

    public void setFinalImageType(String finalImageType) {
        this.finalImageType = finalImageType;
    }
}
