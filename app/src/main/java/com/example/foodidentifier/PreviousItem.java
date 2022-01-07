package com.example.foodidentifier;

public class PreviousItem {
    private String previousItemName;
    private String previousItemConfidence;

    public PreviousItem(String previousItemName, String previousItemConfidence) {
        this.previousItemName = previousItemName;
        this.previousItemConfidence = previousItemConfidence;
    }

    public PreviousItem() {

    }

    public String getPreviousItemName() {
        return previousItemName;
    }

    public void setPreviousItemName(String previousItemName) {
        this.previousItemName = previousItemName;
    }

    public String getPreviousItemConfidence() {
        return previousItemConfidence;
    }

    public void setPreviousItemConfidence(String previousItemConfidence) {
        this.previousItemConfidence = previousItemConfidence;
    }
}
