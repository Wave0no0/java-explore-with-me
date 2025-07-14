package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {
    private static final Logger log = LoggerFactory.getLogger(StatsClient.class);
    private final RestTemplate restTemplate;

    @Value("${stats.server.url}")
    private String serverUrl;

    public StatsClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void saveHit(EndpointHitDto hitDto) {
        try {
            HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto);
            restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);
        } catch (RestClientException e) {
            log.error("Ошибка при отправке hit в stats-сервис: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = encode(start);
        String endStr = encode(end);

        StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats?start=" + startStr + "&end=" + endStr);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                String encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8);
                urlBuilder.append("&uris=").append(encodedUri);
            }
        }

        urlBuilder.append("&unique=").append(unique);

        try {
            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(urlBuilder.toString(), ViewStatsDto[].class);
            if (response.hasBody() && response.getBody() != null) {
                return List.of(response.getBody());
            } else {
                log.warn("Ответ от stats-сервиса пустой");
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            log.error("Ошибка при получении статистики из stats-сервиса: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String encode(LocalDateTime dateTime) {
        String raw = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}