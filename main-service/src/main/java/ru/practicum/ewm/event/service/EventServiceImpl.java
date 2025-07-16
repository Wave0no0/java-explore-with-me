package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitSaveDto;
import ru.practicum.StatisticsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.enums.EventSortType;
import ru.practicum.ewm.enums.EventStatus;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.AccessDeniedException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.InvalidDateException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.enums.StateAction;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatisticsClient statisticsClient;

    @Override
    @Transactional
    public EventDto createEvent(Long userId, EventSaveDto eventSaveDto) {
        log.info("Creating new event");
        User eventInitiator = findUserById(userId);
        Category eventCategory = findCategoryById(eventSaveDto.getCategory());

        validateEventDate(eventSaveDto.getEventDate());

        Event newEvent = eventMapper.mapToEvent(eventSaveDto);
        newEvent.setCategory(eventCategory);
        newEvent.setConfirmedRequests(0);
        newEvent.setInitiator(eventInitiator);
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setStatus(EventStatus.AWAITING_MODERATION);

        Location eventLocation = locationRepository.save(locationMapper.mapToLocation(eventSaveDto.getLocation()));
        newEvent.setLocation(eventLocation);

        setDefaultValues(newEvent, eventSaveDto);

        EventDto createdEvent = eventMapper.mapToEventDto(eventRepository.save(newEvent));
        log.info("Event created successfully, eventDto: {}", createdEvent);
        return createdEvent;
    }

    @Override
    public List<EventDto> findUserEvents(Long userId, Integer from, Integer size) {
        log.info("Finding user's events");
        List<EventDto> userEvents = eventRepository.findUserEventsLimited(userId, size, from).stream()
                .map(eventMapper::mapToEventDto)
                .toList();
        log.info("{} user events found", userEvents.size());
        return userEvents;
    }

    @Override
    public List<EventDto> findEventsForAdmin(List<Long> users, List<EventStatus> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Integer from, Integer size) {
        log.info("Finding events for admin");
        validateDateRange(rangeStart, rangeEnd);
        List<Event> adminEvents = eventRepository.findEventsWithFilters(
                users, states, categories, rangeStart, rangeEnd, from, size
        );
        log.info("{} events found for admin", adminEvents.size());
        return adminEvents.stream()
                .map(eventMapper::mapToEventDto)
                .toList();
    }

    @Override
    public EventDto findUserEventById(Long userId, Long eventId) {
        log.info("Finding event by user");
        EventDto foundEvent = eventMapper.mapToEventDto(findEventById(eventId));
        log.info("Event found successfully, eventDto: {}", foundEvent);
        return foundEvent;
    }

    @Override
    @Transactional
    public EventDto modifyEventByUser(Long userId, Long eventId, EventUpdateUserDto eventUpdate) {
        log.info("Modifying user's event");
        Event existingEvent = findEventById(eventId);
        validateUserOwnership(userId, existingEvent);
        validateEventModificationState(existingEvent);
        validateEventDateForModification(eventUpdate.getEventDate());

        eventMapper.updateEventFromUserRequest(eventUpdate, existingEvent);

        if (eventUpdate.getCategory() != null) {
            Category updatedCategory = findCategoryById(eventUpdate.getCategory());
            existingEvent.setCategory(updatedCategory);
        }

        processUserStateAction(eventUpdate.getStateAction(), existingEvent);
        EventDto modifiedEvent = eventMapper.mapToEventDto(existingEvent);
        log.info("User's event modified successfully, eventDto: {}", modifiedEvent);
        return modifiedEvent;
    }

    @Override
    @Transactional
    public EventDto moderateEventByAdmin(Long eventId, EventUpdateAdminDto eventUpdate) {
        log.info("Moderating event by admin");
        Event targetEvent = findEventById(eventId);

        validateEventDateForAdminModeration(eventUpdate.getEventDate(), targetEvent);

        eventMapper.updateEventFromAdminRequest(eventUpdate, targetEvent);

        if (eventUpdate.getCategory() != null) {
            Category updatedCategory = findCategoryById(eventUpdate.getCategory());
            targetEvent.setCategory(updatedCategory);
        }

        processAdminStateAction(eventUpdate.getStateAction(), targetEvent);
        EventDto moderatedEvent = eventMapper.mapToEventDto(targetEvent);
        log.info("Event moderated successfully by admin, eventDto: {}", moderatedEvent);
        return moderatedEvent;
    }

    @Override
    public EventDto findEventById(Long eventId, HttpServletRequest request) {
        Event targetEvent = findEventById(eventId);
        if (!EventStatus.PUBLISHED.equals(targetEvent.getStatus())) {
            log.warn("Event with id {} not found", eventId);
            throw new NotFoundException(String.format("Event with id %d not found", eventId));
        }
        recordEventView(request);
        EventDto foundEvent = eventMapper.mapToEventDto(targetEvent);
        foundEvent.setViews(calculateEventViews(targetEvent));
        log.info("Event found successfully, eventDto: {}", foundEvent);
        return foundEvent;
    }

    @Override
    public List<EventShortDto> findEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Boolean onlyAvailable, EventSortType sort, Integer from,
                                          Integer size, HttpServletRequest request) {
        log.info("Finding public events");
        validateDateRange(rangeStart, rangeEnd);
        List<Event> publicEvents = eventRepository.findPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size
        );
        recordEventView(request);
        List<EventShortDto> eventShortDtos = publicEvents.stream()
                .map(event -> {
                    EventShortDto eventShortDto = eventMapper.mapToEventShortDto(event);
                    eventShortDto.setViews(calculateEventViews(event));
                    return eventShortDto;
                })
                .toList();
        log.info("{} public events found", eventShortDtos.size());
        return applyEventSorting(eventShortDtos, sort);
    }

    private User findUserById(Long userId) {
        log.debug("Finding user with id {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new NotFoundException(String.format("User with id %d not found", userId));
                });
    }

    private Event findEventById(Long eventId) {
        log.debug("Finding event with id {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event with id {} not found", eventId);
                    return new NotFoundException(String.format("Event with id %d not found", eventId));
                });
    }

    private Category findCategoryById(Long categoryId) {
        log.debug("Finding category with id {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with id {} not found", categoryId);
                    return new NotFoundException(String.format("Category with id %d not found", categoryId));
                });
    }

    private void recordEventView(HttpServletRequest request) {
        log.debug("Recording event view, client ip: {}, path: {}", request.getRemoteAddr(), request.getRequestURI());
        EndpointHitSaveDto hitData = new EndpointHitSaveDto(
                "ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
        statisticsClient.recordEndpointHit(hitData);
        log.debug("Event view recorded successfully {}", hitData);
    }

    private Long calculateEventViews(Event event) {
        List<ViewStatsDto> viewStats = statisticsClient.retrieveViewStatistics(
                event.getPublishedOn(),
                LocalDateTime.now(),
                List.of("/events/" + event.getId()),
                true
        );
        return viewStats.isEmpty() ? 0 : viewStats.getFirst().getHits();
    }

    private List<EventShortDto> applyEventSorting(List<EventShortDto> events, EventSortType sort) {
        log.debug("Applying sorting to {} events by {}", events.size(), sort);
        return switch (sort) {
            case BY_VIEWS -> events.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed()).toList();
            case BY_DATE -> events.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate).reversed()).toList();
            case null -> events;
        };
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new InvalidDateException("End date cannot be before start date");
            }
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2L))) {
            log.warn("Cannot create event with invalid date");
            throw new InvalidDateException("Event cannot start earlier than 2 hours from now");
        }
    }

    private void validateUserOwnership(Long userId, Event event) {
        if (!userId.equals(event.getInitiator().getId())) {
            log.warn("Modification attempt not from initiator, userId: {}, eventId: {}", userId, event.getId());
            throw new AccessDeniedException("Only initiator can modify event");
        }
    }

    private void validateEventModificationState(Event event) {
        if (!EventStatus.AWAITING_MODERATION.equals(event.getStatus()) &&
            !EventStatus.REJECTED.equals(event.getStatus())) {
            log.warn("Event is in wrong state {}", event.getStatus());
            throw new ConflictException("Only awaiting moderation or rejected event can be modified");
        }
    }

    private void validateEventDateForModification(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2L))) {
            log.warn("Cannot modify event, <2 hours before start");
            throw new InvalidDateException("Cannot modify event because it starts in less than 2 hours");
        }
    }

    private void validateEventDateForAdminModeration(LocalDateTime eventDate, Event event) {
        if (eventDate != null && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1L))) {
            log.warn("Cannot moderate event {}, <1 hour before start", event);
            throw new InvalidDateException("Cannot moderate event because it starts in less than 1 hour");
        }
    }

    private void setDefaultValues(Event event, EventSaveDto eventSaveDto) {
        if (eventSaveDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (eventSaveDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (eventSaveDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
    }

    private void processUserStateAction(StateAction stateAction, Event event) {
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW -> event.setStatus(EventStatus.AWAITING_MODERATION);
                case CANCEL_REVIEW -> event.setStatus(EventStatus.REJECTED);
                default -> throw new IllegalArgumentException("State action is not supported");
            }
        }
    }

    private void processAdminStateAction(StateAction stateAction, Event event) {
        if (stateAction != null) {
            if (EventStatus.AWAITING_MODERATION.equals(event.getStatus())) {
                switch (stateAction) {
                    case PUBLISH_EVENT:
                        event.setStatus(EventStatus.PUBLISHED);
                        event.setPublishedOn(LocalDateTime.now());
                        break;
                    case REJECT_EVENT:
                        event.setStatus(EventStatus.REJECTED);
                        break;
                    default:
                        throw new IllegalArgumentException("State action is not supported");
                }
            } else {
                log.warn("Event is not awaiting moderation, state: {}", event.getStatus());
                throw new ConflictException("Event must be awaiting moderation for status moderation");
            }
        }
    }
}

