package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {
    private Long id;

    private String app;

    private String uri;

    private String ip;

    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime timestamp;
}
