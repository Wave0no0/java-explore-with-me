package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitSaveDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.enums.Sort;
import ru.practicum.ewm.enums.State;
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
import ru.practicum.StatsClient;

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
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDto addEvent(Long userId, EventSaveDto eventSaveDto) {
        log.info("Saving event");
        User initiator = findUserById(userId);
        Category category = findCategoryById(eventSaveDto.getCategory());

        if (eventSaveDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            log.warn("Can't save event {} with invalid date", eventSaveDto);
            throw new InvalidDateException("Event can't start earlier than 2 hours from now");
        }

        Event event = eventMapper.mapToEvent(eventSaveDto);
        event.setCategory(category);
        event.setConfirmedRequests(0);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);

        Location location = locationRepository.save(locationMapper.mapToLocation(eventSaveDto.getLocation()));
        event.setLocation(location);
        event.setCategory(category);

        if (eventSaveDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (eventSaveDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (eventSaveDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        EventDto eventDto = eventMapper.mapToEventDto(eventRepository.save(event));
        log.info("Event saved successfully, eventDto: {}", eventDto);
        return eventDto;
    }

    @Override
    public List<EventDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Search user's events");
        List<EventDto> eventDtos = eventRepository.findUserEventsLimited(userId, size, from).stream()
                .map(eventMapper::mapToEventDto)
                .toList();
        log.info("{} events were found", eventDtos.size());
        return eventDtos;
    }

    @Override
    public List<EventDto> getEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Integer from, Integer size) {
        log.info("Search events by admin");
        checkRange(rangeStart, rangeEnd);
        List<Event> events = eventRepository.findEventsFiltered(
                users, states, categories, rangeStart, rangeEnd, from, size
        );
        log.info("{} events were found by admin", events.size());
        return events.stream()
                .map(eventMapper::mapToEventDto)
                .toList();
    }

    @Override
    public EventDto getUserEventById(Long userId, Long eventId) {
        log.info("Search event by user");
        EventDto eventDto = eventMapper.mapToEventDto(findEventById(eventId));
        log.info("Event was found successfully, eventDto: {}", eventDto);
        return eventDto;
    }

    @Override
    @Transactional
    public EventDto updateEventByUser(Long userId, Long eventId, EventUpdateUserDto eventUpdate) {
        log.info("Update user's event");
        Event event = findEventById(eventId);
        if (!userId.equals(event.getInitiator().getId())) {
            log.warn("Updating attempt not from initiator, userId: {}, eventId: {}", userId, eventId);
            throw new AccessDeniedException("Only initiator can update event");
        }
        if (!State.PENDING.equals(event.getState()) && !State.CANCELED.equals(event.getState())) {
            log.warn("Event is in wrong state {}", event.getState());
            throw new ConflictException("Only pending or canceled event can be updated");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            log.warn("Can't update event {}, <2 hours before start", event);
            throw new InvalidDateException("Can't update event because it starts in less than 2 hours");
        }

        eventMapper.updateEventFromUserRequest(eventUpdate, event);

        if (eventUpdate.getCategory() != null) {
            Category category = findCategoryById(eventUpdate.getCategory());
            event.setCategory(category);
        }

        if (eventUpdate.getStateAction() != null) {
            switch (eventUpdate.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new IllegalArgumentException("State action is not supported");
            }
        }
        EventDto eventDto = eventMapper.mapToEventDto(event);
        log.info("User's event updated successfully, eventDto: {}", eventDto);
        return eventDto;
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, EventUpdateAdminDto eventUpdate) {
        log.info("Update event by admin");
        Event event = findEventById(eventId);

        if (eventUpdate.getEventDate() != null
                && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1L))) {
            log.warn("Can't update event {}, <1 hour before start", event);
            throw new InvalidDateException("Can't update event because it starts in less than 1 hour");
        }

        eventMapper.updateEventFromAdminRequest(eventUpdate, event);

        if (eventUpdate.getCategory() != null) {
            Category category = findCategoryById(eventUpdate.getCategory());
            event.setCategory(category);
        }

        if (eventUpdate.getStateAction() != null) {
            if (State.PENDING.equals(event.getState())) {
                switch (eventUpdate.getStateAction()) {
                    case PUBLISH_EVENT:
                        event.setState(State.PUBLISHED);
                        event.setPublishedOn(LocalDateTime.now());
                        break;
                    case REJECT_EVENT:
                        event.setState(State.CANCELED);
                        break;
                    default:
                        throw new IllegalArgumentException("State action is not supported");
                }
            } else {
                log.warn("Event is not pending, state: {}", event.getState());
                throw new ConflictException("Event must be pending for status moderation");
            }
        }
        EventDto eventDto = eventMapper.mapToEventDto(event);
        log.info("Event updated successfully by admin, eventDto: {}", eventDto);
        return eventDto;
    }

    @Override
    public EventDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = findEventById(eventId);
        if (!State.PUBLISHED.equals(event.getState())) {
            log.warn("Event with id {} not found", eventId);
            throw new NotFoundException(String.format("Event with id %d not found", eventId));
        }
        sendStats(request);
        EventDto eventDto = eventMapper.mapToEventDto(event);
        eventDto.setViews(getEventViews(event));
        log.info("Event was found successfully, eventDto: {}", eventDto);
        return eventDto;
    }

    @Override
    public List<EventShortDto> searchEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, Sort sort, Integer from,
                                            Integer size, HttpServletRequest request) {
        log.info("Search public events");
        checkRange(rangeStart, rangeEnd);
        List<Event> events = eventRepository.searchEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size
        );
        sendStats(request);
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> {
                    EventShortDto eventShortDto = eventMapper.mapToEventShortDto(event);
                    eventShortDto.setViews(getEventViews(event));
                    return eventShortDto;
                })
                .toList();
        log.info("{} public events were found", eventShortDtos.size());
        return sortEvents(eventShortDtos, sort);
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

    private Category findCategoryById(Long categoryId) {
        log.debug("Search category with id {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with id {} not found", categoryId);
                    return new NotFoundException(String.format("Category with id %d not found", categoryId));
                });
    }

    private void sendStats(HttpServletRequest request) {
        log.debug("Save hit, client ip: {}, path: {}", request.getRemoteAddr(), request.getRequestURI());
        EndpointHitSaveDto hitSaveDto = new EndpointHitSaveDto(
                "ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
        statsClient.saveHit(hitSaveDto);
        log.debug("Hit saved successfully {}", hitSaveDto);
    }

    private Long getEventViews(Event event) {
        List<ViewStatsDto> stats = statsClient.getStats(
                event.getPublishedOn(),
                LocalDateTime.now(),
                List.of("/events/" + event.getId()),
                true
        );
        return stats.isEmpty() ? 0 : stats.getFirst().getHits();
    }

    private List<EventShortDto> sortEvents(List<EventShortDto> eventShortDtos, Sort sort) {
        log.debug("Sort {} events by {}", eventShortDtos.size(), sort);
        return switch (sort) {
            case VIEWS -> eventShortDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed()).toList();
            case EVENT_DATE -> eventShortDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate).reversed()).toList();
            case null -> eventShortDtos;
        };
    }

    private void checkRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new InvalidDateException("End date can't be before start date");
            }
        }
    }
}

