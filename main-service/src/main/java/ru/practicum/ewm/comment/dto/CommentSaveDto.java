package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentSaveDto {
    @NotBlank(message = "Comment text must not be blank")
    private String text;
    @NotNull(message = "Event id must not be null")
    private Long eventId;
    @NotNull(message = "User id must not be null")
    private Long userId;
}