package com.taskmanagement.service;
import com.taskmanagement.dto.UserDto;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.UserNotFoundException;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto createUser(UserDto userDto) {
        log.info("Creating user with email: {}", userDto.getEmail());
        var user = UserDto.mapDtoToEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(user.getRole() != null ? (user.getRole()) : Role.USER);
        var savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return UserDto.mapEntityToDto(savedUser);
    }

    public UserDto updateUserById(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);

        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        existingUser.setRole(userDto.getRole() != null ? Role.valueOf(userDto.getRole()) : Role.USER);

        var updatedUser = userRepository.save(existingUser);
        log.info("User updated with ID: {}", updatedUser.getId());
        return UserDto.mapEntityToDto(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        userRepository.delete(existingUser);
        log.info("User deleted with ID: {}", id);
    }

    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);

        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        return UserDto.mapEntityToDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");

        var users = userRepository.findAll();
        var userDtos = users.stream()
                .map(UserDto::mapEntityToDto)
                .collect(Collectors.toList());

        log.info("Fetched {} users", userDtos.size());
        return userDtos;
    }
}
