package com.pindrop.drops.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CreateDropRequest {

    @JsonAlias({"userId"})
    private String userId;

    // Accept both "content" and legacy "description"
    @JsonAlias({"content", "description"})
    private String content;

    private Integer rating;

    private String imageUrl;

    private String savedPlaceId;

    @JsonAlias({"locationName", "title"})
    private String locationName;

    // Accept both lat/lon and latitude/longitude
    @JsonAlias({"lat", "latitude"})
    private Double lat;

    @JsonAlias({"lon", "lng", "longitude"})
    private Double lon;

    private Object createdAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSavedPlaceId() { return savedPlaceId; }
    public void setSavedPlaceId(String savedPlaceId) { this.savedPlaceId = savedPlaceId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
}
