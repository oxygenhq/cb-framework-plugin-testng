package io.cloudbeat.testng;

public class FailureModel {
    public FailureModel(String message) {
        type = "TESTNG_ERROR";
        this.message = message;
    }

    public String type;
    public String data;
    public String message;
    public String location;
}
