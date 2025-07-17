package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitSaveDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.InvalidDateException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper hitMapper;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitSaveDto hitSaveDto) {
        EndpointHit endpointHit = hitMapper.mapToEndpointHit(hitSaveDto);
        return hitMapper.mapToEndpointHitDto(statsRepository.save(endpointHit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start != null && end != null) {
            if (end.isBefore(start)) {
                throw new InvalidDateException("End date can't be before start date");
            }
        }

        if (unique) {
            return statsRepository.findDistinctViewStats(start, end, uris);
        }
        return statsRepository.findAllViewStats(start, end, uris);
    }
}
