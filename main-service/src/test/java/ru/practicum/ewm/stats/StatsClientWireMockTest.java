package ru.practicum.ewm.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsClientWireMockTest {
    @Mock
    private RestTemplate restTemplate;
    private StatsClient statsClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        statsClient = new StatsClient(restTemplate, objectMapper);
    }

    @Test
    void getStats_successfulResponse() throws Exception {
        String responseJson = "[{\"app\":\"ewm-service\",\"uri\":\"/events/1\",\"hits\":5}]";
        Object responseBody = objectMapper.readValue(responseJson, Object.class);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class), any(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        List<ViewStatsDto> stats = statsClient.getStats(LocalDateTime.now(), LocalDateTime.now(), List.of("/events/1"), true);
        assertNotNull(stats);
        assertEquals(1, stats.size());
        assertEquals(5, stats.get(0).getHits());
    }

    @Test
    void getStats_emptyResponse() throws Exception {
        Object responseBody = objectMapper.readValue("[]", Object.class);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class), any(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        List<ViewStatsDto> stats = statsClient.getStats(LocalDateTime.now(), LocalDateTime.now(), List.of("/events/1"), true);
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
}
