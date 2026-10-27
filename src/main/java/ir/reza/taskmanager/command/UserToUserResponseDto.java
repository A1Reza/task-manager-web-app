package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.response.UserResponseDto;
import ir.reza.taskmanager.entity.User;

public class UserToUserResponseDto {

    public UserResponseDto execute(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}