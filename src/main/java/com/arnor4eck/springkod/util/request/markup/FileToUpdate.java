package com.arnor4eck.springkod.util.request.markup;

import jakarta.validation.constraints.NotBlank;

public record FileToUpdate(@NotBlank String fileName, @NotBlank String newLabel) {
}
