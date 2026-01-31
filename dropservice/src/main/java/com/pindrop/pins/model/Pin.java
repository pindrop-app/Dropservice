package com.pindrop.pins.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Pin {

    private String pinId;

    // Who created the drop
    private String userId;

    // Main text content (maps to UI "vibe")
    private String content;

    // Rating 0..100
    private Integer rating;

    // Optional image URL (for now, keep as URL string; do NOT store base64)
    private String imageUrl;

    // Location info
    private String savedPlaceId;
    private String locationName;
    private Double lat;
    private Double lon;

    // Store as ISO-8601 string for simplicity
    private String createdAt;

    // PinIt metadata (not required; bookmarks live in UserBookmarks)
    private String pinnedByUserId;
    private String pinnedAt;

    @DynamoDbPartitionKey
    public String getPinId() {
        return pinId;
    }

    public void setPinId(String pinId) {
        this.pinId = pinId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSavedPlaceId() {
        return savedPlaceId;
    }

    public void setSavedPlaceId(String savedPlaceId) {
        this.savedPlaceId = savedPlaceId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPinnedByUserId() {
        return pinnedByUserId;
    }

    public void setPinnedByUserId(String pinnedByUserId) {
        this.pinnedByUserId = pinnedByUserId;
    }

    public String getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(String pinnedAt) {
        this.pinnedAt = pinnedAt;
    }
}
