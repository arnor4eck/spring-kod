package com.arnor4eck.springkod.util.file_validation;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileValidation {
    boolean validate(byte[] bytes);
    default boolean validate(MultipartFile file) throws IOException {
        return validate(file.getBytes());
    }
}
