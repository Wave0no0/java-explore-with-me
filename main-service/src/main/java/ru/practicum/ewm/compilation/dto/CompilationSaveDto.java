package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationSaveDto {

    private Boolean pinned;

    @NotBlank(message = "Compilation title must not be blank")
    @Length(min = 1, max = 50, message = "Compilation title must be 1-50 characters")
    private String title;

    private Set<Long> events;
}
