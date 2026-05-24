package com.arnor4eck.springkod.util.file_validation;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class TikaProbabilityFileValidator extends FullyEqualsTikaValidator{
    public TikaProbabilityFileValidator(Tika tika) {
        super(tika, "application/json");
    }
}
