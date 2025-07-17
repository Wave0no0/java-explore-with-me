package ru.practicum.ewm.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DuplicatedDataException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private ru.practicum.ewm.category.service.CategoryServiceImpl categoryService;

    private CategorySaveDto saveDto;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        saveDto = new CategorySaveDto();
        saveDto.setName("TestCategory");
        category = new Category();
        category.setId(1L);
        category.setName("TestCategory");
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("TestCategory");
    }

    @Test
    void addCategory_happyPath() {
        when(categoryMapper.mapToCategory(saveDto)).thenReturn(category);
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.mapToCategoryDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.addCategory(saveDto);
        assertNotNull(result);
        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
        verify(categoryRepository).findByName("TestCategory");
        verify(categoryRepository).save(category);
    }

    @Test
    void addCategory_duplicateName_throwsException() {
        when(categoryMapper.mapToCategory(saveDto)).thenReturn(category);
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.of(category));

        assertThrows(DuplicatedDataException.class, () -> categoryService.addCategory(saveDto));
        verify(categoryRepository).findByName("TestCategory");
        verify(categoryRepository, never()).save(any());
    }
} 