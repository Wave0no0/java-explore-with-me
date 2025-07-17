package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationSaveDto;
import ru.practicum.ewm.compilation.dto.CompilationUpdateDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(CompilationSaveDto compilationSaveDto) {
        log.info("Saving compilation");
        Compilation compilation = compilationMapper.mapToCompilation(compilationSaveDto);

        if (compilationSaveDto.getPinned() == null) {
            compilation.setPinned(false);
        }

        if (compilationSaveDto.getEvents() != null && !compilationSaveDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(compilationSaveDto.getEvents()));
            compilation.setEvents(events);
        } else {
            compilation.setEvents(Collections.emptySet());
        }
        CompilationDto compilationDto = compilationMapper.mapToCompilationDto(compilationRepository.save(compilation));
        log.info("Compilation saved successfully, compilationDto: {}", compilationDto);
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Search compilations");
        List<Compilation> compilations;

        if (pinned) {
            compilations = compilationRepository.findPinnedCompilationLimited(from, size);
        } else {
            compilations = compilationRepository.findCompilationLimited(from, size);
        }
        log.info("{} compilations were found", compilations.size());
        return compilations.stream()
                .map(compilationMapper::mapToCompilationDto)
                .toList();
    }


    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        CompilationDto compilationDto = compilationMapper.mapToCompilationDto(findCompilationById(compilationId));
        log.info("Compilation was found successfully, compilationDto: {}", compilationDto);
        return compilationDto;
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilationUpdateDto) {
        log.info("Updating compilation");
        Compilation compilation = findCompilationById(compilationId);

        if (compilationUpdateDto.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(compilationUpdateDto.getEvents()));
            compilation.setEvents(events);
        }
        if (compilationUpdateDto.getPinned() != null) {
            compilation.setPinned(compilationUpdateDto.getPinned());
        }
        if (compilationUpdateDto.getTitle() != null && !compilationUpdateDto.getTitle().isBlank()) {
            compilation.setTitle(compilationUpdateDto.getTitle());
        }
        CompilationDto compilationDto = compilationMapper.mapToCompilationDto(compilation);
        log.info("Compilation updated successfully, compilationDto: {}", compilationDto);
        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        log.info("Deleting compilation");
        findCompilationById(compilationId);
        compilationRepository.deleteById(compilationId);
        log.info("Compilation with id {} deleted successfully", compilationId);
    }

    private Compilation findCompilationById(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> {
                    log.warn("Compilation with id {} not found", compilationId);
                    return new NotFoundException(String.format("Compilation with id %d not found",
                            compilationId));
                });
    }
}
