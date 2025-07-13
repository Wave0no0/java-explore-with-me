package ru.practicum.stats.hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private final HitRepository hitRepository;

    @Override
    public void save(EndpointHitDto hit) {
        log.debug("Saving hit in service: {}", hit);
        hitRepository.save(HitMapper.toEntity(hit));
    }
}