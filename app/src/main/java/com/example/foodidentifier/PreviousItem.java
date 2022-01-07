package com.example.foodidentifier;

public class PreviousItem {
    private String confidence;
    private String name;

    public PreviousItem(String confidence, String name) {
        this.confidence = confidence;
        this.name = name;
    }

    public PreviousItem() {

    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
