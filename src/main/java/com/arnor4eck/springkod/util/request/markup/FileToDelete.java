package com.arnor4eck.springkod.util.request.markup;

import jakarta.validation.constraints.NotBlank;

public record FileToDelete(@NotBlank String fileName) {
}
