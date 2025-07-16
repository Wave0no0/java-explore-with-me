package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationUpdateDto {
    private Boolean pinned;

    @Length(min = 1, max = 50, message = "Compilation title must be 1-50 characters")
    private String title;

    private Set<Long> events;
}
