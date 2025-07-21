package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserSaveDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(UserSaveDto userSaveDto) {
        log.info("Saving user");
        User user = userMapper.mapToUser(userSaveDto);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Can't save user {} with duplicated email", userSaveDto.getName());
            throw new DuplicatedDataException(String.format("Email %s is already in use", user.getEmail()));
        }
        UserDto userDto = userMapper.mapToUserDto(userRepository.save(user));
        log.info("User saved successfully, userDto: {}", userDto);
        return userDto;
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Search users");
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids);
        } else {
            users = userRepository.findUsersLimited(from, size);
        }
        log.info("{} users were found", users.size());
        return users.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user");
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new NotFoundException(String.format("User with id %d not found", userId));
                });
        userRepository.deleteById(userId);
        log.info("User with id {} deleted successfully", userId);
    }
}
