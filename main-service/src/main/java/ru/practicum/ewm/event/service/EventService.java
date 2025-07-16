package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.enums.EventSortType;
import ru.practicum.ewm.enums.EventStatus;
import ru.practicum.ewm.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventDto createEvent(Long userId, EventSaveDto eventSaveDto);

    List<EventDto> findUserEvents(Long userId, Integer from, Integer size);

    List<EventDto> findEventsForAdmin(List<Long> users, List<EventStatus> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventDto findUserEventById(Long userId, Long eventId);

    EventDto modifyEventByUser(Long userId, Long eventId, EventUpdateUserDto eventUpdateDto);

    EventDto moderateEventByAdmin(Long eventId, EventUpdateAdminDto eventUpdateDto);

    EventDto findEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> findEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd, Boolean onlyAvailable, EventSortType sort, Integer from,
                                   Integer size, HttpServletRequest request);
}
