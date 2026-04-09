package com.example.auth.auth_app_backend.Services;

import com.example.auth.auth_app_backend.dtos.UserDto;
import com.example.auth.auth_app_backend.entities.Provider;
import com.example.auth.auth_app_backend.entities.User;
import com.example.auth.auth_app_backend.exceptions.ResourceNotFoundException;
import com.example.auth.auth_app_backend.helpers.UserHelper;
import com.example.auth.auth_app_backend.repositories.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepo userRepo, ModelMapper modelMapper) {
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto){
        if(userDto.getEmail()==null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if(userRepo.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        User user = modelMapper.map(userDto , User.class);
        user.setProvider(userDto.getProvider()!=null ? userDto.getProvider() : Provider.LOCAL);
        User savedUser = userRepo.save(user);
        return modelMapper.map(savedUser , UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        return null;
    }

    @Override
    public void deleteUser(String id) {
        UUID uId = UserHelper.parseUUID(id);
         User user = userRepo.findById(uId).orElseThrow( ()-> new ResourceNotFoundException("User not found by ID") );
        userRepo.delete(user);
    }

    @Override
    public UserDto getUserById(String id) {
        User user = userRepo.findById(UserHelper.parseUUID(id)).orElseThrow( ()-> new ResourceNotFoundException("User Not found by ID") );
        return modelMapper.map(user , UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow( ()->new ResourceNotFoundException("User not found with Email ID"));
        return modelMapper.map(user , UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepo
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user , UserDto.class))
                .toList();
    }
}
