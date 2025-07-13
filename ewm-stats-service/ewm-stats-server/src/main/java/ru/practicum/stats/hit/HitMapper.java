package ru.practicum.stats.hit;

import ru.practicum.dto.EndpointHitDto;
import java.time.format.DateTimeFormatter;

public class HitMapper {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit toEntity(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}