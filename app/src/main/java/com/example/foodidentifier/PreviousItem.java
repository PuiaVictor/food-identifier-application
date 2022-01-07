package com.example.foodidentifier;

public class PreviousItem {
    private String name;
    private String confidence;


    public PreviousItem(String name,String confidence) {
        this.name = name;
        this.confidence = confidence;
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
