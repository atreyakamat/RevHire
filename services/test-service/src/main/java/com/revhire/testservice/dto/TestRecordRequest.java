package com.revhire.testservice.dto;

public class TestRecordRequest {

    private String name;
    private String message;

    public TestRecordRequest() {
        // Default constructor required for JSON deserialization
    }

    public TestRecordRequest(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
