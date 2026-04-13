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

    //Get User with given emailId
    @GetMapping("/emailId/{emailId}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("emailId") String emailId){
        return ResponseEntity.ok(userService.getUserByEmail(emailId));
    }

    //delete user
    // /api/v1/users/{userId}
    @DeleteMapping("/{userId}")
    public void deleteUser( @PathVariable("userId") String userId){
        userService.deleteUser(userId);
    }

    //update user
    // /api/v1/users/{userId}
    @PutMapping("{userId}")
    public ResponseEntity<UserDto> updateUser( @RequestBody UserDto userDto , @PathVariable("userId") String Id){
        return ResponseEntity.ok(userService.updateUser(userDto , Id));
    }
}



