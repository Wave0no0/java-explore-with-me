package ru.practicum.ewm.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.enums.State;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotNull(message = "List of request ids must not be null")
    private List<Long> requestIds;

    @NotNull(message = "Status for update must not be null")
    private State status;
}
