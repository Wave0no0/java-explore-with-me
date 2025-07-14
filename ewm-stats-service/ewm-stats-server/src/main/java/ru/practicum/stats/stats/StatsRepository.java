package ru.practicum.stats.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.hit.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(e))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :from AND :to
          AND e.uri IN :endpoints
        GROUP BY e.app, e.uri
        ORDER BY COUNT(e) DESC
        """)
    List<ViewStatsDto> getStats(LocalDateTime from, LocalDateTime to, List<String> endpoints);

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(e))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :from AND :to
        GROUP BY e.app, e.uri
        ORDER BY COUNT(e) DESC
        """)
    List<ViewStatsDto> getStatsWithoutUriFilter(LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :from AND :to
          AND e.uri IN :endpoints
        GROUP BY e.app, e.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
        """)
    List<ViewStatsDto> getStatsUnique(LocalDateTime from, LocalDateTime to, List<String> endpoints);

    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :from AND :to
        GROUP BY e.app, e.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
        """)
    List<ViewStatsDto> getStatsUniqueWithoutUriFilter(LocalDateTime from, LocalDateTime to);
}