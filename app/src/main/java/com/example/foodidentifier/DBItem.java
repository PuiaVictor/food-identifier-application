package com.example.foodidentifier;

import android.media.Image;

public class DBItem {

    private String name;
    private String confidence;
    private String imageUrl;

    public DBItem(String name, String confidence, String imageUrl) {
        this.name = name;
        this.confidence = confidence;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
