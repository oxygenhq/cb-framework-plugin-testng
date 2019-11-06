package io.cloudbeat.testng;

public class FailureModel {

    public FailureModel(){}

    public FailureModel(String message) {
        type = "TESTNG_ERROR";
        this.message = message;
        isFatal = true;
    }

    public String message;
    public String type;
    public String data;
    public int line;
    public String details;
    public boolean isFatal;
}
