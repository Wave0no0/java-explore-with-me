package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.enums.Sort;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventDto addEvent(Long userId, EventSaveDto eventSaveDto);

    List<EventDto> getUserEvents(Long userId, Integer from, Integer size);

    List<EventDto> getEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventDto getUserEventById(Long userId, Long eventId);

    EventDto updateEventByUser(Long userId, Long eventId, EventUpdateUserDto eventUpdateDto);

    EventDto updateEventByAdmin(Long eventId, EventUpdateAdminDto eventUpdateDto);

    EventDto getEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> searchEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd, Boolean onlyAvailable, Sort sort, Integer from,
                                     Integer size, HttpServletRequest request);
}
