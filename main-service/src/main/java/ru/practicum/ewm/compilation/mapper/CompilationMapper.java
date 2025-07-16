package ru.practicum.ewm.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationSaveDto;
import ru.practicum.ewm.compilation.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    Compilation mapToCompilation(CompilationSaveDto compilationSaveDto);

    CompilationDto mapToCompilationDto(Compilation compilation);
}
