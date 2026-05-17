package com.arnor4eck.springkod.util.file_validation.validator;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file_validation.FileValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TikaFileValidator implements FileValidator{

    private final FileValidation imageFilesValidation;

    private final FileValidation markupFilesValidation;

    public TikaFileValidator(@Qualifier("tikaImageFilesValidation") FileValidation imageFilesValidation,
                             @Qualifier("tikaMarkupFilesValidation") FileValidation markupFilesValidation){
        this.imageFilesValidation = imageFilesValidation;
        this.markupFilesValidation = markupFilesValidation;
    }

    @Override
    public boolean validate(byte[] bytes, FileType fileType) {
        return switch (fileType){
            case IMAGE -> validateImage(bytes);
            case MARKUP_FILE -> validateMarkupFile(bytes);
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
