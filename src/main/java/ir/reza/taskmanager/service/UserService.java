package ir.reza.taskmanager.service;

import ir.reza.taskmanager.command.UserRequestDtoToUser;
import ir.reza.taskmanager.command.UserToUserResponseDto;
import ir.reza.taskmanager.dto.request.UserRequestDto;
import ir.reza.taskmanager.dto.response.UserResponseDto;
import ir.reza.taskmanager.entity.User;
import ir.reza.taskmanager.exception.DuplicateResourceException;
import ir.reza.taskmanager.exception.ResourceNotFoundException;
import ir.reza.taskmanager.repository.user.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    private final UserRequestDtoToUser requestConverter =
            new UserRequestDtoToUser();

    private final UserToUserResponseDto responseConverter =
            new UserToUserResponseDto();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(responseConverter::execute)
                .toList();
    }

    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + id
            );
        }

        return responseConverter.execute(user);
    }

    public UserResponseDto add(UserRequestDto dto) {
        boolean emailExists = userRepository.findAll()
                .stream()
                .anyMatch(user ->
                        user.getEmail().equalsIgnoreCase(dto.email())
                );

        if (emailExists) {
            throw new DuplicateResourceException(
                    "User email already exists: " + dto.email()
            );
        }

        User user = requestConverter.execute(dto);
        User savedUser = userRepository.add(user);

        return responseConverter.execute(savedUser);
    }
}