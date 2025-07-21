package ru.practicum.ewm.event.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.util.DateTimeUtil;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "category", ignore = true)
    Event mapToEvent(EventSaveDto eventSaveDto);

    @Mapping(target = "createdOn", source = "createdOn", dateFormat = DateTimeUtil.DATE_PATTERN)
    EventDto mapToEventDto(Event event);

    EventShortDto mapToEventShortDto(Event event);

    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserRequest(EventUpdateUserDto eventUpdate, @MappingTarget Event event);

    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminRequest(EventUpdateAdminDto eventUpdate, @MappingTarget Event event);
}
