package com.arnor4eck.springkod.util.file_validation;

import lombok.AllArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TikaImageFileValidator implements FileValidation {

    private final Tika tika;

    private static final String IMAGE_STRING = "image/";

    @Override
    public boolean validate(byte[] bytes) {
        String detected = tika.detect(bytes);

        return detected.startsWith(IMAGE_STRING);
    }
}
