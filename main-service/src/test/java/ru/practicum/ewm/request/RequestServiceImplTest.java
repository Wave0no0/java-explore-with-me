package ru.practicum.ewm.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RequestMapper requestMapper;
    @InjectMocks
    private ru.practicum.ewm.request.service.RequestServiceImpl requestService;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        event = new Event();
        event.setId(1L);
        event.setInitiator(new User());
        event.getInitiator().setId(2L);
        event.setState(State.PUBLISHED);
        event.setParticipantLimit(2);
        event.setConfirmedRequests(2);
    }

    @Test
    void addRequest_limitReached_throwsConflict() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findByRequesterIdAndEventId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(ConflictException.class, () -> requestService.addRequest(1L, 1L));
    }
}
