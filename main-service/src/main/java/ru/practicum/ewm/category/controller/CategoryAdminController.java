package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategorySaveDto;
import ru.practicum.ewm.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid CategorySaveDto categorySaveDto) {
        log.info("POST /admin/categories {}", categorySaveDto);
        return categoryService.addCategory(categorySaveDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{catId}, catId: {}", catId);
        categoryService.deleteCategoryById(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto modifyCategory(@PathVariable Long catId, @RequestBody @Valid CategorySaveDto categorySaveDto) {
        log.info("PATCH /admin/categories/{catId} {}", categorySaveDto);
        return categoryService.updateCategory(catId, categorySaveDto);
    }
}
