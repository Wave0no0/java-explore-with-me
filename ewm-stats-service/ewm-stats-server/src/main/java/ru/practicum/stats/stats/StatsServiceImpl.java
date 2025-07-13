package ru.practicum.stats.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime from, LocalDateTime to, List<String> endpoints, boolean onlyUnique) {
        boolean noFilter = (endpoints == null || endpoints.isEmpty());
        log.debug("getStats called: from={}, to={}, endpoints={}, onlyUnique={}", from, to, endpoints, onlyUnique);
        if (onlyUnique) {
            return noFilter
                    ? statsRepository.getStatsUniqueWithoutUriFilter(from, to)
                    : statsRepository.getStatsUnique(from, to, endpoints);
        } else {
            return noFilter
                    ? statsRepository.getStatsWithoutUriFilter(from, to)
                    : statsRepository.getStats(from, to, endpoints);
        }
    }
}