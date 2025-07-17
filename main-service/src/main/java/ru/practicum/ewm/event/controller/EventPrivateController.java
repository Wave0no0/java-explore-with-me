package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventSaveDto;
import ru.practicum.ewm.event.dto.EventUpdateUserDto;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable Long userId,
                             @RequestBody @Valid EventSaveDto eventSaveDto) {
        log.info("POST /users/{userId}/events {}", eventSaveDto);
        return eventService.addEvent(userId, eventSaveDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getUserEvents(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /users/{userId}/events, userId: {}, from: {}, size: {}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEventById(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        log.info("GET /users/{userId}/events/{eventId}, eventId {}", eventId);
        return eventService.getUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEvent(@PathVariable Long userId,
                                @PathVariable Long eventId,
                                @RequestBody @Valid EventUpdateUserDto eventUpdateDto) {
        log.info("PATCH /users/{userId}/events/{eventId} {}", eventUpdateDto);
        return eventService.updateEventByUser(userId, eventId, eventUpdateDto);
    }
}
