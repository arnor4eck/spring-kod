package com.arnor4eck.springkod.util.file_validation;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class TikaMetadataFileValidator extends FullyEqualsTikaValidator{
    public TikaMetadataFileValidator(Tika tika) {
        super(tika, "text/csv");
    }
}
