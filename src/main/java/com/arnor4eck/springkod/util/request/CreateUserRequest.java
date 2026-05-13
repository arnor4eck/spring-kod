package com.arnor4eck.springkod.util.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String username,
                                @NotBlank @Email(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$") String email,
                                @NotBlank String password) {
}
