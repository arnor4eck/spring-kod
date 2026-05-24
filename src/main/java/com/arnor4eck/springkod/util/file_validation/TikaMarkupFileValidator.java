package com.arnor4eck.springkod.util.file_validation;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class TikaMarkupFileValidator extends FullyEqualsTikaValidator {
    public TikaMarkupFileValidator(Tika tika) {
        super(tika, "text/csv");
    }
}
