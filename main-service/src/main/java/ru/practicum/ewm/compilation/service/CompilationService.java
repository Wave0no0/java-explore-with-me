package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationSaveDto;
import ru.practicum.ewm.compilation.dto.CompilationUpdateDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(CompilationSaveDto compilationSaveDto);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compilationId);

    CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilationUpdateDto);

    void deleteCompilation(Long compilationId);
}
