package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(CategorySaveDto categorySaveDto) {
        log.info("Saving category");
        Category category = categoryMapper.mapToCategory(categorySaveDto);
        checkCategoryName(category.getName());
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(categoryRepository.save(category));
        log.info("Category saved successfully, categoryDto: {}", categoryDto);
        return categoryDto;
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(findCategoryById(categoryId));
        log.info("Category was found successfully, categoryDto: {}", categoryDto);
        return categoryDto;
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Search categories");
        Pageable pageable = PageRequest.of(from / size, size);
        List<CategoryDto> categoryDtos = categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::mapToCategoryDto)
                .toList();
        log.info("{} categories were found", categoryDtos.size());
        return categoryDtos;
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategorySaveDto categorySaveDto) {
        log.info("Updating category");
        Category category = findCategoryById(categoryId);
        if (!categorySaveDto.getName().equals(category.getName())) {
            checkCategoryName(categorySaveDto.getName());
        }
        category.setName(categorySaveDto.getName());
        Category saved = categoryRepository.save(category);
        CategoryDto categoryDto = categoryMapper.mapToCategoryDto(saved);
        log.info("Category updated successfully, categoryDto {}", categoryDto);
        return categoryDto;
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) {
        log.info("Deleting category");
        Category category = findCategoryById(categoryId);
        if (!eventRepository.findByCategoryId(categoryId).isEmpty()) {
            log.warn("Deleting category with id {} has events", categoryId);
            throw new ConflictException(String.format(
                    "Can't delete category %s because it contains events", category.getName())
            );
        }
        categoryRepository.deleteById(categoryId);
        log.info("Category with id {} deleted successfully", categoryId);
    }

    private Category findCategoryById(Long categoryId) {
        log.debug("Search category with id {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with id {} not found", categoryId);
                    return new NotFoundException(String.format("Category with id %d not found", categoryId));
                });
    }

    private void checkCategoryName(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            log.warn("Name {} in use", name);
            throw new DuplicatedDataException(String.format(
                    "Category name %s is already in use", name)
            );
        }
    }
}
