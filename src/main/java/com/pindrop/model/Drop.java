package com.pindrop.model;

public class Drop {
    private String id;
    private String content;
    private Integer rating;
    private String imageUrl;
    private String savedPlaceId;
    private double lat;
    private double lon;
    private long createdAt;

    public Drop() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSavedPlaceId() { return savedPlaceId; }
    public void setSavedPlaceId(String savedPlaceId) { this.savedPlaceId = savedPlaceId; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
