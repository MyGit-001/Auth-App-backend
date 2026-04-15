package com.example.auth.auth_app_backend.Services;

import com.example.auth.auth_app_backend.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);
}
