package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;
import ru.practicum.ewm.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category mapToCategory(CategorySaveDto categorySaveDto);

    CategoryDto mapToCategoryDto(Category category);
}
