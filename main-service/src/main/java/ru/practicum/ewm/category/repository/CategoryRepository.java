package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.category.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    @Query("""
            select c
            from Category c
            order by c.id asc
            limit :size
            offset :from
            """)
    List<Category> findCategoriesLimited(@Param("from") Integer from,
                                         @Param("size") Integer size);
}
