package ru.practicum.ewm.user.service;


import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserSaveDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserSaveDto userSaveDto);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}
