package ru.practicum.ewm.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("""
            select c
            from Compilation c
            where c.pinned = true
            order by c.id asc
            limit :size
            offset :from
            """)
    List<Compilation> findPinnedCompilationLimited(@Param("from") Integer from,
                                                   @Param("size") Integer size);

    @Query("""
            select c
            from Compilation c
            order by c.id asc
            limit :size
            offset :from
            """)
    List<Compilation> findCompilationLimited(@Param("from") Integer from,
                                             @Param("size") Integer size);
}
