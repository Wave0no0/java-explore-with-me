package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.AccessDeniedException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Saving participation request");
        User requester = findUserById(userId);
        Event event = findEventById(eventId);

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            log.warn("Duplicate request, userId: {}, eventId: {}", userId, eventId);
            throw new DuplicatedDataException(String.format(
                    "User with id %d has already submitted a request for event with id %d", userId, eventId)
            );
        }
        if (userId.equals(event.getInitiator().getId())) {
            log.warn("Request from initiator, userId: {}, eventId: {}", userId, eventId);
            throw new AccessDeniedException("Event initiator can't submit a request to their own event");
        }
        if (!State.PUBLISHED.equals(event.getState())) {
            log.warn("Request for unpublished event, userId: {}, eventId: {}", userId, eventId);
            throw new ConflictException("Only published events are available for requests");
        }
        checkParticipantLimit(event);

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setCreated(LocalDateTime.now());
        participationRequest.setEvent(event);
        participationRequest.setRequester(requester);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(State.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            participationRequest.setStatus(State.PENDING);
        }

        ParticipationRequestDto requestDto = requestMapper.mapToRequestDto(requestRepository.save(participationRequest));
        log.info("Participation request saved successfully, requestDto: {}", requestDto);
        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Search user's requests");
        List<ParticipationRequestDto> requestDtos = requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::mapToRequestDto)
                .toList();
        log.info("{} requests were found for userId {}", requestDtos.size(), userId);
        return requestDtos;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request");
        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Request with id {} not found", requestId);
                    return new NotFoundException(String.format("Request with id %d not found", requestId));
                });
        if (!userId.equals(participationRequest.getRequester().getId())) {
            log.warn("Cancel not available for user with id {}", userId);
            throw new AccessDeniedException("Only requester can cancel request");
        }
        participationRequest.setStatus(State.CANCELED);
        log.info("Participation request with id {} canceled successfully", requestId);
        return requestMapper.mapToRequestDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        log.info("Search requests for user's event");
        findEventById(eventId);
        List<ParticipationRequestDto> requestDtos = requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::mapToRequestDto)
                .toList();
        log.info("{} requests were found for eventId {}", requestDtos.size(), eventId);
        return requestDtos;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult moderateRequests(Long userId,
                                                           Long eventId,
                                                           EventRequestStatusUpdateRequest request) {
        log.info("Moderating requests");
        Event event = findEventById(eventId);

        if (!userId.equals(event.getInitiator().getId())) {
            log.warn("Moderation attempt not from initiator, userId: {}, eventId: {}", userId, eventId);
            throw new AccessDeniedException("Only initiator can moderate requests");
        }
        checkParticipantLimit(event);

        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndStatus(
                request.getRequestIds(), State.PENDING
        );

        if (requests.isEmpty()) {
            log.info("Pending requests for event with id {} not found", eventId);
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return moderateAll(State.CONFIRMED, requests);
        }
        if (State.REJECTED.equals(request.getStatus())) {
            return moderateAll(State.REJECTED, requests);
        }

        int availableCount = event.getParticipantLimit() - event.getConfirmedRequests();
        if (availableCount <= 0) {
            // Всё отклоняем
            requests.forEach(r -> r.setStatus(State.REJECTED));
            requestRepository.saveAll(requests);
            return new EventRequestStatusUpdateResult(List.of(), requestMapper.mapToListRequestDto(requests));
        }

        // Подтверждаем только availableCount заявок одним запросом
        List<Long> confirmedIds = requestRepository.updateStatusForIdsLimited(State.CONFIRMED.name(),
                request.getRequestIds(), availableCount);
        List<ParticipationRequest> confirmed = requests.stream()
                .filter(r -> confirmedIds.contains(r.getId()))
                .peek(r -> r.setStatus(State.CONFIRMED))
                .toList();
        List<ParticipationRequest> rejected = requests.stream()
                .filter(r -> !confirmedIds.contains(r.getId()))
                .peek(r -> r.setStatus(State.REJECTED))
                .toList();
        requestRepository.saveAll(rejected);
        event.setConfirmedRequests(event.getConfirmedRequests() + confirmed.size());
        log.info("Requests moderated, confirmed: {}, rejected: {}", confirmed.size(), rejected.size());
        return new EventRequestStatusUpdateResult(
                requestMapper.mapToListRequestDto(confirmed),
                requestMapper.mapToListRequestDto(rejected)
        );
    }

    private User findUserById(Long userId) {
        log.debug("Search user with id {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new NotFoundException(String.format("User with id %d not found", userId));
                });
    }

    private Event findEventById(Long eventId) {
        log.debug("Search event with id {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event with id {} not found", eventId);
                    return new NotFoundException(String.format("Event with id %d not found", eventId));
                });

    }

    private void checkParticipantLimit(Event event) {
        Integer participantLimit = event.getParticipantLimit();

        log.warn("Participant limit {} reached for eventId {}", participantLimit, event.getId());
        if (participantLimit != 0 && event.getConfirmedRequests() >= participantLimit) {
            throw new ConflictException(String.format("Event with id %d has reached participant limit", event.getId()));
        }
    }

    private EventRequestStatusUpdateResult moderateAll(State state, List<ParticipationRequest> requests) {
        log.debug("Applying {} status to all {} requests", state, requests.size());
        if (State.CONFIRMED.equals(state)) {
            requests.forEach(r -> r.setStatus(State.CONFIRMED));
            return new EventRequestStatusUpdateResult(requestMapper.mapToListRequestDto(requests), List.of());
        } else {
            requests.forEach(r -> r.setStatus(State.REJECTED));
            return new EventRequestStatusUpdateResult(List.of(), requestMapper.mapToListRequestDto(requests));
        }
    }
}
