package com.arnor4eck.springkod.util.file_validation;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileValidator {
    boolean validate(byte[] bytes, FileType fileType);
    default boolean validate(MultipartFile file, FileType fileType) throws IOException {
        return validate(file.getBytes(), fileType);
    }
}
