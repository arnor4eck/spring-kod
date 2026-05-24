package com.arnor4eck.springkod;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file_validation.FileValidation;
import com.arnor4eck.springkod.util.file_validation.validator.TikaFileValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TikaFileValidatorTest {

    private TikaFileValidator tikaFileValidator;


    private FileValidation imageFileValidation;

    private FileValidation markupFileValidation;

    @BeforeEach
    void setUp() {
        imageFileValidation = mock(FileValidation.class);
        markupFileValidation = mock(FileValidation.class);
        var metadataFileValidation = mock(FileValidation.class);
        var probabilityFileValidation = mock(FileValidation.class);
        tikaFileValidator = new TikaFileValidator(imageFileValidation, markupFileValidation, metadataFileValidation, probabilityFileValidation);
    }

    @Test
    public void testImageValidateCorrect() {
        when(imageFileValidation.validate(any())).thenReturn(true);

        boolean validationResult = tikaFileValidator.validate(new byte[0], FileType.IMAGE);

        boolean result = true;
        Assertions.assertEquals(result, validationResult);
    }

    @Test
    public void testImageValidateIncorrect() {
        when(imageFileValidation.validate(any())).thenReturn(false);

        boolean validationResult = tikaFileValidator.validate(new byte[0], FileType.IMAGE);

        boolean result = false;
        Assertions.assertEquals(result, validationResult);
    }

    @Test
    public void testMarkupValidateCorrect() {
        when(markupFileValidation.validate(any())).thenReturn(true);

        boolean validationResult = tikaFileValidator.validate(new byte[0], FileType.MARKUP_FILE);

        boolean result = true;
        Assertions.assertEquals(result, validationResult);
    }

    @Test
    public void testMarkupValidateIncorrect() {
        when(markupFileValidation.validate(any())).thenReturn(false);

        boolean validationResult = tikaFileValidator.validate(new byte[0], FileType.MARKUP_FILE);

        boolean result = false;
        Assertions.assertEquals(result, validationResult);
    }
}
