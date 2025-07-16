package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(CategorySaveDto categorySaveDto);

    CategoryDto getCategoryById(Long categoryId);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto updateCategory(Long categoryId, CategorySaveDto categorySaveDto);

    void deleteCategoryById(Long categoryId);
}
