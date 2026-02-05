package com.adnanumar.linkedin.user_service.service;

import com.adnanumar.linkedin.user_service.dto.LoginRequestDto;
import com.adnanumar.linkedin.user_service.dto.SignupRequestDto;
import com.adnanumar.linkedin.user_service.dto.UserDto;
import com.adnanumar.linkedin.user_service.entity.User;
import com.adnanumar.linkedin.user_service.exception.BadRequestException;
import com.adnanumar.linkedin.user_service.exception.ResourceNotFoundException;
import com.adnanumar.linkedin.user_service.repository.UserRepository;
import com.adnanumar.linkedin.user_service.utils.PasswordUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    final UserRepository userRepository;

    final ModelMapper modelMapper;

    final JwtService jwtService;

    public UserDto signup(SignupRequestDto signupRequestDto) {
        log.info("signup request received");

        boolean exists = userRepository.existsByEmail(signupRequestDto.getEmail());
        if (exists) {
            throw new BadRequestException("User already exists, cannot signup again.");
        }

        User user = modelMapper.map(signupRequestDto, User.class);
        user.setPassword(PasswordUtil.hashPassword(signupRequestDto.getPassword()));
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public String login(LoginRequestDto loginRequestDto) {
        log.info("Login request received");
        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() ->
                new ResourceNotFoundException("User not found with email: " + loginRequestDto.getEmail()));

        boolean isPasswordMatch = PasswordUtil.checkPassword(loginRequestDto.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            throw new BadRequestException("Incorrect password");
        }

        return jwtService.generateAccessToken(user);
    }

}
