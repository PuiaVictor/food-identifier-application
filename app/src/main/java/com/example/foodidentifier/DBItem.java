package com.example.foodidentifier;

import android.media.Image;

public class DBItem {

    private String name;
    private String confidence;
    private Image image;

    public DBItem(String name, String confidence) {
        this.name = name;
        this.confidence = confidence;
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
}
