package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.enums.EventStatus;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> findPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, Boolean onlyAvailable, Integer from,
                                 Integer size);

    List<Event> findEventsWithFilters(List<Long> users, List<EventStatus> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                      Integer from, Integer size);
}
