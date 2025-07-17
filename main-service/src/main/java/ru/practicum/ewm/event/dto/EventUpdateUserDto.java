package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.enums.StateAction;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventUpdateUserDto {

    @Length(min = 20, max = 2000, message = "Event annotation must be 20-2000 characters")
    private String annotation;

    @Positive(message = "Category id must be positive")
    private Long category;

    @Length(min = 20, max = 7000, message = "Event description must be 20-7000 characters")
    private String description;

    @Future(message = "Event date must be in future")
    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Participant limit id must be positive or 0")
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    @Length(min = 3, max = 120, message = "Event title must be 3-120 characters")
    private String title;
}
