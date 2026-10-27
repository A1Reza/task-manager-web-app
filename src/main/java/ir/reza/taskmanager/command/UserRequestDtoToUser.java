package ir.reza.taskmanager.command;

import ir.reza.taskmanager.dto.request.UserRequestDto;
import ir.reza.taskmanager.entity.User;

public class UserRequestDtoToUser {

    public User execute(UserRequestDto dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .build();
    }
}