package ru.practicum.ewm.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.enums.State;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Long id;

    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private State status;
}
