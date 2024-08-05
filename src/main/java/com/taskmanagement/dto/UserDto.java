package com.taskmanagement.dto;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

@Data
@Schema(description = "Data Transfer Object for User")
public class UserDto {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Schema(description = "Email of the user", example = "user@example.com")
    private String email;

    @Schema(description = "Name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Password of the user", example = "password123")
    private String password;

    @Schema(description = "Role of the user", example = "USER")
    private String role;

    public static UserDto mapEntityToDto(User user) {
        Objects.requireNonNull(user, "User entity cannot be null");
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole().toString());
        return dto;
    }

    public static User mapDtoToEntity(UserDto userDto) {
        Objects.requireNonNull(userDto, "UserDto cannot be null");
        User user = new User();
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setPassword(userDto.getPassword());
        user.setRole(Role.valueOf(userDto.getRole()));
        return user;
    }
}
