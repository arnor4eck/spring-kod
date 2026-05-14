package com.arnor4eck.springkod.util.validation;

import com.arnor4eck.springkod.entity.datasitory.DatasitoryType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DatasitoryTypeValidator implements ConstraintValidator<DatasitoryTypeValid, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try{
            DatasitoryType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
