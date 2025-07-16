package ru.practicum.ewm.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.QEvent;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Event> searchEvents(String text, List<Long> categories, Boolean paid,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                    Integer from, Integer size) {
        QEvent event = QEvent.event;

        return jpaQueryFactory
                .selectFrom(event)
                .where(text != null ? event.annotation.containsIgnoreCase(text)
                                .or(event.description.containsIgnoreCase(text)) : null,
                        categories != null ? event.category.id.in(categories) : null,
                        paid != null ? event.paid.eq(paid) : null,
                        rangeStart == null && rangeEnd == null ? event.eventDate.gt(LocalDateTime.now()) : null,
                        rangeStart != null ? event.eventDate.goe(rangeStart) : null,
                        rangeEnd != null ? event.eventDate.loe(rangeEnd) : null,
                        event.state.eq(State.PUBLISHED),
                        onlyAvailable != null && onlyAvailable ?
                                event.participantLimit.gt(event.confirmedRequests) : null)
                .offset(from)
                .limit(size)
                .fetch();
    }

    @Override
    public List<Event> findEventsFiltered(List<Long> users, List<State> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                          Integer from, Integer size) {
        QEvent event = QEvent.event;
        return jpaQueryFactory
                .selectFrom(event)
                .where(users != null ? event.initiator.id.in(users) : null,
                        states != null ? event.state.in(states) : null,
                        categories != null ? event.category.id.in(categories) : null,
                        rangeStart != null ? event.eventDate.goe(rangeStart) : null,
                        rangeEnd != null ? event.eventDate.loe(rangeEnd) : null)
                .offset(from)
                .limit(size)
                .fetch();
    }
}
