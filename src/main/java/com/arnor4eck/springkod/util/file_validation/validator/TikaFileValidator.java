package com.arnor4eck.springkod.util.file_validation.validator;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file_validation.FileValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TikaFileValidator implements FileValidator{

    private final FileValidation imageFileValidator;

    private final FileValidation markupFileValidator;

    private final FileValidation metadataFileValidator;

    private final FileValidation probabilityFileValidator;

    public TikaFileValidator(@Qualifier("tikaImageFileValidator") FileValidation imageFileValidator,
                             @Qualifier("tikaMarkupFileValidator") FileValidation markupFileValidator,
                             @Qualifier("tikaMetadataFileValidator") FileValidation metadataFileValidator,
                             @Qualifier("tikaProbabilityFileValidator") FileValidation probabilityFileValidator){
        this.imageFileValidator = imageFileValidator;
        this.markupFileValidator = markupFileValidator;
        this.metadataFileValidator = metadataFileValidator;
        this.probabilityFileValidator = probabilityFileValidator;
    }

    @Override
    public boolean validate(byte[] bytes, FileType fileType) {
        return switch (fileType){
            case IMAGE -> validateImage(bytes);
            case MARKUP_FILE -> validateMarkupFile(bytes);
            case METADATA -> validateMetadataFile(bytes);
            case PROBABILITY ->  validateProbabilityFile(bytes);
            default -> throw new IllegalArgumentException("Некорректный формат файла");
        };
    }

    private boolean validateImage(byte[] bytes){
        return imageFileValidator.validate(bytes);
    }

    private boolean validateMarkupFile(byte[] bytes){
        return markupFileValidator.validate(bytes);
    }

    private boolean validateProbabilityFile(byte[] bytes){
        return probabilityFileValidator.validate(bytes);
    }

    private boolean validateMetadataFile(byte[] bytes){
        return metadataFileValidator.validate(bytes);
    }
}
