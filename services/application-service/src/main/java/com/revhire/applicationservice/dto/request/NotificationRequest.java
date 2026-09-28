package com.revhire.applicationservice.dto.request;

public class NotificationRequest {

    private Long recipientId;
    private String type;
    private String channel;
    private String title;
    private String message;

    public NotificationRequest() {
    }

    public NotificationRequest(Long recipientId, String type, String channel, String title, String message) {
        this.recipientId = recipientId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
