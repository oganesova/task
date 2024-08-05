package com.taskmanagement.controller;

import com.taskmanagement.dto.UserDto;
import com.taskmanagement.entity.JwtRequest;
import com.taskmanagement.entity.JwtResponse;
import com.taskmanagement.security.JwtUtils;
import com.taskmanagement.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Operation(summary = "Authenticate user and generate JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Parameter(description = "User login request with email and password")
            @RequestBody JwtRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);

            log.info("User '{}' authenticated successfully", loginRequest.getEmail());
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (BadCredentialsException | DisabledException e) {
            log.error("Authentication failed for user '{}': {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Authentication failed for user '{}': {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
        }
    }

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Registration failed due to invalid input")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "User registration details")
            @RequestBody UserDto userDto) {
        try {
            UserDto registeredUser = userService.createUser(userDto);
            log.info("User '{}' registered successfully", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Registration failed for user '{}': Email already in use", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use. Please choose a different email.");
        } catch (Exception e) {
            log.error("Registration failed for user '{}': {}", userDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
        }
    }
}

