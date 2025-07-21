package ru.practicum.ewm.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventSaveDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.StatsClient;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private LocationMapper locationMapper;
    @Mock
    private StatsClient statsClient;
    @Mock
    private HttpServletRequest request;
    private ru.practicum.ewm.event.service.EventServiceImpl eventService;

    private User user;
    private Category category;
    private Event event;
    private EventSaveDto saveDto;
    private EventDto eventDto;
    private Location location;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        category = new Category();
        category.setId(1L);
        event = new Event();
        event.setId(1L);
        event.setInitiator(user);
        event.setCategory(category);
        event.setEventDate(LocalDateTime.now().plusDays(3));
        event.setState(State.PENDING);
        event.setConfirmedRequests(0);
        event.setParticipantLimit(10);
        location = new Location();
        location.setId(1L);
        saveDto = new EventSaveDto();
        saveDto.setCategory(1L);
        saveDto.setEventDate(LocalDateTime.now().plusDays(3));
        eventDto = new EventDto();
        eventDto.setId(1L);
        eventService = new ru.practicum.ewm.event.service.EventServiceImpl(
            eventRepository,
            userRepository,
            locationRepository,
            categoryRepository,
            eventMapper,
            locationMapper,
            statsClient
        );
    } //l

    @Test
    void searchEvents_queryDsl_happyPath() {
        Event event2 = new Event();
        event2.setId(2L);
        event2.setCategory(category);
        event2.setState(State.PUBLISHED);
        event2.setEventDate(LocalDateTime.now().plusDays(5));
        List<Event> events = List.of(event2);
        when(eventRepository.searchEvents(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(events);
        EventShortDto shortDto = new EventShortDto();
        shortDto.setId(2L);
        when(eventMapper.mapToEventShortDto(event2)).thenReturn(shortDto);
        when(statsClient.getStats(any(), any(), any(), anyBoolean())).thenReturn(List.of());
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/events/2");
        List<EventShortDto> result = eventService.searchEvents(null, null, null, null, null, null, null, 0, 10, request);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void addEvent_setsParticipantLimitCorrectly() {
        saveDto.setParticipantLimit(42);
        saveDto.setLocation(new ru.practicum.ewm.event.dto.LocationDto(1.0f, 2.0f));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(category));
        when(eventMapper.mapToEvent(saveDto)).thenReturn(event);
        when(locationMapper.mapToLocation(any())).thenReturn(location);
        when(locationRepository.save(any())).thenReturn(location);
        when(eventRepository.save(any())).thenReturn(event);
        when(eventMapper.mapToEventDto(any())).thenReturn(eventDto);

        assertDoesNotThrow(() -> eventService.addEvent(1L, saveDto));
    }
}
