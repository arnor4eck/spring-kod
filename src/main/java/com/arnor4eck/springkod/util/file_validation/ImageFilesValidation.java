package com.arnor4eck.springkod.util.file_validation;

import org.springframework.stereotype.Component;

@Component
public class ImageFilesValidation implements FileValidation {
    @Override
    public boolean validate(byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
