package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class RequestPrivateController {
    private final RequestService requestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        log.info("POST /users/{userId}/requests, userId: {}, eventId: {}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findUserRequests(@PathVariable Long userId) {
        log.info("GET /users/{userId}/requests, userId: {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("PATCH /users/{userId}/requests/{requestId}/cancel, userId: {}, requestId: {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findEventRequests(@PathVariable Long userId,
                                                            @PathVariable Long eventId) {
        log.info("GET /users/{userId}/events/{eventId}/requests, userId: {}, eventId: {}", userId, eventId);
        return requestService.getUserEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult processRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId,
                                                          @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("PATCH /users/{userId}/events/{eventId}/requests, userId: {}, eventId: {}", userId, eventId);
        return requestService.moderateRequests(userId, eventId, request);
    }
}
