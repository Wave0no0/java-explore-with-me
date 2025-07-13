package ru.practicum.stats.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam("start")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
            @RequestParam("end")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to,
            @RequestParam(value = "uris", required = false) List<String> endpoints,
            @RequestParam(value = "unique", defaultValue = "false") boolean onlyUnique
    ) {
        log.info("Stats requested: from={}, to={}, endpoints={}, onlyUnique={}", from, to, endpoints, onlyUnique);
        return statsService.getStats(from, to, endpoints, onlyUnique);
    }
}