package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveDto {

    @NotBlank(message = "User name must not be blank")
    @Length(min = 2, max = 250, message = "User name must be 2-250 characters")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    @Length(min = 6, max = 254, message = "Email must be 6-254 characters")
    private String email;
}
