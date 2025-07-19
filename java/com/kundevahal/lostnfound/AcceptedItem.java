package com.kundevahal.lostnfound;

import java.io.Serializable;

public class AcceptedItem implements Serializable {
    private String adharImageUrl;
    private String adharNumber;
    private String dateTime;
    private String description;
    private String itemId;
    private String itemImageUrl;
    private String location;
    private String name;
    private String userEmail;

    // Default constructor required for calls to DataSnapshot.getValue(AcceptedItem.class)
    public AcceptedItem() {}

    // Add getters and setters for each field

    public String getAdharImageUrl() {
        return adharImageUrl;
    }

    public void setAdharImageUrl(String adharImageUrl) {
        this.adharImageUrl = adharImageUrl;
    }

    public String getAdharNumber() {
        return adharNumber;
    }

    public void setAdharNumber(String adharNumber) {
        this.adharNumber = adharNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
