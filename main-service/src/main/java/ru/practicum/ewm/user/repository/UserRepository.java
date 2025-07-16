package ru.practicum.ewm.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findAllByIdIn(List<Long> ids);

    @Query("""
            select u
            from User u
            order by u.id asc
            limit :size
            offset :from
            """)
    List<User> findUsersLimited(@Param("from") Integer from,
                                @Param("size") Integer size);
}
