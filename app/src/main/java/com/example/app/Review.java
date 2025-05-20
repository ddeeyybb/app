package com.example.app.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Model class representing a Review made by a user.
 */
public class Review {

    private Integer rating;      // Rating from 1 to 5
    private String comment;
    private String username;
    private Long timestamp;     // Milliseconds since epoch
    private String uid;

    // Formatter for date display
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Review() {}

    /**
     * Constructor with rating and comment.
     * @param rating rating from 1 to 5
     * @param comment review comment
     */
    public Review(Integer rating, String comment) {
        setRating(rating);
        this.comment = comment;
    }

    /**
     * Constructor without uid.
     * @param rating rating from 1 to 5
     * @param comment review comment
     * @param username reviewer's username
     * @param timestamp review time in milliseconds
     */
    public Review(Integer rating, String comment, String username, Long timestamp) {
        setRating(rating);
        this.comment = comment;
        this.username = username;
        this.timestamp = timestamp;
    }

    /**
     * Full constructor including uid.
     * @param rating rating from 1 to 5
     * @param comment review comment
     * @param username reviewer's username
     * @param timestamp review time in milliseconds
     * @param uid user's unique ID
     */
    public Review(Integer rating, String comment, String username, Long timestamp, String uid) {
        setRating(rating);
        this.comment = comment;
        this.username = username;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    /**
     * Gets the rating.
     * @return rating (1-5)
     */
    public Integer getRating() {
        return rating;
    }

    /**
     * Sets the rating. Enforces rating to be between 1 and 5.
     * @param rating rating value
     */
    public void setRating(Integer rating) {
        if (rating == null || rating < 1) {
            this.rating = 1;
        } else if (rating > 5) {
            this.rating = 5;
        } else {
            this.rating = rating;
        }
    }

    /**
     * Gets the review comment.
     * @return comment text
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the review comment.
     * @param comment comment text
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the username of the reviewer.
     * @return username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * @param username user name string
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the timestamp in milliseconds since epoch.
     * @return timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp in milliseconds.
     * @param timestamp time since epoch
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the user's unique ID.
     * @return uid string
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the user's unique ID.
     * @param uid user id string
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Returns a formatted date string from the timestamp.
     * If timestamp is null or invalid, returns an empty string.
     * @return formatted date e.g. "May 20, 2025"
     */
    public String getFormattedDate() {
        if (timestamp == null || timestamp <= 0) {
            return "";
        }
        return dateFormat.format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating=" + rating +
                ", comment='" + comment + '\'' +
                ", username='" + username + '\'' +
                ", timestamp=" + timestamp +
                ", uid='" + uid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review review = (Review) o;
        return Objects.equals(rating, review.rating) &&
                Objects.equals(comment, review.comment) &&
                Objects.equals(username, review.username) &&
                Objects.equals(timestamp, review.timestamp) &&
                Objects.equals(uid, review.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating, comment, username, timestamp, uid);
    }
}
