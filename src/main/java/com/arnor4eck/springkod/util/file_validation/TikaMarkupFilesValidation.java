package com.arnor4eck.springkod.util.file_validation;

import lombok.AllArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TikaMarkupFilesValidation implements FileValidation {

    private final Tika tika;

    private static final String[] MARKUP_STRINGS = {"text/csv", "text/plain"};

    @Override
    public boolean validate(byte[] bytes) {
        String detected = tika.detect(bytes);

        for(String markupString : MARKUP_STRINGS) {
            if(markupString.equals(detected)) {
                return true;
            }
        }

        return false;
    }
}
