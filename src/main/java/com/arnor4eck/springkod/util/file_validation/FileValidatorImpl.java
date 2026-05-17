package com.arnor4eck.springkod.util.file_validation;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FileValidatorImpl implements FileValidator{

    private final ImageFilesValidation imageFilesValidation;

    private final MarkupFilesValidation markupFilesValidation;

    @Override
    public boolean validate(byte[] bytes, FileType fileType) {
        return switch (fileType){
            case IMAGE -> validateImage(bytes);
            case MARKUP_FILE ->  validateMarkupFile(bytes);
            default -> throw new IllegalArgumentException("Некорректный формат файла");
        };
    }

    private boolean validateImage(byte[] bytes){
        return imageFilesValidation.validate(bytes);
    }

    private boolean validateMarkupFile(byte[] bytes){
        return markupFilesValidation.validate(bytes);
    }
}
