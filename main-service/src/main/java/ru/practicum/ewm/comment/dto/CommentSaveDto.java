package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentSaveDto {
    @NotBlank(message = "Comment text must not be blank")
    @Size(max = 1000, message = "Comment text must not exceed 1000 characters")
    private String text;
    @NotNull(message = "Event id must not be null")
    private Long eventId;
    @NotNull(message = "User id must not be null")
    private Long userId;
}