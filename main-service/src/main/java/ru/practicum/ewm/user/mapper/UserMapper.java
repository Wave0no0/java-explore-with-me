package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserSaveDto;
import ru.practicum.ewm.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User mapToUser(UserSaveDto userSaveDto);

    UserDto mapToUserDto(User user);
}
