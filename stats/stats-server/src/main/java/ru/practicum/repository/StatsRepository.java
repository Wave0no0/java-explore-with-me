package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            select new ru.practicum.ViewStatsDto(
                eh.app,
                eh.uri,
                count(eh.ip) as hits
            )
            from EndpointHit eh
            where eh.timestamp between :start and :end
            and (:uris is null or eh.uri in :uris)
            group by eh.app, eh.uri
            order by count(eh.ip) desc
            """)
    List<ViewStatsDto> findAllViewStats(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("uris") List<String> uris);

    @Query("""
            select new ru.practicum.ViewStatsDto(
                eh.app,
                eh.uri,
                count(distinct eh.ip) as hits
            )
            from EndpointHit eh
            where eh.timestamp between :start and :end
            and (:uris is null or eh.uri in :uris)
            group by eh.app, eh.uri
            order by count(distinct eh.ip) desc
            """)
    List<ViewStatsDto> findDistinctViewStats(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("uris") List<String> uris);
}
