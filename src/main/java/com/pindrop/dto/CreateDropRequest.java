package com.pindrop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class CreateDropRequest {

    @NotBlank
    private String content;

    @Min(0)
    @Max(100)
    private Integer rating;

    private String imageUrl;
    private String savedPlaceId;
    private Double lat;
    private Double lon;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSavedPlaceId() { return savedPlaceId; }
    public void setSavedPlaceId(String savedPlaceId) { this.savedPlaceId = savedPlaceId; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }
}
