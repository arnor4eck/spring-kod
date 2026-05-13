package com.arnor4eck.springkod.util.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(@NotBlank String email,
                                    @NotBlank String password) {
}
