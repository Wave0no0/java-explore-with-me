package ru.practicum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsClient {
    private final RestTemplate httpClient;
    private final ObjectMapper jsonMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DateTimeUtil.DATE_PATTERN);

    public StatisticsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.httpClient = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
        this.jsonMapper = new ObjectMapper();
    }

    public void recordEndpointHit(EndpointHitSaveDto hitData) {
        sendHttpRequest(HttpMethod.POST, "/hit", null, hitData);
    }

    public List<ViewStatsDto> retrieveViewStatistics(LocalDateTime startTime, LocalDateTime endTime, 
                                                     List<String> endpointUris, Boolean uniqueOnly) {
        Map<String, Object> queryParams = Map.of(
                "start", startTime.format(DATE_FORMATTER),
                "end", endTime.format(DATE_FORMATTER),
                "uris", String.join(",", endpointUris),
                "unique", uniqueOnly);
        
        ResponseEntity<Object> response = sendHttpRequest(
                HttpMethod.GET,
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                queryParams,
                null
        );
        
        try {
            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response body from statistics service");
            }
            return jsonMapper.readValue(
                    jsonMapper.writeValueAsString(response.getBody()), new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process statistics response");
        }
    }

    private <T> ResponseEntity<Object> sendHttpRequest(HttpMethod method, String endpoint,
                                                       @Nullable Map<String, Object> parameters, @Nullable T requestBody) {
        HttpEntity<T> httpEntity = new HttpEntity<>(requestBody, createDefaultHeaders());

        ResponseEntity<Object> statisticsResponse;
        try {
            if (parameters != null) {
                statisticsResponse = httpClient.exchange(endpoint, method, httpEntity, Object.class, parameters);
            } else {
                statisticsResponse = httpClient.exchange(endpoint, method, httpEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return processStatisticsResponse(statisticsResponse);
    }

    private HttpHeaders createDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> processStatisticsResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
