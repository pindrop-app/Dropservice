package com.pindrop.places.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PlacesService {
    private static final Logger log = LoggerFactory.getLogger(PlacesService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public PlacesService(ObjectMapper objectMapper,
                         @Value("${google.places.api-key:}") String apiKey) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public Map<String, String> reverseGeocode(double lat, double lon) {
        if (apiKey.isBlank()) {
            log.warn("Google API key is missing; reverse geocode will return fallback values.");
            return Map.of("name", "Current location", "formattedAddress", "");
        }

        try {
            String latlng = lat + "," + lon;
            String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                    + URLEncoder.encode(latlng, StandardCharsets.UTF_8)
                    + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String body = restClient.get().uri(url).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(body);

            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                String formatted = results.get(0).path("formatted_address").asText("");
                // best-effort "name": first component, else whole formatted
                String name = formatted;
                int comma = formatted.indexOf(',');
                if (comma > 0) name = formatted.substring(0, comma);
                return Map.of("name", name, "formattedAddress", formatted);
            }
        } catch (Exception e) {
            log.warn("Reverse geocode failed for lat={}, lon={}: {}", lat, lon, e.getMessage());
        }
        return Map.of("name", "Current location", "formattedAddress", "");
    }
}
