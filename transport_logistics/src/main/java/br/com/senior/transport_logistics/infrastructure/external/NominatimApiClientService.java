package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;


@Service
public class NominatimApiClientService {

    private static final String USER_AGENT = "transport-api/1.0 (test@example.com)";
    private static final String API_URL = "https://nominatim.openstreetmap.org/search";

    public CoordinatesDTO getCoordinates(String address) {
        try {
            String jsonResponse = queryApi(address);
            return extractCoordinates(jsonResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching coordinates: " + e.getMessage());
        }
    }

    private String queryApi(String address) throws Exception {
        String formattedUrl = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .toUriString();

        HttpURLConnection connection = (HttpURLConnection) new URL(formattedUrl).openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    private CoordinatesDTO extractCoordinates(String json) {
        JSONArray results = new JSONArray(json);

        if (results.isEmpty()) {
            throw new RuntimeException("Address not found.");
        }

        JSONObject firstResult = results.getJSONObject(0);
        double latitude = firstResult.getDouble("lat");
        double longitude = firstResult.getDouble("lon");

        return new CoordinatesDTO(latitude, longitude);
    }
}

