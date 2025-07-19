package com.kundevahal.lostnfound;

import java.io.Serializable;

public class HandoverRecord implements Serializable {
    private String itemId; // or whatever type you are using
    private String ownerName;
    private String ownerPhone;
    private String photoUrl;
    private String timestamp; // New parameter for timestamp

    // Constructor with five parameters
    public HandoverRecord(String itemId, String ownerName, String ownerPhone, String photoUrl, String timestamp) {
        this.itemId = itemId;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp; // Initialize the timestamp
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
