package com.example.auth.auth_app_backend.Services;

import com.example.auth.auth_app_backend.dtos.UserDto;


public interface UserService {
    //Create User
    UserDto createUser(UserDto userDto);
    //Update User
    UserDto updateUser(UserDto userDto, String userId);
    //Delete User
    void deleteUser(String id);

    UserDto getUserById(String id);
    UserDto getUserByEmail(String email);
    Iterable<UserDto> getAllUsers();
}