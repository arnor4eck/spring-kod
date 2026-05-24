package com.arnor4eck.springkod.util.file_validation;

import org.apache.tika.Tika;

public abstract class FullyEqualsTikaValidator implements FileValidation {

    private final Tika tika;

    private final String EQUAL_STRING;

    public FullyEqualsTikaValidator(Tika tika, String equalString) {
        this.tika = tika;
        this.EQUAL_STRING = equalString;
    }

    @Override
    public boolean validate(byte[] bytes){
        String detected = tika.detect(bytes);

        return EQUAL_STRING.equals(detected);
    }
}
