package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    @Query("""
            select e
            from Event e
            where e.initiator.id = :userId
            order by e.id asc
            limit :size
            offset :from
            """)
    List<Event> findUserEventsLimited(@Param("userId") Long userId,
                                      @Param("size") Integer size,
                                      @Param("from") Integer from);

    List<Event> findByCategoryId(Long categoryId);

    List<Event> findAllByIdIn(Set<Long> ids);
}
