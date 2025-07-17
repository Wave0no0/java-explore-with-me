package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByIdInAndStatus(List<Long> requestIds, State status);

    @Modifying
    @Query(value = """
        UPDATE participation_request
        SET status = :status
        WHERE id IN (
            SELECT id FROM participation_request
            WHERE id IN :ids AND status = 'PENDING'
            LIMIT :limit
            FOR UPDATE
        )
        RETURNING id
        """, nativeQuery = true)
    List<Long> updateStatusForIdsLimited(@Param("status") String status, @Param("ids") List<Long> ids, @Param("limit") int limit);
}
