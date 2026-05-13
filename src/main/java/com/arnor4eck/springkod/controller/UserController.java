package com.arnor4eck.springkod.controller;

import com.arnor4eck.springkod.service.UserService;
import com.arnor4eck.springkod.util.dto.user.UserDto;
import com.arnor4eck.springkod.util.request.AuthenticationRequest;
import com.arnor4eck.springkod.util.request.CreateUserRequest;
import com.arnor4eck.springkod.util.response.AuthenticationResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<@NonNull UserDto> registerUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                new UserDto(
                        userService.registration(createUserRequest)));
    }

    @PostMapping("/authentication")
    public ResponseEntity<@NonNull AuthenticationResponse> authenticateUser(@RequestBody @Valid AuthenticationRequest request) {
        AuthenticationResponse response = userService.authenticate(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
                new UserDto(userService.getUser(email))
        );
    }
}
