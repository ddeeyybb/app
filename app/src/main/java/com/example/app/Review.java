package com.example.app.models;

public class Review {
    private int rating;
    private String comment;
    private String username;
    private long timestamp;
    private String uid;

    public Review() {}

    public Review(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public Review(int rating, String comment, String username, long timestamp) {
        this.rating = rating;
        this.comment = comment;
        this.username = username;
        this.timestamp = timestamp;
    }

    public Review(int rating, String comment, String username, long timestamp, String uid) {
        this.rating = rating;
        this.comment = comment;
        this.username = username;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    // Getters
    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    // Setters
    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
