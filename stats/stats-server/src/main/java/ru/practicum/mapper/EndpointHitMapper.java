package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitSaveDto;
import ru.practicum.model.EndpointHit;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    EndpointHitDto mapToEndpointHitDto(EndpointHit endpointHit);

    EndpointHit mapToEndpointHit(EndpointHitSaveDto hitDto);
}
