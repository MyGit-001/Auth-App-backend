package com.example.auth.auth_app_backend.Controllers;

import com.example.auth.auth_app_backend.Services.UserService;
import com.example.auth.auth_app_backend.Services.UserServiceImpl;
import com.example.auth.auth_app_backend.dtos.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    //Create User API
    @PostMapping
    public ResponseEntity<UserDto> createUser( @RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    //Get All User Api
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
